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

    private String baseUrl;
    private HttpClient http;
    private final ObjectMapper mapper = new ObjectMapper();

    public AiRecoServiceImpl() {}
    public AiRecoServiceImpl(String baseUrl) { this.baseUrl = baseUrl; }

    @PostConstruct
    public void init() {
        if (this.baseUrl == null || this.baseUrl.isBlank()) {
            String fromProp = aiRecoUrlProp;
            String fromEnv  = System.getenv("AI_RECO_BASE_URL");
            this.baseUrl = (fromProp != null && !fromProp.isBlank())
                    ? fromProp
                    : (fromEnv != null && !fromEnv.isBlank() ? fromEnv : "http://localhost:8000");
        }

        // Ép dùng HTTP/1.1 và để HttpClient tự set Content-Length
        this.http = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofMillis(connectTimeoutMs))
                .build();

        System.out.println("[AI RECO] baseUrl=" + this.baseUrl);
    }

    private HttpResponse<String> sendWithRetry(HttpRequest req) throws IOException, InterruptedException {
        int attempt = 0;
        IOException lastIo = null;
        InterruptedException lastInt = null;

        while (attempt < Math.max(1, retryAttempts)) {
            attempt++;
            try {
                HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
                int code = resp.statusCode();
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
                .timeout(Duration.ofMillis(readTimeoutMs));
    }

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

            byte[] payload = mapper.writeValueAsBytes(body);
            HttpRequest req = baseReq("/reindex")
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Accept", "application/json")
                    // KHÔNG set Content-Length!
                    .POST(HttpRequest.BodyPublishers.ofByteArray(payload))
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

            byte[] payload = mapper.writeValueAsBytes(body);
            HttpRequest req = baseReq("/rank")
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(payload))
                    .build();

            HttpResponse<String> resp = sendWithRetry(req);

            Map<String, Object> result = mapper.readValue(resp.body(), new TypeReference<Map<String, Object>>() {});
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
        ChatAnswerDTO fallback = new ChatAnswerDTO();
        fallback.setAnswer("Có lỗi khi gọi chatbot. Vui lòng thử lại.");
        fallback.setModel("");

        try {
            Map<String, Object> body = new HashMap<>();
            body.put("sessionId", sessionId);
            body.put("question", question);
            body.put("topK", topK != null ? topK : 4);

            byte[] payload = mapper.writeValueAsBytes(body);
            HttpRequest req = baseReq("/chat")
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Accept", "application/json")
                    // TUYỆT ĐỐI không thêm Content-Length
                    .POST(HttpRequest.BodyPublishers.ofByteArray(payload))
                    .build();

            HttpResponse<String> resp = sendWithRetry(req);

            System.out.println("[AI CHAT] status=" + resp.statusCode() + " body=" + resp.body());

            if (resp.statusCode() != 200 || resp.body() == null || resp.body().isBlank()) {
                fallback.setAnswer("AI hiện không phản hồi. Thử lại sau.");
                fallback.setModel("offline");
                return fallback;
            }

            Map<String, Object> result = mapper.readValue(
                    resp.body(),
                    new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {}
            );

            ChatAnswerDTO ans = new ChatAnswerDTO();
            Object a = result.get("answer");
            Object m = result.get("model");
            ans.setAnswer(a == null ? "Mình chưa có câu trả lời." : String.valueOf(a));
            ans.setModel(m == null ? "" : String.valueOf(m));
            return ans;

        } catch (Exception ex) {
            System.err.println("[AI CHAT] error: " + ex.getMessage());
            return fallback;
        }
    }
}
