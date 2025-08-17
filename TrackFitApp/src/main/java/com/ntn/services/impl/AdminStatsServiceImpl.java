package com.ntn.services.impl;

import com.ntn.services.AdminStatsService;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Service
@Transactional
public class AdminStatsServiceImpl implements AdminStatsService {

    @Autowired
    private LocalSessionFactoryBean factory;

    @Override
    public long countUsers() {
        Session s = factory.getObject().getCurrentSession();
        Long c = s.createQuery("select count(u.userId) from User u", Long.class).getSingleResult();
        return c != null ? c : 0L;
    }

    @Override
    public long countExercises() {
        Session s = factory.getObject().getCurrentSession();
        Long c = s.createQuery("select count(e.exercisesId) from Exercises e", Long.class).getSingleResult();
        return c != null ? c : 0L;
    }

    @Override
    public long countTemplatePlans() {
        Session s = factory.getObject().getCurrentSession();
        Long c = s.createQuery("select count(p.planId) from WorkoutPlan p where p.isTemplate = true", Long.class)
                  .getSingleResult();
        return c != null ? c : 0L;
    }

    @Override
    public long countTodayWorkouts() {
        Session s = factory.getObject().getCurrentSession();
        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime start = LocalDate.now().atStartOfDay(zone);
        Date from = Date.from(start.toInstant());
        Date to   = Date.from(start.plusDays(1).minusNanos(1).toInstant());

        Long c = s.createQuery(
                "select count(h.historyId) from UserWorkoutHistory h " +
                "where h.completedAt between :from and :to", Long.class)
                .setParameter("from", from)
                .setParameter("to", to)
                .getSingleResult();
        return c != null ? c : 0L;
    }

    @Override
    public List<String> systemChartLabels() {
        List<String> labels = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) labels.add(today.minusDays(i).toString());
        return labels;
    }

    @Override
    public List<Long> systemChartData() {
        Session s = factory.getObject().getCurrentSession();
        ZoneId zone = ZoneId.systemDefault();

        LocalDate startDate = LocalDate.now().minusDays(6);
        Date from = Date.from(startDate.atStartOfDay(zone).toInstant());
        Date to   = Date.from(LocalDate.now().plusDays(1).atStartOfDay(zone).minusNanos(1).toInstant());

        List<Date> dates = s.createQuery(
                "select h.completedAt from UserWorkoutHistory h " +
                "where h.completedAt between :from and :to and upper(h.status) = 'COMPLETED'",
                Date.class)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();

        Map<LocalDate, Long> bucket = new HashMap<>();
        for (Date d : dates) {
            LocalDate key = d.toInstant().atZone(zone).toLocalDate();
            bucket.merge(key, 1L, Long::sum);
        }

        List<Long> data = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            data.add(bucket.getOrDefault(day, 0L));
        }
        return data;
    }
}
