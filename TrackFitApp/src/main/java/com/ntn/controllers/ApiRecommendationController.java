package com.ntn.controllers;

import com.ntn.dto.RecommendationItemDTO;
import com.ntn.dto.RecommendationParamsDTO;
import com.ntn.services.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/secure/recommendations")
@CrossOrigin
public class ApiRecommendationController {

    @Autowired private RecommendationService recommendationService;

    @GetMapping("/auto")
    public ResponseEntity<?> auto(@RequestParam(value = "size", required = false) Integer size, Principal principal) {
        RecommendationParamsDTO params = new RecommendationParamsDTO();
        params.setSize(size);
        List<RecommendationItemDTO> data = recommendationService.recommendExercises(principal.getName(), params);
        return ResponseEntity.ok(data);
    }

    @GetMapping
    public ResponseEntity<?> list(
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "kw", required = false) String kw,
            @RequestParam(value = "availableMinutes", required = false) Integer availableMinutes,
            @RequestParam(value = "intensity", required = false) String intensity,
            @RequestParam(value = "goalType", required = false) String goalType,
            Principal principal
    ) {
        RecommendationParamsDTO params = new RecommendationParamsDTO();
        params.setSize(size);
        params.setKw(kw);
        params.setAvailableMinutes(availableMinutes);
        params.setIntensity(intensity);
        params.setGoalType(goalType);

        List<RecommendationItemDTO> data = recommendationService.recommendExercises(principal.getName(), params);
        return ResponseEntity.ok(data);
    }

}
