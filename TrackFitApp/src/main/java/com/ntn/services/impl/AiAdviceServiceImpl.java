package com.ntn.services.impl;

import com.ntn.dto.NotificationCreateDTO;
import com.ntn.dto.RecommendationItemDTO;
import com.ntn.dto.RecommendationParamsDTO;
import com.ntn.repositories.NotificationRepository;
import com.ntn.repositories.UserRepository;
import com.ntn.services.AiAdviceService;
import com.ntn.services.NotificationService;
import com.ntn.services.RecommendationService;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiAdviceServiceImpl implements AiAdviceService {

    @Autowired private RecommendationService recoService;
    @Autowired private NotificationService notificationService;
    @Autowired private UserRepository userRepo;
    @Autowired private NotificationRepository notifRepo;

    @Override
    public int sendAdviceFromReco(String username, RecommendationParamsDTO params, int top) {
        return sendAdviceFromRecoIfNotExists(username, params, top, 0); // 0 = không chống trùng
    }

    @Override
    public int sendAdviceFromRecoIfNotExists(String username, RecommendationParamsDTO params,
                                             int top, int withinDays) {
        if (params == null) params = new RecommendationParamsDTO();
        if (top <= 0) top = 3;

        var user = userRepo.getUserByUsername(username);
        if (user == null) return 0;

        var recs = recoService.recommendExercises(username, params);
        if (recs == null || recs.isEmpty()) return 0;

        Date since = withinDays > 0
                ? new Date(System.currentTimeMillis() - withinDays * 24L * 3600_000L)
                : new Date(0L);

        int count = 0;
        for (int i = 0; i < Math.min(top, recs.size()); i++) {
            var it = recs.get(i);
            StringBuilder msg = new StringBuilder("Gợi ý hôm nay: ")
                    .append(it.getName() != null ? it.getName() : "bài tập");
            if (it.getEstimatedMinutes() != null) msg.append(" (").append(it.getEstimatedMinutes()).append("′)");
            if (it.getReason() != null && !it.getReason().isBlank()) msg.append(". Lý do: ").append(it.getReason());
            String message = msg.toString();

            // chống trùng theo message trong withinDays
            long existed = notifRepo.countSimilarMessageSince(user.getUserId(), message, since);
            if (withinDays > 0 && existed > 0) continue;

            var dto = new NotificationCreateDTO();
            dto.setMessage(message);
            dto.setType("advice");   // enum DB chữ thường
            dto.setSource("AI");
            dto.setSender("AI Coach");

            notificationService.createForUsername(username, dto);
            count++;
        }
        return count;
    }
}
