package com.ntn.dto;

import jakarta.validation.constraints.NotBlank;

public class GoogleLoginDTO {
    @NotBlank(message = "Token Google không được để trống")
    private String credential;

    public String getCredential() {
        return credential;
    }

    public void setCredential(String credential) {
        this.credential = credential;
    }
}
