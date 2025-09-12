package com.ntn.services.impl;

import com.ntn.dto.RecommendationItemDTO;
import com.ntn.dto.RecommendationParamsDTO;
import com.ntn.pojo.Notification;
import com.ntn.pojo.User;
import com.ntn.repositories.NotificationRepository;
import com.ntn.repositories.UserRepository;
import com.ntn.services.AiAdviceService;
import com.ntn.services.RecommendationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public class AiAdviceServiceImpl implements AiAdviceService {

    private final RecommendationService recoService;
    private final UserRepository userRepo;
    private final NotificationRepository notifRepo;

    public AiAdviceServiceImpl(RecommendationService recoService,
                               UserRepository userRepo,
                               NotificationRepository notifRepo) {
        this.recoService = recoService;
        this.userRepo = userRepo;
        this.notifRepo = notifRepo;
    }

    @Override
    public int sendAdviceFromRecoIfNotExists(
            String username,
            RecommendationParamsDTO params,
            int top,
            int withinDays
    ) {
        var u = userRepo.getUserByUsername(username);
        if (u == null) return 0;

        List<RecommendationItemDTO> recs = recoService.recommendExercises(username, params);
        int created = 0;

        for (int i = 0; i < Math.min(top, recs.size()); i++) {
            var dto = recs.get(i);

            var n = new Notification();
            n.setUserId(u);
            n.setType("AI_ADVICE");
            n.setMessage("Hôm nay hãy thử: " + dto.getName()
                    + (dto.getEstimatedMinutes() != null ? " (" + dto.getEstimatedMinutes() + "’)" : "")
                    + (dto.getReason() != null ? " – " + dto.getReason() : ""));
            n.setIsRead(false);
            n.setCreatedAt(new Date());
            notifRepo.save(n);
            created++;
        }
        return created;
    }
}
