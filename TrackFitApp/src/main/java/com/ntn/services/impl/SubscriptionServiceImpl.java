package com.ntn.services.impl;

import com.ntn.dto.SubscriptionConfirmDTO;
import com.ntn.dto.SubscriptionStatusDTO;
import com.ntn.dto.UserResponseDTO;
import com.ntn.pojo.User;
import com.ntn.repositories.UserRepository;
import com.ntn.services.ChatQuotaService;
import com.ntn.services.PremiumService;
import com.ntn.services.SubscriptionService;
import com.ntn.services.UserService;
import com.ntn.services.WebSocketEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@Transactional
public class SubscriptionServiceImpl implements SubscriptionService {

    @Autowired
    private UserRepository userRepo;
    @Autowired
    private UserService userService;
    @Autowired
    private PremiumService premiumService;
    @Autowired
    private ChatQuotaService chatQuotaService;
    @Autowired
    private WebSocketEventService webSocketEventService;

    @Override
    public SubscriptionStatusDTO getStatus(String username) {
        User u = mustUser(username);
        boolean premium = premiumService.isPremiumActive(u);
        SubscriptionStatusDTO dto = new SubscriptionStatusDTO();
        dto.setPremium(premium);
        dto.setPremiumExpiresAt(u.getPremiumExpiresAt());
        dto.setChatDailyLimit(chatQuotaService.getDailyLimit());
        dto.setChatRemaining(chatQuotaService.getRemaining(u.getUserId(), premium));
        return dto;
    }

    @Override
    public UserResponseDTO confirmPayment(String username, SubscriptionConfirmDTO body) {
        User u = mustUser(username);
        int days = "yearly".equalsIgnoreCase(body.getPlanKey()) ? 365 : 30;

        LocalDateTime base = u.getPremiumExpiresAt();
        if (base != null && base.isAfter(LocalDateTime.now())) {
            base = base.plusDays(days);
        } else {
            base = LocalDateTime.now().plusDays(days);
        }

        u.setIsPremium(true);
        u.setPremiumExpiresAt(base);
        u.setUpdatedAt(LocalDateTime.now());
        userRepo.updateUser(u);

        webSocketEventService.publishSubscriptionActivated(username, Map.of(
                "planKey", body.getPlanKey(),
                "premiumExpiresAt", base.toString(),
                "transferRef", body.getTransferRef() == null ? "" : body.getTransferRef()
        ));

        return userService.getUserByUsername(username);
    }

    private User mustUser(String username) {
        User u = userRepo.getUserByUsername(username);
        if (u == null) {
            throw new IllegalArgumentException("Không tìm thấy người dùng");
        }
        return u;
    }
}
