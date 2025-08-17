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

    @Autowired private UserRepository userRepo;
    @Autowired private UserWorkoutHistoryRepository historyRepo;
    @Autowired private PlanDetailRepository planDetailRepo;

    @Override
    public StatsSummaryDTO summary(String username, Date from, Date to) {
        User u = userRepo.getUserByUsername(username);
        if (u == null) throw new IllegalArgumentException("Không tìm thấy người dùng");

        // Mặc định 30 ngày gần nhất
        if (to == null) to = new Date();
        if (from == null) from = Date.from(Instant.ofEpochMilli(to.getTime()).minus(Period.ofDays(30)));

        List<UserWorkoutHistory> all = historyRepo.findBetween(u.getUserId(), from, to, null);

        long totalSessions = all.size();
        List<UserWorkoutHistory> completed = all.stream()
                .filter(h -> "COMPLETED".equalsIgnoreCase(h.getStatus()))
                .collect(Collectors.toList());
        long totalCompleted = completed.size();

        long totalMinutes = 0L;
        Map<String, Long> byGoalCount = new HashMap<>();

        // gom theo ngày
        Map<LocalDate, List<UserWorkoutHistory>> byDay = new TreeMap<>();
        for (UserWorkoutHistory h : all) {
            Date d = h.getCompletedAt() != null ? h.getCompletedAt() : new Date();
            LocalDate ld = Instant.ofEpochMilli(d.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
            byDay.computeIfAbsent(ld, k -> new ArrayList<>()).add(h);
        }

        List<StatsDailyPointDTO> daily = new ArrayList<>();
        for (var e : byDay.entrySet()) {
            LocalDate day = e.getKey();
            List<UserWorkoutHistory> list = e.getValue();

            long sessions = list.size();
            List<UserWorkoutHistory> done = list.stream().filter(h -> "COMPLETED".equalsIgnoreCase(h.getStatus())).toList();

            long minutes = 0L;
            for (UserWorkoutHistory h : done) {
                Integer planId = h.getPlanId() != null ? h.getPlanId().getPlanId() : null;
                Integer exId = h.getExercisesId() != null ? h.getExercisesId().getExercisesId() : null;
                if (planId != null && exId != null) {
                    Integer avg = planDetailRepo.avgDurationByPlanAndExercise(planId, exId);
                    if (avg != null) minutes += avg;
                }
                if (h.getPlanId() != null && h.getPlanId().getGoalId() != null) {
                    String g = h.getPlanId().getGoalId().getGoalType();
                    if (g != null) byGoalCount.merge(g, 1L, Long::sum);
                }
            }
            totalMinutes += minutes;

            StatsDailyPointDTO p = new StatsDailyPointDTO();
            p.setDate(Date.from(day.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            p.setSessions(sessions);
            p.setCompleted(done.size());
            p.setMinutes(minutes);
            daily.add(p);
        }

        List<StatsByGoalDTO> byGoal = byGoalCount.entrySet().stream()
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

