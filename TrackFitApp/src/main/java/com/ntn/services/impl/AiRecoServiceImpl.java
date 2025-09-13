package com.ntn.services.impl;

import com.ntn.dto.ChatAnswerDTO;
import com.ntn.dto.RankCandidateDTO;
import com.ntn.dto.RankResponseItemDTO;
import com.ntn.services.AiRecoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.http.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.io.IOException;

@Service
public class AiRecoServiceImpl implements AiRecoService {

    // ========== cấu hình từ application.properties ==========
    @Value("${ai.reco.url:}")
    private String aiRecoUrlProp;

    @Value("${ai.http.connect-timeout-ms:5000}")
    private int connectTimeoutMs;

    @Value("${ai.http.read-timeout-ms:30000}")
    private int readTimeoutMs;

    @Value("${ai.http.retry.attempts:3}")
    private int retryAttempts;

    @Value("${ai.http.retry.backoff-ms:400}")
    private int backoffMs;

    // ========================================================
    private String baseUrl;                 // URL cuối cùng dùng để gọi AI
    private HttpClient http;                // HTTP client có connect-timeout
    private final ObjectMapper mapper = new ObjectMapper();

    public AiRecoServiceImpl() {
        // no-args constructor cho Spring
    }

    /** Cho phép tạo bean thủ công (nếu bạn muốn new AiRecoServiceImpl("...")). */
    public AiRecoServiceImpl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @PostConstruct
    public void init() {
        // Ưu tiên: constructor -> property ai.reco.url -> ENV AI_RECO_BASE_URL -> default
        if (this.baseUrl == null || this.baseUrl.isBlank()) {
            String fromProp = aiRecoUrlProp;
            String fromEnv  = System.getenv("AI_RECO_BASE_URL");
            this.baseUrl = (fromProp != null && !fromProp.isBlank())
                    ? fromProp
                    : (fromEnv != null && !fromEnv.isBlank() ? fromEnv : "http://localhost:8000");
        }

        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(connectTimeoutMs))
                .build();
    }

    // -------------------- tiện ích gửi kèm retry --------------------
    private HttpResponse<String> sendWithRetry(HttpRequest req) throws IOException, InterruptedException {
        int attempt = 0;
        IOException lastIo = null;
        InterruptedException lastInt = null;

        while (attempt < Math.max(1, retryAttempts)) {
            attempt++;
            try {
                HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
                int code = resp.statusCode();
                // retry nếu 5xx hoặc 429
                if (code >= 500 || code == 429) {
                    Thread.sleep(backoffMs * (long) attempt);
                    continue;
                }
                return resp;
            } catch (IOException e) {
                lastIo = e;
                Thread.sleep(backoffMs * (long) attempt);
            } catch (InterruptedException e) {
                lastInt = e;
                Thread.sleep(backoffMs * (long) attempt);
            }
        }
        if (lastIo != null) throw lastIo;
        if (lastInt != null) throw lastInt;
        throw new IOException("HTTP retry failed without specific exception");
    }

    private HttpRequest.Builder baseReq(String path) {
        return HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .timeout(Duration.ofMillis(readTimeoutMs)); // read-timeout per request
    }

    // ======================= API public =======================

    @Override
    public boolean aiHealth() {
        try {
            HttpRequest req = baseReq("/health").GET().build();
            HttpResponse<String> resp = sendWithRetry(req);
            return resp.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean reindexManual(List<RankCandidateDTO> items) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("items", items);

            String json = mapper.writeValueAsString(body);
            HttpRequest req = baseReq("/reindex")
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> resp = sendWithRetry(req);
            return resp.statusCode() == 200;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<RankResponseItemDTO> rankExercises(String query, List<RankCandidateDTO> candidates, Integer topK) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("query", query);
            body.put("candidates", candidates);
            body.put("topK", topK != null ? topK : 10);

            String json = mapper.writeValueAsString(body);
            HttpRequest req = baseReq("/rank")
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> resp = sendWithRetry(req);

            Map<String, Object> result = mapper.readValue(resp.body(), new TypeReference<Map<String, Object>>(){});
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) result.get("items");

            List<RankResponseItemDTO> out = new ArrayList<>();
            if (items != null) {
                for (Map<String, Object> m : items) {
                    RankResponseItemDTO it = new RankResponseItemDTO();
                    it.setId(m.get("id"));
                    it.setTitle((String) m.get("title"));
                    it.setText((String) m.get("text"));
                    it.setGroup((String) m.get("group"));
                    Object sc = m.get("score");
                    it.setScore(sc == null ? null : Double.valueOf(sc.toString()));
                    out.add(it);
                }
            }
            return out;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public ChatAnswerDTO chatAsk(String sessionId, String question, Integer topK) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("sessionId", sessionId);
            body.put("question", question);
            body.put("topK", topK != null ? topK : 4);

            String json = mapper.writeValueAsString(body);
            HttpRequest req = baseReq("/chat")
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> resp = sendWithRetry(req);

            Map<String, Object> result = mapper.readValue(resp.body(), new TypeReference<Map<String, Object>>(){});
            ChatAnswerDTO ans = new ChatAnswerDTO();
            ans.setAnswer((String) result.get("answer"));
            ans.setModel((String) result.get("model"));
            return ans;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
