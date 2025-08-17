
package com.ntn.dto;

import jakarta.validation.constraints.*;
import java.util.Date;

public class HistoryCreateUpdateDTO {

    @NotNull
    private Integer exerciseId;

    @NotNull
    private Integer planId;

    @NotBlank
    @Size(max = 9)
    @Pattern(regexp = "^(COMPLETED|SKIPPED|MISSED|ONGOING)$",
            message = "status phải là COMPLETED, SKIPPED, MISSED hoặc ONGOING")
    private String status;

    private Date completedAt;

    public Integer getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(Integer exerciseId) {
        this.exerciseId = exerciseId;
    }

    public Integer getPlanId() {
        return planId;
    }

    public void setPlanId(Integer planId) {
        this.planId = planId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Date completedAt) {
        this.completedAt = completedAt;
    }
}
