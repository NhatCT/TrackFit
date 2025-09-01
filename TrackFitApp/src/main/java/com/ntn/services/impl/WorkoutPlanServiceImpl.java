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

    @Autowired
    private UserRepository userRepo;
    @Autowired
    private GoalRepository goalRepo;
    @Autowired
    private ExercisesRepository exercisesRepo;
    @Autowired
    private WorkoutPlanRepository planRepo;
    @Autowired
    private PlanDetailRepository detailRepo;

    @Override
    public WorkoutPlanResponseDTO createPlan(String username, WorkoutPlanCreateRequest req) {
        User owner;

        if (req.getUserId() != null) {
            // Nếu có userId trong request → giả định đây là admin tạo hộ
            owner = userRepo.findById(req.getUserId());
            if (owner == null) {
                throw new IllegalArgumentException("UserId không hợp lệ");
            }
        } else {
            // Người dùng tự tạo → lấy từ principal
            owner = userRepo.getUserByUsername(username);
            if (owner == null) {
                throw new IllegalArgumentException("Không tìm thấy người dùng");
            }
        }

        WorkoutPlan plan = new WorkoutPlan();
        plan.setUserId(owner);
        plan.setPlanName(req.getPlanName());
        plan.setIsTemplate(Boolean.TRUE.equals(req.getIsTemplate())); // mặc định false
        plan.setCreatedAt(new Date());

        if (req.getGoalId() != null) {
            Goal g = goalRepo.findById(req.getGoalId());
            if (g == null || !g.getUserId().getUserId().equals(owner.getUserId())) {
                throw new IllegalArgumentException("Mục tiêu không tồn tại hoặc không thuộc về user này");
            }
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
        if (p == null) {
            throw new IllegalArgumentException("Không tìm thấy kế hoạch");
        }
        return toPlanDTO(p);
    }

    @Override
    public Map<String, Object> listPlansByUserPaged(String username, Integer page, Integer pageSize, String kw) {
        User user = mustGetUser(username);

        Map<String, String> params = new HashMap<>();
        if (kw != null && !kw.isBlank()) {
            params.put("kw", kw.trim());
        }
        if (page != null) {
            params.put("page", String.valueOf(page));
        }
        if (pageSize != null) {
            params.put("pageSize", String.valueOf(pageSize));
        }

        List<WorkoutPlan> plans = planRepo.getPlansByUser(user.getUserId(), params);
        long total = planRepo.countPlansByUser(user.getUserId(), params);

        int p = (page == null ? 1 : Math.max(page, 1));
        int ps = (pageSize == null ? 10 : Math.max(pageSize, 1));
        int totalPages = (int) Math.ceil(total * 1.0 / ps);

        return Map.of(
                "page", p,
                "pageSize", ps,
                "totalPages", totalPages,
                "totalElements", total,
                "items", mapToListItems(plans)
        );
    }

    @Override
    public void deletePlan(String username, Integer planId) {
        User user = mustGetUser(username);
        WorkoutPlan p = planRepo.findById(planId);
        if (p == null || !p.getUserId().getUserId().equals(user.getUserId())) {
            throw new IllegalArgumentException("Kế hoạch không tồn tại hoặc không thuộc về bạn");
        }
        planRepo.delete(p);
    }

    @Override
    public PlanDetailViewDTO addDetail(Integer planId, PlanDetailItemDTO dto) {
        WorkoutPlan p = planRepo.findById(planId);
        if (p == null) {
            throw new IllegalArgumentException("Không tìm thấy kế hoạch");
        }
        PlanDetail saved = addDetailEntity(p, dto);
        return toDetailDTO(saved);
    }

    @Override
    public PlanDetailViewDTO updateDetail(Integer detailId, PlanDetailItemDTO dto) {
        PlanDetail d = detailRepo.findById(detailId);
        if (d == null) {
            throw new IllegalArgumentException("Không tìm thấy dòng chi tiết");
        }

        if (dto.getExerciseId() != null) {
            Exercises ex = exercisesRepo.findById(dto.getExerciseId());
            if (ex == null) {
                throw new IllegalArgumentException("Không tìm thấy bài tập");
            }
            d.setExercisesId(ex);
        }
        if (dto.getDayOfWeek() != null) {
            d.setDayOfWeek(dto.getDayOfWeek());
        }
        if (dto.getDuration() != null) {
            d.setDuration(dto.getDuration());
        }

        d = detailRepo.save(d);
        return toDetailDTO(d);
    }

    @Override
    public void deleteDetail(Integer detailId) {
        PlanDetail d = detailRepo.findById(detailId);
        if (d == null) {
            throw new IllegalArgumentException("Không tìm thấy dòng chi tiết");
        }
        detailRepo.delete(d);
    }

    private User mustGetUser(String username) {
        User u = userRepo.getUserByUsername(username);
        if (u == null) {
            throw new IllegalArgumentException("Không tìm thấy người dùng");
        }
        return u;
    }

    // Ghép tên hiển thị: firstName + lastName (fallback username)
    private String displayUserName(User u) {
        if (u == null) {
            return null;
        }
        String fn = u.getFirstName() == null ? "" : u.getFirstName().trim();
        String ln = u.getLastName() == null ? "" : u.getLastName().trim();
        String full = (fn + " " + ln).trim();
        return full.isEmpty() ? u.getUsername() : full;
    }

    // Chuỗi hiển thị cho Goal (thân thiện)
    private String displayGoal(Goal g) {
        if (g == null) {
            return null;
        }
        String type = g.getGoalType() != null ? g.getGoalType().trim() : "";
        String dur = g.getWorkoutDuration() != null ? (g.getWorkoutDuration() + "′") : "";
        String inten = g.getIntensity() != null ? g.getIntensity().trim() : "";
        // ghép với " · ", bỏ phần rỗng
        List<String> parts = new ArrayList<>();
        if (!type.isEmpty()) {
            parts.add(type);
        }
        if (!dur.isEmpty()) {
            parts.add(dur);
        }
        if (!inten.isEmpty()) {
            parts.add(inten);
        }
        String joined = String.join(" · ", parts);
        return joined.isEmpty() ? ("Goal #" + g.getGoalId()) : joined;
    }

    // Map list items có userName/goalName
    private List<WorkoutPlanListItemDTO> mapToListItems(List<WorkoutPlan> plans) {
        return plans.stream()
                .map(w -> new WorkoutPlanListItemDTO(
                w.getPlanId(),
                w.getPlanName(),
                w.getIsTemplate(),
                w.getCreatedAt(),
                w.getUserId() != null ? w.getUserId().getUserId() : null,
                displayUserName(w.getUserId()),
                w.getGoalId() != null ? w.getGoalId().getGoalId() : null,
                displayGoal(w.getGoalId())
        ))
                .collect(Collectors.toList());
    }

    private PlanDetail addDetailEntity(WorkoutPlan plan, PlanDetailItemDTO dto) {
        Exercises ex = exercisesRepo.findById(dto.getExerciseId());
        if (ex == null) {
            throw new IllegalArgumentException("Không tìm thấy bài tập");
        }
        PlanDetail d = new PlanDetail();
        d.setPlanId(plan);
        d.setExercisesId(ex);
        d.setDayOfWeek(dto.getDayOfWeek());
        d.setDuration(dto.getDuration());
        return detailRepo.save(d);
    }

    // Trả DTO chi tiết + tên user/goal
    private WorkoutPlanResponseDTO toPlanDTO(WorkoutPlan p) {
        WorkoutPlanResponseDTO dto = new WorkoutPlanResponseDTO();
        dto.setPlanId(p.getPlanId());
        dto.setPlanName(p.getPlanName());
        dto.setIsTemplate(p.getIsTemplate());
        dto.setCreatedAt(p.getCreatedAt());

        dto.setUserId(p.getUserId() != null ? p.getUserId().getUserId() : null);
        dto.setUserName(displayUserName(p.getUserId()));

        dto.setGoalId(p.getGoalId() != null ? p.getGoalId().getGoalId() : null);
        dto.setGoalName(displayGoal(p.getGoalId()));

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

    @Override
    public Map<String, Object> listAllPlansPaged(Integer page, Integer pageSize, String kw) {
        Map<String, String> params = new HashMap<>();
        if (kw != null && !kw.isBlank()) {
            params.put("kw", kw.trim());
        }
        if (page != null) {
            params.put("page", String.valueOf(page));
        }
        if (pageSize != null) {
            params.put("pageSize", String.valueOf(pageSize));
        }

        List<WorkoutPlan> plans = planRepo.getPlans(params);
        long total = planRepo.countPlans(params);

        int p = (page == null ? 1 : Math.max(page, 1));
        int ps = (pageSize == null ? 10 : Math.max(pageSize, 1));
        int totalPages = (int) Math.ceil(total * 1.0 / ps);

        return Map.of(
                "page", p,
                "pageSize", ps,
                "totalPages", totalPages,
                "totalElements", total,
                "items", mapToListItems(plans)
        );
    }

    @Override
    public WorkoutPlanResponseDTO createPlanForUser(Integer userId, WorkoutPlanCreateRequest req) {
        User user = userRepo.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("Không tìm thấy người dùng");
        }

        WorkoutPlan plan = new WorkoutPlan();
        plan.setUserId(user);
        plan.setPlanName(req.getPlanName());
        plan.setIsTemplate(req.getIsTemplate() != null ? req.getIsTemplate() : Boolean.FALSE);
        plan.setCreatedAt(new Date());

        if (req.getGoalId() != null) {
            Goal g = goalRepo.findById(req.getGoalId());
            if (g == null || !g.getUserId().getUserId().equals(user.getUserId())) {
                throw new IllegalArgumentException("Mục tiêu không tồn tại hoặc không thuộc về user này");
            }
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
    public void deletePlanAdmin(Integer planId) {
        WorkoutPlan p = planRepo.findById(planId);
        if (p == null) {
            throw new IllegalArgumentException("Kế hoạch không tồn tại");
        }
        planRepo.delete(p);
    }

    @Override
    public WorkoutPlanResponseDTO updatePlanAdmin(Integer planId, WorkoutPlanCreateRequest req) {
        WorkoutPlan p = planRepo.findById(planId);
        if (p == null) {
            throw new IllegalArgumentException("Không tìm thấy kế hoạch");
        }

        if (req.getPlanName() != null && !req.getPlanName().isBlank()) {
            p.setPlanName(req.getPlanName().trim());
        }
        if (req.getIsTemplate() != null) {
            p.setIsTemplate(req.getIsTemplate());
        }
        if (req.getGoalId() != null) {
            Goal g = goalRepo.findById(req.getGoalId());
            if (g == null) {
                throw new IllegalArgumentException("Mục tiêu không tồn tại");
            }
            p.setGoalId(g);
        }

        planRepo.save(p);
        p = planRepo.findById(planId);
        return toPlanDTO(p);
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
