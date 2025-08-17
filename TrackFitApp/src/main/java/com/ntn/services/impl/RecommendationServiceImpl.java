package com.ntn.services.impl;

import com.ntn.dto.RecommendationItemDTO;
import com.ntn.dto.RecommendationParamsDTO;
import com.ntn.dto.AiRankRequest;
import com.ntn.dto.AiRankedExercise;
import com.ntn.pojo.Exercises;
import com.ntn.pojo.Goal;
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

    @Autowired private UserRepository userRepo;
    @Autowired private GoalRepository goalRepo;
    @Autowired private ExercisesRepository exercisesRepo;
    @Autowired private UserWorkoutHistoryRepository historyRepo;

    @Value("${ai.reco.url:}")
    private String aiRecoUrl;
    @Autowired(required = false)
    private RestTemplate rest;

    @Override
    @Cacheable(value = "reco_exercises",
            key = "#username + '_' + (#params.size==null?10:#params.size) + '_' + (#params.kw==null?'':#params.kw) + '_' + (#params.availableMinutes==null?25:#params.availableMinutes) + '_' + (#params.intensity==null?'':#params.intensity) + '_' + (#params.goalType==null?'':#params.goalType)")
    public List<RecommendationItemDTO> recommendExercises(String username, RecommendationParamsDTO params) {
        User u = userRepo.getUserByUsername(username);
        if (u == null) throw new IllegalArgumentException("Không tìm thấy người dùng");

        Goal latest = goalRepo.findByUserId(u.getUserId()).stream()
                .max(Comparator.comparing(Goal::getCreatedAt)).orElse(null);

        String goalType = pick(params.getGoalType(), latest != null ? latest.getGoalType() : null, "lose_weight");
        String intensity = normalizeIntensity(pick(params.getIntensity(), latest != null ? latest.getIntensity() : null, "Medium"));
        int minutes = params.getAvailableMinutes() != null ? params.getAvailableMinutes() : 25;
        int size = params.getSize() != null ? Math.max(params.getSize(), 1) : 10;

        double completion = recentCompletionRate(u.getUserId(), 14);
        intensity = adaptIntensity(intensity, completion);

        Map<String,String> q = new HashMap<>();
        if (params.getKw() != null && !params.getKw().isBlank()) q.put("kw", params.getKw());
        List<Exercises> candidates = exercisesRepo.getExercises(q);

        candidates.sort(Comparator
                .comparing((Exercises e) -> e.getTargetGoal()!=null && e.getTargetGoal().equalsIgnoreCase(goalType) ? 0 : 1)
                .thenComparing(e -> e.getCreatedAt()==null ? 0L : -e.getCreatedAt().getTime()));

        Set<Integer> recent = recentlyDoneExerciseIds(u.getUserId(), 7);
        List<Exercises> diversified = new ArrayList<>();
        List<Exercises> tail = new ArrayList<>();
        for (Exercises e: candidates) {
            if (!recent.contains(e.getExercisesId())) diversified.add(e);
            else tail.add(e);
        }
        diversified.addAll(tail);

        List<Exercises> ranked = diversified.stream().limit(40).collect(Collectors.toList());
        Map<Integer,String> reason = new HashMap<>();
        if (rest != null && aiRecoUrl != null && !aiRecoUrl.isBlank() && !ranked.isEmpty()) {
            try {
                AiRankRequest req = buildAiPayload(u, goalType, intensity, minutes, ranked);
                AiRankedExercise[] r = rest.postForObject(aiRecoUrl + "/rank", req, AiRankedExercise[].class);
                if (r != null) {
                    Map<Integer, Double> score = new HashMap<>();
                    for (AiRankedExercise it: r) {
                        score.put(it.getExerciseId(), it.getScore());
                        if (it.getReason()!=null) reason.put(it.getExerciseId(), it.getReason());
                    }
                    ranked.sort(Comparator.comparing((Exercises e) -> score.getOrDefault(e.getExercisesId(), 0.0)).reversed());
                }
            } catch (Exception ignore) {}
        }

        return ranked.stream().limit(size).map(e -> {
            RecommendationItemDTO d = new RecommendationItemDTO();
            d.setExerciseId(e.getExercisesId());
            d.setName(e.getName());
            d.setMuscleGroup(e.getMuscleGroup());
            d.setReason(reason.get(e.getExercisesId()));
            return d;
        }).collect(Collectors.toList());
    }

    private String pick(String... opts) { for (String s: opts) if (s!=null && !s.isBlank()) return s; return null; }
    private String normalizeIntensity(String x) {
        if (x==null) return "Medium";
        String s = x.trim().toLowerCase();
        if (s.startsWith("l")) return "Low";
        if (s.startsWith("h")) return "High";
        return "Medium";
    }
    private String adaptIntensity(String cur, double completionRate) {
        List<String> scale = List.of("Low","Medium","High");
        int idx = scale.indexOf(cur); if (idx < 0) idx = 1;
        if (completionRate < 0.5 && idx > 0) idx--;
        if (completionRate > 0.85 && idx < 2) idx++;
        return scale.get(idx);
    }
    private double recentCompletionRate(Integer userId, int days) {
        var to = new Date();
        var from = new Date(System.currentTimeMillis()-days*24L*3600_000L);
        long done = historyRepo.countCompletedBetween(userId, from, to);
        long total = historyRepo.countByUserId(userId, null, null, null);
        if (total == 0) return 1.0;
        double rate = (double) done / Math.max(1, total);
        return Math.max(0.0, Math.min(1.0, rate));
    }
    private Set<Integer> recentlyDoneExerciseIds(Integer userId, int days) {
        var to = new Date();
        var from = new Date(System.currentTimeMillis()-days*24L*3600_000L);
        return historyRepo.findBetween(userId, from, to, "COMPLETED").stream()
                .map(h -> h.getExercisesId().getExercisesId())
                .collect(java.util.stream.Collectors.toSet());
    }
    private AiRankRequest buildAiPayload(com.ntn.pojo.User u, String goalType, String intensity, int minutes, List<Exercises> cand) {
        AiRankRequest req = new AiRankRequest();
        var user = new AiRankRequest.UserInfo();
        user.id = u.getUserId();
        user.goalType = goalType;
        user.gender = u.getGender();
        req.setUser(user);
        req.setContext(Map.of("availableMinutes", minutes, "intensity", intensity));
        req.setCandidates(cand.stream().map(e -> {
            var c = new AiRankRequest.Candidate();
            c.exerciseId = e.getExercisesId();
            c.name = e.getName();
            c.muscleGroup = e.getMuscleGroup();
            c.minutes = null;     // có metadata thì set
            c.difficulty = null;  // có metadata thì set
            return c;
        }).collect(Collectors.toList()));
        return req;
    }
}
