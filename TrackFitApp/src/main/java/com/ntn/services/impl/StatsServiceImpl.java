package com.ntn.services.impl;

import com.ntn.dto.*;
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
    @Autowired private UserRepository userRepo;

    private static final ZoneId VN = ZoneId.of("Asia/Ho_Chi_Minh");

    @Override
    public StatsSummaryDTO summarySystem(Date from, Date to) {
        return computeSummary(null, from, to);
    }

    @Override
    public StatsSummaryDTO summaryUser(String username, Date from, Date to) {
        if (username == null || username.isBlank())
            throw new IllegalArgumentException("username không được trống");

        User u = userRepo.getUserByUsername(username);
        if (u == null) throw new IllegalArgumentException("Không tìm thấy người dùng: " + username);

        return computeSummary(u.getUserId(), from, to);
    }

    private StatsSummaryDTO computeSummary(Integer userId, Date from, Date to) {
        LocalDate toDay = (to != null)
                ? to.toInstant().atZone(VN).toLocalDate()
                : LocalDate.now(VN);
        LocalDate fromDay = (from != null)
                ? from.toInstant().atZone(VN).toLocalDate()
                : toDay.minusDays(30);

        Date startDate = Date.from(fromDay.atStartOfDay(VN).toInstant());
        Date endDate   = Date.from(toDay.plusDays(1).atStartOfDay(VN).toInstant());

        List<UserWorkoutHistory> all = historyRepo.findBetween(userId, startDate, endDate, null);

        long totalSessions = all.size();
        List<UserWorkoutHistory> completed = all.stream()
                .filter(h -> "COMPLETED".equalsIgnoreCase(h.getStatus()))
                .toList();
        long totalCompleted = completed.size();

        Map<String, Integer> durationCache = new HashMap<>();
        long totalMinutes = 0L;

        Map<LocalDate, List<UserWorkoutHistory>> byDay = new HashMap<>();
        for (UserWorkoutHistory h : all) {
            Date d = (h.getCompletedAt() != null) ? h.getCompletedAt() : new Date();
            LocalDate ld = d.toInstant().atZone(VN).toLocalDate();
            byDay.computeIfAbsent(ld, k -> new ArrayList<>()).add(h);
        }

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

        // byGoal
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

        // byExercise
        Map<String, Long> byExerciseMap = new HashMap<>();
        for (UserWorkoutHistory h : completed) {
            if (h.getExercisesId() != null && h.getExercisesId().getName() != null) {
                String name = h.getExercisesId().getName().trim();
                if (!name.isEmpty()) byExerciseMap.merge(name, 1L, Long::sum);
            }
        }
        List<StatsByExerciseDTO> byExercise = byExerciseMap.entrySet().stream()
                .map(e -> new StatsByExerciseDTO(e.getKey(), e.getValue()))
                .sorted((a, b) -> Long.compare(b.getCompletedCount(), a.getCompletedCount()))
                .collect(Collectors.toList());

        String topExerciseName = byExercise.isEmpty() ? null : byExercise.get(0).getName();

        StatsSummaryDTO out = new StatsSummaryDTO();
        out.setTotalSessions(totalSessions);
        out.setTotalCompleted(totalCompleted);
        out.setTotalMinutes(totalMinutes);
        out.setDaily(daily);
        out.setByGoal(byGoal);
        out.setByExercise(byExercise);
        out.setTopExerciseName(topExerciseName);

        return out;
    }
}
