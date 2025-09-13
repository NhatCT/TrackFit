package com.ntn.controllers;

import com.ntn.dto.*;
import com.ntn.services.AiRecoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/api/secure/ai")
public class AiRecoController {

    private final AiRecoService ai;
    @Autowired
    public AiRecoController(AiRecoService ai) {
        this.ai = ai;
    }

    // Health check AI
    @GetMapping("/health")
    public ResponseEntity<?> aiHealth(){
        boolean ok = ai.aiHealth();
        return ResponseEntity.ok(Map.of("ok", ok));
    }

    // Manual reindex: frontend có thể gửi items để build index
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

    // Chatbot proxy → AI Reco /chat
    @PostMapping("/chat/ask")
    public ResponseEntity<ChatAnswerDTO> chatAsk(@RequestBody ChatRequestDTO body){
        ChatAnswerDTO ans = ai.chatAsk(
                body.getSessionId(), body.getQuestion(), body.getTopK()
        );
        return ResponseEntity.ok(ans);
    }
}
