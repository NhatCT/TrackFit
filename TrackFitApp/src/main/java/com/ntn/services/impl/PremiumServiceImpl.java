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
        return true;
    }

    @Override
    public boolean isPremiumActive(String username) {
        return true;
    }

    @Override
    public void requirePremium(String username) {
        // No-op: everyone has premium access
    }
}
