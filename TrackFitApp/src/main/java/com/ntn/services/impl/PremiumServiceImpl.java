package com.ntn.services.impl;

import com.ntn.exceptions.PremiumRequiredException;
import com.ntn.pojo.User;
import com.ntn.repositories.UserRepository;
import com.ntn.services.PremiumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PremiumServiceImpl implements PremiumService {

    @Autowired
    private UserRepository userRepo;

    @Override
    public boolean isPremiumActive(User user) {
        if (user == null || user.getIsPremium() == null || !user.getIsPremium()) {
            return false;
        }
        LocalDateTime expires = user.getPremiumExpiresAt();
        if (expires == null) {
            return true;
        }
        return expires.isAfter(LocalDateTime.now());
    }

    @Override
    public boolean isPremiumActive(String username) {
        User u = userRepo.getUserByUsername(username);
        return isPremiumActive(u);
    }

    @Override
    public void requirePremium(String username) {
        if (!isPremiumActive(username)) {
            throw new PremiumRequiredException();
        }
    }
}
