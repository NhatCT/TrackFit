package com.ntn.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public class ExerciseCreateUpdateDTO {

    @NotBlank(message = "{exercise.name.notBlank}")
    @Size(max = 100, message = "{exercise.name.size}")
    private String name;

    @Size(max = 50, message = "{exercise.targetGoal.size}")
    private String targetGoal;

    @Size(max = 50, message = "{exercise.muscleGroup.size}")
    private String muscleGroup;

    @Size(max = 255, message = "{exercise.videoUrl.size}")
    @URL(message = "{exercise.videoUrl.url}")
    private String videoUrl;

    @Size(max = 2000, message = "{exercise.description.size}")
    private String description;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTargetGoal() {
        return targetGoal;
    }

    public void setTargetGoal(String targetGoal) {
        this.targetGoal = targetGoal;
    }

    public String getMuscleGroup() {
        return muscleGroup;
    }

    public void setMuscleGroup(String muscleGroup) {
        this.muscleGroup = muscleGroup;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
 