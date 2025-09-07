package com.ntn.services;

import java.util.List;

public interface AdminStatsService {
    long countUsers();
    long countExercises();
    long countTemplatePlans();
    long countTodayWorkouts();
    List<String> systemChartLabels();
    List<Long> systemChartData();    
}
