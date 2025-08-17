package com.ntn.controllers;

import com.ntn.services.AdminStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @Autowired
    private AdminStatsService statsService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("usersCount",         statsService.countUsers());
        model.addAttribute("exercisesCount",     statsService.countExercises());
        model.addAttribute("templatePlansCount", statsService.countTemplatePlans());
        model.addAttribute("todayWorkouts",      statsService.countTodayWorkouts());

        model.addAttribute("chartLabels", statsService.systemChartLabels());
        model.addAttribute("chartData",   statsService.systemChartData());
        return "dashboard";
    }
}
