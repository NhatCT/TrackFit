package com.ntn.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class SubscriptionStatusDTO {

    private boolean premium;
    private boolean isPremium;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime premiumExpiresAt;

    private int chatDailyLimit;
    private int chatRemaining;

    public boolean isPremium() {
        return premium;
    }

    public void setPremium(boolean premium) {
        this.premium = premium;
        this.isPremium = premium;
    }

    public boolean getIsPremium() {
        return isPremium;
    }

    public void setIsPremium(boolean isPremium) {
        this.isPremium = isPremium;
        this.premium = isPremium;
    }

    public LocalDateTime getPremiumExpiresAt() {
        return premiumExpiresAt;
    }

    public void setPremiumExpiresAt(LocalDateTime premiumExpiresAt) {
        this.premiumExpiresAt = premiumExpiresAt;
    }

    public int getChatDailyLimit() {
        return chatDailyLimit;
    }

    public void setChatDailyLimit(int chatDailyLimit) {
        this.chatDailyLimit = chatDailyLimit;
    }

    public int getChatRemaining() {
        return chatRemaining;
    }

    public void setChatRemaining(int chatRemaining) {
        this.chatRemaining = chatRemaining;
    }
}
