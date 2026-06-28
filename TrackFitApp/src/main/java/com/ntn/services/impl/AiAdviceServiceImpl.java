package com.ntn.services.impl;

import com.ntn.dto.RecommendationItemDTO;
import com.ntn.dto.RecommendationParamsDTO;
import com.ntn.dto.NotificationCreateDTO;
import com.ntn.pojo.User;
import com.ntn.repositories.UserRepository;
import com.ntn.services.NotificationService;
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
    private final NotificationService notifService;

    public AiAdviceServiceImpl(RecommendationService recoService,
                               UserRepository userRepo,
                               NotificationService notifService) {
        this.recoService = recoService;
        this.userRepo = userRepo;
        this.notifService = notifService;
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

            var req = new NotificationCreateDTO();
            req.setMessage("Hôm nay hãy thử: " + dto.getName()
                    + (dto.getEstimatedMinutes() != null ? " (" + dto.getEstimatedMinutes() + "’)" : "")
                    + (dto.getReason() != null ? " – " + dto.getReason() : ""));
            req.setType("ADVICE");
            req.setSource("AI");
            req.setSender("AI Coach");
            notifService.createForUser(username, req);
            created++;
        }
        return created;
    }
}
