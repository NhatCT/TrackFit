package com.ntn.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class CreateOrderDTO {

    @NotBlank(message = "Vui lòng chọn gói đăng ký")
    @Pattern(regexp = "monthly|yearly", message = "Gói phải là monthly hoặc yearly")
    private String planKey;

    public String getPlanKey() {
        return planKey;
    }

    public void setPlanKey(String planKey) {
        this.planKey = planKey;
    }
}
