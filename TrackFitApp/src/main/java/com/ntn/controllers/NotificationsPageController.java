package com.ntn.controllers;

import com.ntn.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
public class NotificationsPageController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/notifications")
    public String page(Model model,
                       Principal principal,
                       @RequestParam(value="page", required=false) Integer page,
                       @RequestParam(value="pageSize", required=false) Integer pageSize,
                       @RequestParam(value="isRead", required=false) Boolean isRead,
                       @RequestParam(value="type", required=false) String type,
                       @RequestParam(value="kw", required=false) String kw) {
        if (principal == null) return "redirect:/login";

        // LẤY DỮ LIỆU AN TOÀN (tránh NPE)
        Map<String, Object> res = notificationService
                .listByUserPaged(principal.getName(), page, pageSize, isRead, type, kw);

        model.addAttribute("items", res.getOrDefault("items", List.of()));
        model.addAttribute("page", res.getOrDefault("page", 1));
        model.addAttribute("pageSize", res.getOrDefault("pageSize", 0));
        model.addAttribute("totalPages", res.getOrDefault("totalPages", 1));
        model.addAttribute("totalElements", res.getOrDefault("totalElements", 0));
        model.addAttribute("kw", kw);
        model.addAttribute("isRead", isRead);
        model.addAttribute("type", type);
        model.addAttribute("pageTitle", "Thông báo");

        return "notifications";
    }

    @PostMapping("/notifications/{id}/read")
    public String toggleRead(@PathVariable("id") Integer id,
                             @RequestParam("value") boolean value,
                             Principal principal,
                             RedirectAttributes ra) {
        if (principal == null) return "redirect:/login";
        notificationService.markRead(principal.getName(), id, value);
        ra.addFlashAttribute("msg", value ? "Đã đánh dấu đã đọc" : "Đã đánh dấu chưa đọc");
        return "redirect:/notifications";
    }

    @PostMapping("/notifications/{id}/delete")
    public String delete(@PathVariable("id") Integer id,
                         Principal principal,
                         RedirectAttributes ra) {
        if (principal == null) return "redirect:/login";
        notificationService.delete(principal.getName(), id);
        ra.addFlashAttribute("msg", "Đã xoá thông báo");
        return "redirect:/notifications";
    }
}
