package com.ntn.controllers;

import com.ntn.services.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;

@Controller
public class StatsPageController {

    @Autowired
    private StatsService statsService;

    @GetMapping("/stats")
    public String stats(@RequestParam(value = "from", required = false)
                        @DateTimeFormat(pattern = "yyyy-MM-dd") Date from,
                        @RequestParam(value = "to", required = false)
                        @DateTimeFormat(pattern = "yyyy-MM-dd") Date to,
                        Model model) {
        var summary = statsService.summarySystem(from, to); // ✅ toàn hệ thống
        model.addAttribute("summary", summary);
        return "stats";
    }
}
