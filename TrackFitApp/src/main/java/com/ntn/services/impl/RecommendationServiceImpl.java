// src/main/java/com/ntn/services/impl/RecommendationServiceImpl.java
package com.ntn.services.impl;

import com.ntn.dto.*;
import com.ntn.pojo.Exercises;
import com.ntn.pojo.Goal;
import com.ntn.pojo.HealthData;
import com.ntn.pojo.User;
import com.ntn.repositories.*;
import com.ntn.services.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class RecommendationServiceImpl implements RecommendationService {

    @Autowired
    private UserRepository userRepo;
    @Autowired
    private GoalRepository goalRepo;
    @Autowired
    private ExercisesRepository exercisesRepo;
    @Autowired
    private UserWorkoutHistoryRepository historyRepo;
    @Autowired
    private HealthDataRepository healthRepo;

    @Value("${ai.reco.url:}")
    private String aiRecoUrl;

    @Autowired(required = false)
    private RestTemplate rest;

    @Override
    @Cacheable(
            value = "reco_exercises",
            key = "#username + '_' + (#params.size==null?8:#params.size) + '_' + "
            + "(#params.kw==null?'':#params.kw) + '_' + "
            + "(#params.availableMinutes==null?25:#params.availableMinutes) + '_' + "
            + "(#params.intensity==null?'':#params.intensity) + '_' + "
            + "(#params.goalType==null?'':#params.goalType)"
    )
    public List<RecommendationItemDTO> recommendExercises(String username, RecommendationParamsDTO params) {
        User u = mustGetUser(username);

        Goal latestGoal = goalRepo.findByUserId(u.getUserId())
                .stream().max(Comparator.comparing(Goal::getCreatedAt)).orElse(null);
        HealthData latestHealth = healthRepo.findByUserId(u.getUserId())
                .stream().max(Comparator.comparing(HealthData::getUpdatedAt)).orElse(null);

        String goalType = pick(params.getGoalType(), latestGoal != null ? latestGoal.getGoalType() : null, "general_fitness");
        String intensity = adaptIntensity(
                pick(params.getIntensity(), latestGoal != null ? latestGoal.getIntensity() : null, "Medium"),
                recentCompletionRate(u.getUserId(), 14)
        );
        int minutesPref = params.getAvailableMinutes() != null ? params.getAvailableMinutes() : 25;
        int size = params.getSize() != null && params.getSize() > 0 ? params.getSize() : 8;

        // === candidates từ DB ===
        Map<String, String> q = new HashMap<>();
        if (params.getKw() != null && !params.getKw().isBlank()) {
            q.put("kw", params.getKw().trim());
        }
        q.put("page", "1");
        q.put("pageSize", "200");

        List<Exercises> candidates = exercisesRepo.getExercises(q);
        if (candidates.isEmpty()) {
            return List.of();
        }

        // === gọi AI service ===
        Map<Integer, Double> scoreMap = new HashMap<>();
        Map<Integer, String> reasonMap = new HashMap<>();

        if (rest != null && aiRecoUrl != null && !aiRecoUrl.isBlank()) {
            try {
                AiRankRequest req = buildAiPayload(u, latestGoal, latestHealth, goalType, intensity, minutesPref, candidates);
                AiRankedExercise[] ranked = rest.postForObject(aiRecoUrl + "/rank", req, AiRankedExercise[].class);
                if (ranked != null) {
                    for (AiRankedExercise it : ranked) {
                        if (it.getExerciseId() != null) {
                            if (it.getScore() != null) {
                                scoreMap.put(it.getExerciseId(), it.getScore());
                            }
                            if (it.getReason() != null) {
                                reasonMap.put(it.getExerciseId(), it.getReason());
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                System.err.println("AI service error: " + ex.getMessage());
            }
        }

        // === hybrid sort: score -> goal match -> createdAt desc ===
        candidates.sort(
                Comparator.comparing((Exercises e) -> scoreMap.getOrDefault(e.getExercisesId(), 0.0)).reversed()
                        .thenComparing(e -> e.getTargetGoal() != null && e.getTargetGoal().equalsIgnoreCase(goalType) ? 0 : 1)
                        .thenComparing(e -> e.getCreatedAt() != null ? -e.getCreatedAt().getTime() : 0L)
        );

        // === map về DTO FE cần ===
        return candidates.stream().limit(size).map(e -> {
            RecommendationItemDTO dto = new RecommendationItemDTO();
            dto.setExercisesId(e.getExercisesId());
            dto.setName(e.getName());
            dto.setDescription(
                    (e.getDescription() != null && !e.getDescription().isBlank())
                    ? e.getDescription()
                    : "Phù hợp với mục tiêu " + goalType
            );
            dto.setMuscleGroup(e.getMuscleGroup());
            dto.setVideoUrl(e.getVideoUrl());
            dto.setCreatedAt(e.getCreatedAt());

            // AI data
            Double score = scoreMap.get(e.getExercisesId());
            dto.setScore(score);
            dto.setReason(reasonMap.getOrDefault(
                    e.getExercisesId(),
                    "Phù hợp với mục tiêu " + goalType + " và cường độ " + intensity
            ));

            // estimatedMinutes: có thể parse từ targetGoal hoặc mặc định
            Integer estMinutes = parseMinutesFromTargetGoal(e.getTargetGoal());
            dto.setEstimatedMinutes(estMinutes);

            // targetGoal = "15’ • AI 0.92"
            List<String> tg = new ArrayList<>();
            if (estMinutes != null) {
                tg.add(estMinutes + "’");
            }
            if (score != null) {
                tg.add("AI " + String.format(Locale.US, "%.2f", score));
            }
            dto.setTargetGoal(tg.isEmpty() ? e.getTargetGoal() : String.join(" • ", tg));

            return dto;
        }).collect(Collectors.toList());
    }

    // ================== helpers ==================
    private User mustGetUser(String username) {
        User u = userRepo.getUserByUsername(username);
        if (u == null) {
            throw new IllegalArgumentException("Không tìm thấy user");
        }
        return u;
    }

    private String pick(String... opts) {
        for (String s : opts) {
            if (s != null && !s.isBlank()) {
                return s;
            }
        }
        return null;
    }

    private String adaptIntensity(String cur, double completionRate) {
        List<String> scale = List.of("Low", "Medium", "High");
        int idx = scale.indexOf(cur);
        if (idx < 0) {
            idx = 1;
        }
        if (completionRate < 0.5 && idx > 0) {
            idx--;
        }
        if (completionRate > 0.85 && idx < 2) {
            idx++;
        }
        return scale.get(idx);
    }

    private double recentCompletionRate(Integer userId, int days) {
        Date to = new Date();
        Date from = new Date(System.currentTimeMillis() - days * 24L * 3600_000L);
        long done = historyRepo.countCompletedBetween(userId, from, to);
        long total = historyRepo.countByUserId(userId, null, null, null);
        return total == 0 ? 1.0 : Math.max(0.0, Math.min(1.0, (double) done / total));
    }

    private AiRankRequest buildAiPayload(User u, Goal g, HealthData h,
            String goalType, String intensity,
            int minutes, List<Exercises> cand) {
        AiRankRequest req = new AiRankRequest();

        AiRankRequest.UserInfo ui = new AiRankRequest.UserInfo();
        ui.id = u.getUserId();
        ui.goalType = goalType;
        ui.gender = u.getGender();
        req.setUser(ui);

        Map<String, Object> ctx = new HashMap<>();
        ctx.put("availableMinutes", minutes);
        ctx.put("intensity", intensity);
        if (h != null) {
            ctx.put("height", h.getHeight());
            ctx.put("weight", h.getWeight());
        }
        req.setContext(ctx);

        req.setCandidates(cand.stream().map(e -> {
            AiRankRequest.Candidate c = new AiRankRequest.Candidate();
            c.exerciseId = e.getExercisesId();
            c.name = e.getName();
            c.muscleGroup = e.getMuscleGroup();
            c.minutes = parseMinutesFromTargetGoal(e.getTargetGoal());
            c.difficulty = "Medium";
            return c;
        }).collect(Collectors.toList()));

        return req;
    }

    private Integer parseMinutesFromTargetGoal(String targetGoal) {
        if (targetGoal == null) {
            return null;
        }
        var matcher = java.util.regex.Pattern.compile("(\\d{1,3})[’']?").matcher(targetGoal);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException ignore) {
            }
        }
        return null;
    }
}
 