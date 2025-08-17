package com.ntn.services;

import java.util.List;

public interface AdminStatsService {
    long countUsers();
    long countExercises();
    long countTemplatePlans();
    long countTodayWorkouts();

    // Dữ liệu biểu đồ 7 ngày gần nhất (COMPLETED)
    List<String> systemChartLabels(); // yyyy-MM-dd
    List<Long> systemChartData();     // số buổi hoàn thành theo ngày
}
