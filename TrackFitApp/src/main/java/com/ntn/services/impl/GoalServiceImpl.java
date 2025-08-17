package com.ntn.services.impl;

import com.ntn.dto.GoalDTO;
import com.ntn.pojo.Goal;
import com.ntn.pojo.User;
import com.ntn.repositories.GoalRepository;
import com.ntn.repositories.UserRepository;
import com.ntn.services.GoalService;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class GoalServiceImpl implements GoalService {

    @Autowired private UserRepository userRepo;
    @Autowired private GoalRepository goalRepo;

    @Override
    public void create(String username, GoalDTO dto) {
        User user = mustGetUser(username);
        Goal g = new Goal();
        g.setUserId(user);
        g.setGoalType(dto.getGoalType());
        g.setWorkoutDuration(dto.getWorkoutDuration());
        g.setIntensity(dto.getIntensity());
        g.setCreatedAt(new Date());
        goalRepo.saveGoal(g);
    }

    @Override
    public List<GoalDTO> listByUsername(String username) {
        User user = mustGetUser(username);
        return goalRepo.findByUserId(user.getUserId()).stream().map(g -> {
            GoalDTO d = new GoalDTO();
            d.setGoalType(g.getGoalType());
            d.setWorkoutDuration(g.getWorkoutDuration());
            d.setIntensity(g.getIntensity());
            return d;
        }).collect(Collectors.toList());
    }

    @Override
    public void update(String username, Integer goalId, GoalDTO dto) {
        User user = mustGetUser(username);
        Goal g = mustGetOwnedGoal(user, goalId);

        if (dto.getGoalType() != null) g.setGoalType(dto.getGoalType());
        if (dto.getWorkoutDuration() != null) g.setWorkoutDuration(dto.getWorkoutDuration());
        if (dto.getIntensity() != null) g.setIntensity(dto.getIntensity());
        goalRepo.saveGoal(g);
    }

    @Override
    public void delete(String username, Integer goalId) {
        User user = mustGetUser(username);
        Goal g = mustGetOwnedGoal(user, goalId);
        goalRepo.deleteGoal(g);
    }

    private User mustGetUser(String username) {
        User u = userRepo.getUserByUsername(username);
        if (u == null) throw new IllegalArgumentException("Không tìm thấy người dùng");
        return u;
    }

    private Goal mustGetOwnedGoal(User u, Integer id) {
        Goal g = goalRepo.findById(id);
        if (g == null || !g.getUserId().getUserId().equals(u.getUserId()))
            throw new IllegalArgumentException("Mục tiêu không tồn tại hoặc không thuộc về bạn");
        return g;
    }

}
