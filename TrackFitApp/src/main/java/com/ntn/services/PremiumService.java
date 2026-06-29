package com.ntn.services;

import com.ntn.pojo.User;

public interface PremiumService {

    boolean isPremiumActive(User user);

    boolean isPremiumActive(String username);

    void requirePremium(String username);
}
