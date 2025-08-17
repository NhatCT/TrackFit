package com.ntn.services.impl;

import com.ntn.dto.HistoryCreateUpdateDTO;
import com.ntn.dto.HistoryDTO;
import com.ntn.pojo.Exercises;
import com.ntn.pojo.User;
import com.ntn.pojo.UserWorkoutHistory;
import com.ntn.pojo.WorkoutPlan;
import com.ntn.repositories.ExercisesRepository;
import com.ntn.repositories.UserRepository;
import com.ntn.repositories.UserWorkoutHistoryRepository;
import com.ntn.repositories.WorkoutPlanRepository;
import com.ntn.services.UserWorkoutHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserWorkoutHistoryServiceImpl implements UserWorkoutHistoryService {

    @Autowired private UserRepository userRepo;
    @Autowired private ExercisesRepository exercisesRepo;
    @Autowired private WorkoutPlanRepository planRepo;
    @Autowired private UserWorkoutHistoryRepository repo;

    @Override
    public HistoryDTO create(String username, HistoryCreateUpdateDTO req) {
        User u = mustUser(username);
        Exercises ex = mustExercise(req.getExerciseId());
        WorkoutPlan plan = mustPlan(req.getPlanId(), u);

        UserWorkoutHistory h = new UserWorkoutHistory();
        h.setUserId(u);
        h.setExercisesId(ex);
        h.setPlanId(plan);
        h.setStatus(req.getStatus());
        h.setCompletedAt(req.getCompletedAt() != null ? req.getCompletedAt() : new Date());
        h = repo.save(h);
        return toDTO(h);
    }

    @Override
    public HistoryDTO get(String username, Integer id) {
        User u = mustUser(username);
        UserWorkoutHistory h = repo.findById(id);
        if (h == null || !h.getUserId().getUserId().equals(u.getUserId()))
            throw new IllegalArgumentException("Lịch sử không tồn tại hoặc không thuộc về bạn");
        return toDTO(h);
    }

    @Override
    public HistoryDTO update(String username, Integer id, HistoryCreateUpdateDTO req) {
        User u = mustUser(username);
        UserWorkoutHistory h = repo.findById(id);
        if (h == null || !h.getUserId().getUserId().equals(u.getUserId()))
            throw new IllegalArgumentException("Lịch sử không tồn tại hoặc không thuộc về bạn");

        if (req.getExerciseId() != null) h.setExercisesId(mustExercise(req.getExerciseId()));
        if (req.getPlanId() != null)     h.setPlanId(mustPlan(req.getPlanId(), u));
        if (req.getStatus() != null)     h.setStatus(req.getStatus());
        if (req.getCompletedAt() != null) h.setCompletedAt(req.getCompletedAt());

        h = repo.save(h);
        return toDTO(h);
    }

    @Override
    public void delete(String username, Integer id) {
        User u = mustUser(username);
        UserWorkoutHistory h = repo.findById(id);
        if (h == null || !h.getUserId().getUserId().equals(u.getUserId()))
            throw new IllegalArgumentException("Lịch sử không tồn tại hoặc không thuộc về bạn");
        repo.delete(h);
    }

    @Override
    public Map<String, Object> listByUserPaged(String username, Integer page, Integer pageSize,
                                               Integer planId, Integer exerciseId, String status) {
        User u = mustUser(username);

        if (pageSize == null || pageSize <= 0) {
            var items = repo.findByUserIdFiltered(u.getUserId(), planId, exerciseId, status)
                    .stream().map(this::toDTO).collect(Collectors.toList());
            long total = items.size();
            return Map.of("page", 1, "pageSize", total, "totalPages", 1, "totalElements", total, "items", items);
        }

        int p = (page == null || page < 1) ? 1 : page;
        long total = repo.countByUserId(u.getUserId(), planId, exerciseId, status);
        int totalPages = (int) Math.ceil(total * 1.0 / pageSize);
        if (p > totalPages && totalPages > 0) p = totalPages;

        var items = repo.findByUserIdPaged(u.getUserId(), planId, exerciseId, status, p, pageSize)
                .stream().map(this::toDTO).collect(Collectors.toList());

        return Map.of("page", p, "pageSize", pageSize, "totalPages", totalPages, "totalElements", total, "items", items);
    }

    // helpers
    private User mustUser(String username) {
        User u = userRepo.getUserByUsername(username);
        if (u == null) throw new IllegalArgumentException("Không tìm thấy người dùng");
        return u;
    }

    private Exercises mustExercise(Integer id) {
        var e = exercisesRepo.findById(id);
        if (e == null) throw new IllegalArgumentException("Không tìm thấy bài tập");
        return e;
    }

    private WorkoutPlan mustPlan(Integer id, User owner) {
        var p = planRepo.findById(id);
        if (p == null || !p.getUserId().getUserId().equals(owner.getUserId()))
            throw new IllegalArgumentException("Kế hoạch không tồn tại hoặc không thuộc về bạn");
        return p;
    }

    private HistoryDTO toDTO(UserWorkoutHistory h) {
        HistoryDTO d = new HistoryDTO();
        d.setHistoryId(h.getHistoryId());
        d.setExerciseId(h.getExercisesId().getExercisesId());
        d.setExerciseName(h.getExercisesId().getName());
        d.setPlanId(h.getPlanId().getPlanId());
        d.setPlanName(h.getPlanId().getPlanName());
        d.setStatus(h.getStatus());
        d.setCompletedAt(h.getCompletedAt());
        return d;
    }
}
