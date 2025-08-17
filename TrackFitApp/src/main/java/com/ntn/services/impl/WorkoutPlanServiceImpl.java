package com.ntn.services.impl;

import com.ntn.dto.*;
import com.ntn.pojo.Exercises;
import com.ntn.pojo.Goal;
import com.ntn.pojo.PlanDetail;
import com.ntn.pojo.User;
import com.ntn.pojo.WorkoutPlan;
import com.ntn.repositories.*;
import com.ntn.services.WorkoutPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class WorkoutPlanServiceImpl implements WorkoutPlanService {

    @Autowired private UserRepository userRepo;
    @Autowired private GoalRepository goalRepo;
    @Autowired private ExercisesRepository exercisesRepo;
    @Autowired private WorkoutPlanRepository planRepo;
    @Autowired private PlanDetailRepository detailRepo;

    @Override
    public WorkoutPlanResponseDTO createPlan(String username, WorkoutPlanCreateRequest req) {
        User user = userRepo.getUserByUsername(username);
        if (user == null) throw new IllegalArgumentException("Không tìm thấy người dùng");

        WorkoutPlan plan = new WorkoutPlan();
        plan.setUserId(user);
        plan.setPlanName(req.getPlanName());
        plan.setIsTemplate(req.getIsTemplate() != null ? req.getIsTemplate() : Boolean.FALSE);
        plan.setCreatedAt(new Date());

        if (req.getGoalId() != null) {
            Goal g = goalRepo.findById(req.getGoalId());
            if (g == null || !g.getUserId().getUserId().equals(user.getUserId()))
                throw new IllegalArgumentException("Mục tiêu không tồn tại hoặc không thuộc về bạn");
            plan.setGoalId(g);
        }

        plan = planRepo.save(plan);

        if (req.getDetails() != null) {
            for (PlanDetailItemDTO d : req.getDetails()) {
                addDetailEntity(plan, d);
            }
        }

        plan = planRepo.findById(plan.getPlanId());
        return toPlanDTO(plan);
    }

    @Override
    public WorkoutPlanResponseDTO getPlan(Integer planId) {
        WorkoutPlan p = planRepo.findById(planId);
        if (p == null) throw new IllegalArgumentException("Không tìm thấy kế hoạch");
        return toPlanDTO(p);
    }
    @Override
    public Map<String, Object> listPlansByUserPaged(String username, Integer page, Integer pageSize, String kw) {
        User user = mustGetUser(username);
        List<WorkoutPlanListItemDTO> all = mapToListItems(planRepo.findByUserId(user.getUserId()));

        if (kw != null && !kw.isBlank()) {
            String k = kw.toLowerCase();
            all = all.stream()
                    .filter(x -> x.getPlanName() != null && x.getPlanName().toLowerCase().contains(k))
                    .collect(Collectors.toList());
        }

        if (page == null || pageSize == null) {
            return Map.of(
                "page", null,
                "pageSize", null,
                "totalPages", 1,
                "totalElements", all.size(),
                "items", all
            );
        }

        int p  = Math.max(page, 1);
        int ps = Math.max(pageSize, 1);
        int total = all.size();
        int totalPages = (int) Math.ceil(total * 1.0 / ps);
        int start = (p - 1) * ps;
        int end = Math.min(start + ps, total);
        List<WorkoutPlanListItemDTO> items = (start >= total) ? List.of() : all.subList(start, end);

        return Map.of(
            "page", p,
            "pageSize", ps,
            "totalPages", totalPages,
            "totalElements", total,
            "items", items
        );
    }

    @Override
    public void deletePlan(String username, Integer planId) {
        User user = mustGetUser(username);
        WorkoutPlan p = planRepo.findById(planId);
        if (p == null || !p.getUserId().getUserId().equals(user.getUserId()))
            throw new IllegalArgumentException("Kế hoạch không tồn tại hoặc không thuộc về bạn");
        planRepo.delete(p);
    }

    @Override
    public PlanDetailViewDTO addDetail(Integer planId, PlanDetailItemDTO dto) {
        WorkoutPlan p = planRepo.findById(planId);
        if (p == null) throw new IllegalArgumentException("Không tìm thấy kế hoạch");
        PlanDetail saved = addDetailEntity(p, dto);
        return toDetailDTO(saved);
    }

    @Override
    public PlanDetailViewDTO updateDetail(Integer detailId, PlanDetailItemDTO dto) {
        PlanDetail d = detailRepo.findById(detailId);
        if (d == null) throw new IllegalArgumentException("Không tìm thấy dòng chi tiết");

        if (dto.getExerciseId() != null) {
            Exercises ex = exercisesRepo.findById(dto.getExerciseId());
            if (ex == null) throw new IllegalArgumentException("Không tìm thấy bài tập");
            d.setExercisesId(ex);
        }
        if (dto.getDayOfWeek() != null) d.setDayOfWeek(dto.getDayOfWeek());
        if (dto.getDuration() != null) d.setDuration(dto.getDuration());

        d = detailRepo.save(d);
        return toDetailDTO(d);
    }

    @Override
    public void deleteDetail(Integer detailId) {
        PlanDetail d = detailRepo.findById(detailId);
        if (d == null) throw new IllegalArgumentException("Không tìm thấy dòng chi tiết");
        detailRepo.delete(d);
    }

    // ===== helpers =====
    private User mustGetUser(String username) {
        User u = userRepo.getUserByUsername(username);
        if (u == null) throw new IllegalArgumentException("Không tìm thấy người dùng");
        return u;
    }

    private List<WorkoutPlanListItemDTO> mapToListItems(List<WorkoutPlan> plans) {
        return plans.stream()
                .map(w -> new WorkoutPlanListItemDTO(
                        w.getPlanId(),
                        w.getPlanName(),
                        w.getIsTemplate(),
                        w.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    private PlanDetail addDetailEntity(WorkoutPlan plan, PlanDetailItemDTO dto) {
        Exercises ex = exercisesRepo.findById(dto.getExerciseId());
        if (ex == null) throw new IllegalArgumentException("Không tìm thấy bài tập");
        PlanDetail d = new PlanDetail();
        d.setPlanId(plan);
        d.setExercisesId(ex);
        d.setDayOfWeek(dto.getDayOfWeek());
        d.setDuration(dto.getDuration());
        return detailRepo.save(d);
    }

    private WorkoutPlanResponseDTO toPlanDTO(WorkoutPlan p) {
        WorkoutPlanResponseDTO dto = new WorkoutPlanResponseDTO();
        dto.setPlanId(p.getPlanId());
        dto.setPlanName(p.getPlanName());
        dto.setIsTemplate(p.getIsTemplate());
        dto.setCreatedAt(p.getCreatedAt());
        dto.setGoalId(p.getGoalId() != null ? p.getGoalId().getGoalId() : null);
        dto.setUserId(p.getUserId() != null ? p.getUserId().getUserId() : null);

        if (p.getPlanDetailSet() != null) {
            List<PlanDetailViewDTO> details = p.getPlanDetailSet().stream()
                    .map(this::toDetailDTO)
                    .sorted(Comparator.comparing(PlanDetailViewDTO::getDayOfWeek)
                                      .thenComparing(PlanDetailViewDTO::getDetailId))
                    .collect(Collectors.toList());
            dto.setDetails(details);
        }
        return dto;
    }

    private PlanDetailViewDTO toDetailDTO(PlanDetail d) {
        PlanDetailViewDTO v = new PlanDetailViewDTO();
        v.setDetailId(d.getDetailId());
        v.setExerciseId(d.getExercisesId().getExercisesId());
        v.setExerciseName(d.getExercisesId().getName());
        v.setDayOfWeek(d.getDayOfWeek());
        v.setDuration(d.getDuration());
        return v;
    }
}
