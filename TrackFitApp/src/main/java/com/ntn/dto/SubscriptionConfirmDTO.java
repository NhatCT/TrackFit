package com.ntn.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class SubscriptionConfirmDTO {

    @NotBlank(message = "Vui lòng chọn gói đăng ký")
    @Pattern(regexp = "monthly|yearly", message = "Gói phải là monthly hoặc yearly")
    private String planKey;

    /** Mã tham chiếu chuyển khoản (demo / đối soát sau này) */
    private String transferRef;

    public String getPlanKey() {
        return planKey;
    }

    public void setPlanKey(String planKey) {
        this.planKey = planKey;
    }

    public String getTransferRef() {
        return transferRef;
    }

    public void setTransferRef(String transferRef) {
        this.transferRef = transferRef;
    }
}
