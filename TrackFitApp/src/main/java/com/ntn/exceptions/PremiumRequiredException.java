package com.ntn.exceptions;

public class PremiumRequiredException extends RuntimeException {
    public PremiumRequiredException() {
        super("Tính năng này chỉ dành cho hội viên GUTIM PRO.");
    }

    public PremiumRequiredException(String message) {
        super(message);
    }
}
