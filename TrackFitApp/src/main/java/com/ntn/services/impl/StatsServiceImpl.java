package com.ntn.services.impl;

import com.ntn.dto.StatsByGoalDTO;
import com.ntn.dto.StatsDailyPointDTO;
import com.ntn.dto.StatsSummaryDTO;
import com.ntn.pojo.User;
import com.ntn.pojo.UserWorkoutHistory;
import com.ntn.repositories.PlanDetailRepository;
import com.ntn.repositories.UserRepository;
import com.ntn.repositories.UserWorkoutHistoryRepository;
import com.ntn.services.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class StatsServiceImpl implements StatsService {

    @Autowired private UserWorkoutHistoryRepository historyRepo;
    @Autowired private PlanDetailRepository planDetailRepo;
    @Autowired private UserRepository userRepo; // <-- thêm

    private static final ZoneId VN = ZoneId.of("Asia/Ho_Chi_Minh");

    @Override
    public StatsSummaryDTO summarySystem(Date from, Date to) {
        return computeSummary(/*userId=*/null, from, to);
    }

    @Override
    public StatsSummaryDTO summaryUser(String username, Date from, Date to) {
        if (username == null || username.isBlank())
            throw new IllegalArgumentException("username không được trống");

        User u = userRepo.getUserByUsername(username);
        if (u == null) throw new IllegalArgumentException("Không tìm thấy người dùng: " + username);

        return computeSummary(u.getUserId(), from, to); // <-- map sang userId rồi gọi core
    }

    // ===== Core tính toán chung cho cả system & user =====
    private StatsSummaryDTO computeSummary(Integer userId, Date from, Date to) {
        // Khoảng thời gian mặc định: 30 ngày gần nhất theo VN
        LocalDate toDay = (to != null)
                ? to.toInstant().atZone(VN).toLocalDate()
                : LocalDate.now(VN);
        LocalDate fromDay = (from != null)
                ? from.toInstant().atZone(VN).toLocalDate()
                : toDay.minusDays(30);

        Date startDate = Date.from(fromDay.atStartOfDay(VN).toInstant());
        Date endDate   = Date.from(toDay.plusDays(1).atStartOfDay(VN).toInstant()); // [from, to+1) exclusive

        // Repo cần hỗ trợ: nếu userId == null thì KHÔNG lọc theo user
        List<UserWorkoutHistory> all = historyRepo.findBetween(userId, startDate, endDate, null);

        long totalSessions = all.size();
        List<UserWorkoutHistory> completed = all.stream()
                .filter(h -> "COMPLETED".equalsIgnoreCase(h.getStatus()))
                .toList();
        long totalCompleted = completed.size();

        // Cache duration theo (planId, exId) để tránh N+1
        Map<String, Integer> durationCache = new HashMap<>();
        long totalMinutes = 0L;

        // Gom theo ngày (VN)
        Map<LocalDate, List<UserWorkoutHistory>> byDay = new HashMap<>();
        for (UserWorkoutHistory h : all) {
            Date d = (h.getCompletedAt() != null) ? h.getCompletedAt() : new Date();
            LocalDate ld = d.toInstant().atZone(VN).toLocalDate();
            byDay.computeIfAbsent(ld, k -> new ArrayList<>()).add(h);
        }

        // Lấp đủ ngày từ fromDay → toDay
        List<StatsDailyPointDTO> daily = new ArrayList<>();
        for (LocalDate day = fromDay; !day.isAfter(toDay); day = day.plusDays(1)) {
            List<UserWorkoutHistory> list = byDay.getOrDefault(day, Collections.emptyList());
            long sessions = list.size();
            List<UserWorkoutHistory> done = list.stream()
                    .filter(h -> "COMPLETED".equalsIgnoreCase(h.getStatus()))
                    .toList();

            long minutes = 0L;
            for (UserWorkoutHistory h : done) {
                Integer planId = (h.getPlanId() != null) ? h.getPlanId().getPlanId() : null;
                Integer exId   = (h.getExercisesId() != null) ? h.getExercisesId().getExercisesId() : null;
                if (planId != null && exId != null) {
                    String key = planId + "#" + exId;
                    Integer avg = durationCache.computeIfAbsent(
                            key, k -> planDetailRepo.avgDurationByPlanAndExercise(planId, exId));
                    if (avg != null) minutes += avg;
                }
            }
            totalMinutes += minutes;

            StatsDailyPointDTO p = new StatsDailyPointDTO();
            p.setDate(Date.from(day.atStartOfDay(VN).toInstant()));
            p.setSessions(sessions);
            p.setCompleted(done.size());
            p.setMinutes(minutes);
            daily.add(p);
        }

        // byGoal: chỉ đếm completed
        Map<String, Long> byGoalMap = new HashMap<>();
        for (UserWorkoutHistory h : completed) {
            if (h.getPlanId() != null && h.getPlanId().getGoalId() != null) {
                String g = h.getPlanId().getGoalId().getGoalType();
                if (g != null && !g.isBlank()) byGoalMap.merge(g, 1L, Long::sum);
            }
        }
        List<StatsByGoalDTO> byGoal = byGoalMap.entrySet().stream()
                .map(e -> new StatsByGoalDTO(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(StatsByGoalDTO::getCompletedCount).reversed())
                .collect(Collectors.toList());

        StatsSummaryDTO out = new StatsSummaryDTO();
        out.setTotalSessions(totalSessions);
        out.setTotalCompleted(totalCompleted);
        out.setTotalMinutes(totalMinutes);
        out.setDaily(daily);
        out.setByGoal(byGoal);
        return out;
    }
}
