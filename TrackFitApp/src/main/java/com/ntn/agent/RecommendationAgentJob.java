package com.ntn.agent;

import com.ntn.dto.RecommendationParamsDTO;
import com.ntn.pojo.User;
import com.ntn.repositories.UserRepository;
import com.ntn.services.RecommendationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RecommendationAgentJob {

    private final UserRepository userRepo;
    private final RecommendationService recoService;

    @Value("${agent.autorun.enabled:true}")
    private boolean enabled;

    @Value("${agent.autoad.enabled:true}") 
    private boolean autoAddEnabled;

    public RecommendationAgentJob(UserRepository userRepo, RecommendationService recoService) {
        this.userRepo = userRepo;
        this.recoService = recoService;
    }

    @Scheduled(cron = "0 0 11 * * *", zone = "Asia/Ho_Chi_Minh")
    public void morningCoach() {
        if (!enabled) return;
        List<User> users = userRepo.findAll();
        for (User u : users) {
            try {
                var p = new RecommendationParamsDTO(); 
                var recs = recoService.recommendExercises(u.getUsername(), p);
                System.out.printf("[AGENT] prepared %d suggestions for %s%n", recs.size(), u.getUsername());

                if (autoAddEnabled && !recs.isEmpty()) {
 
                }
            } catch (Exception e) {
                System.err.printf("[AGENT] error for %s: %s%n", u.getUsername(), e.getMessage());
            }
        }
    }
}

