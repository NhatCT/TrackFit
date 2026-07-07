package com.ntn.controllers;

import com.ntn.dto.*;
import com.ntn.repositories.UserRepository;
import com.ntn.repositories.HealthDataRepository;
import com.ntn.repositories.GoalRepository;
import com.ntn.pojo.User;
import com.ntn.pojo.HealthData;
import com.ntn.pojo.Goal;
import com.ntn.services.AiRecoService;
import com.ntn.services.ChatQuotaService;
import com.ntn.services.PremiumService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/api/secure/ai")
public class AiRecoController {

    private final AiRecoService ai;
    @Autowired private UserRepository userRepo;
    @Autowired private PremiumService premiumService;
    @Autowired private ChatQuotaService chatQuotaService;
    @Autowired private HealthDataRepository healthRepo;
    @Autowired private GoalRepository goalRepo;

    @Autowired
    public AiRecoController(AiRecoService ai) {
        this.ai = ai;
    }

    @GetMapping("/health")
    public ResponseEntity<?> aiHealth(){
        boolean ok = ai.aiHealth();
        return ResponseEntity.ok(Map.of("ok", ok));
    }

    @PostMapping("/reindex")
    public ResponseEntity<?> aiReindex(@RequestBody Map<String, Object> body){
        Object raw = body.get("items");
        if(!(raw instanceof List<?>)) return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "items[] required"));
        @SuppressWarnings("unchecked")
        List<Map<String,Object>> maps = (List<Map<String,Object>>) raw;

        List<RankCandidateDTO> items = new ArrayList<>();
        for(Map<String,Object> m: maps){
            Object id = m.get("id");
            String title = (String)m.getOrDefault("title", "");
            String text  = (String)m.getOrDefault("text", "");
            String group = (String)m.getOrDefault("group", null);
            items.add(new RankCandidateDTO(id, title, text, group));
        }
        boolean ok = ai.reindexManual(items);
        return ResponseEntity.ok(Map.of("ok", ok));
    }

    @GetMapping("/recommendations/auto")
    public ResponseEntity<List<RankResponseItemDTO>> autoReco(
            @RequestParam(name="q", defaultValue = "gợi ý bài tập phù hợp") String query,
            @RequestParam(name="topK", defaultValue = "8") Integer topK
    ){
        List<RankCandidateDTO> candidates = new ArrayList<>();
        candidates.add(new RankCandidateDTO(101, "Chống đẩy", "Ngực - tay sau, 3x10-12", "chest"));
        candidates.add(new RankCandidateDTO(102, "Squat", "Đùi - mông, 4x8-10", "legs"));
        candidates.add(new RankCandidateDTO(103, "Plank", "Core/abs, 3x45-60s", "core"));

        List<RankResponseItemDTO> ranked = ai.rankExercises(query, candidates, topK);
        return ResponseEntity.ok(ranked);
    }

    @PostMapping("/chat/ask")
    public ResponseEntity<ChatAnswerDTO> chatAsk(@RequestBody ChatRequestDTO body, Principal principal){
        User u = userRepo.getUserByUsername(principal.getName());
        boolean premium = premiumService.isPremiumActive(u);
        chatQuotaService.consumeOrThrow(u.getUserId(), premium);

        StringBuilder context = new StringBuilder();
        context.append("[Bối cảnh người dùng: ");
        if (u.getGender() != null) {
            context.append("Giới tính: ").append("Male".equalsIgnoreCase(u.getGender()) ? "Nam" : "Nữ").append(", ");
        }
        if (u.getBirthDate() != null) {
            int age = java.time.LocalDate.now().getYear() - u.getBirthDate().getYear();
            context.append(age).append(" tuổi, ");
        }

        // Get latest health data
        List<HealthData> hdList = healthRepo.findByUserId(u.getUserId());
        if (hdList != null && !hdList.isEmpty()) {
            HealthData latest = hdList.get(hdList.size() - 1);
            if (latest.getHeight() != null && latest.getWeight() != null) {
                double h = latest.getHeight().doubleValue();
                double w = latest.getWeight().doubleValue();
                double bmi = w / Math.pow(h / 100.0, 2);
                String bmiLabel = "Bình thường";
                if (bmi < 18.5) bmiLabel = "Thiếu cân";
                else if (bmi >= 23 && bmi < 25) bmiLabel = "Thừa cân";
                else if (bmi >= 25) bmiLabel = "Béo phì";
                
                context.append(String.format("Chiều cao: %.1fcm, Cân nặng: %.1fkg, BMI: %.1f (%s), ", h, w, bmi, bmiLabel));
            }
        }

        // Get goal
        List<Goal> goalList = goalRepo.findByUserId(u.getUserId());
        if (goalList != null && !goalList.isEmpty()) {
            Goal g = goalList.get(goalList.size() - 1);
            String goalLabel = g.getGoalType();
            if ("fat_loss".equals(goalLabel)) goalLabel = "Giảm mỡ";
            else if ("muscle_gain".equals(goalLabel)) goalLabel = "Tăng cơ";
            else if ("endurance".equals(goalLabel)) goalLabel = "Sức bền";
            else if ("flexibility".equals(goalLabel)) goalLabel = "Dẻo dai";
            else if ("general_fitness".equals(goalLabel)) goalLabel = "Thể lực chung";
            
            String intensityLabel = g.getIntensity();
            if ("Low".equals(intensityLabel)) intensityLabel = "Thấp";
            else if ("Medium".equals(intensityLabel)) intensityLabel = "Trung bình";
            else if ("High".equals(intensityLabel)) intensityLabel = "Cao";
            
            context.append("Mục tiêu: ").append(goalLabel).append(", Cường độ: ").append(intensityLabel).append(". ");
        }
        context.append("Tư vấn cá nhân hóa ngắn gọn, tự nhiên, thân thiện dựa trên thể trạng này]. ");

        String personalizedQuestion = context.toString() + body.getQuestion();

        ChatAnswerDTO ans = ai.chatAsk(
                body.getSessionId(), personalizedQuestion, body.getTopK()
        );
        return ResponseEntity.ok(ans);
    }
}
