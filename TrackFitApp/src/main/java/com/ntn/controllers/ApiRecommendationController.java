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

    @Autowired private RecommendationService recService;

    @GetMapping
    public ResponseEntity<?> recommend(
            Principal principal,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "kw", required = false) String kw,
            @RequestParam(value = "availableMinutes", required = false) Integer availableMinutes,
            @RequestParam(value = "intensity", required = false) String intensity,
            @RequestParam(value = "goalType", required = false) String goalType
    ) {
        var p = new com.ntn.dto.RecommendationParamsDTO();
        p.setSize(size);
        p.setKw(kw);
        p.setAvailableMinutes(availableMinutes);
        p.setIntensity(intensity);
        p.setGoalType(goalType);
        List<RecommendationItemDTO> res = recService.recommendExercises(principal.getName(), p);
        return ResponseEntity.ok(res);
    }
}

  
