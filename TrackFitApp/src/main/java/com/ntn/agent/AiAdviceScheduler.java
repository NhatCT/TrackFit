package com.ntn.agent;

import com.ntn.dto.RecommendationParamsDTO;
import com.ntn.pojo.User;
import com.ntn.repositories.UserRepository;
import com.ntn.services.AiAdviceService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AiAdviceScheduler {

    private final UserRepository userRepo;
    private final AiAdviceService adviceService;

    @Value("${ai.advice.enabled:true}") private boolean enabled;
    @Value("${ai.advice.top:3}") private int top;
    @Value("${ai.advice.withinDays:1}") private int withinDays;

    public AiAdviceScheduler(UserRepository userRepo, AiAdviceService adviceService) {
        this.userRepo = userRepo;
        this.adviceService = adviceService;
    }

    @Scheduled(cron = "0 0 7 * * *", zone = "Asia/Ho_Chi_Minh")
    public void dailyAdvice() {
        if (!enabled) return;
        for (User u : userRepo.findAll()) {
            try {
                int created = adviceService.sendAdviceFromRecoIfNotExists(
                        u.getUsername(), new RecommendationParamsDTO(), top, withinDays);
                System.out.printf("[AI-ADVICE] %s -> %d notifications%n", u.getUsername(), created);
            } catch (Exception e) {
                System.err.printf("[AI-ADVICE] error %s: %s%n", u.getUsername(), e.getMessage());
            }
        }
    }
}
