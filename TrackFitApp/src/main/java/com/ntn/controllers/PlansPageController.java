package com.ntn.controllers;

import com.ntn.dto.PlanDetailItemDTO;
import com.ntn.dto.WorkoutPlanCreateRequest;
import com.ntn.services.WorkoutPlanService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Map;

@Controller
public class PlansPageController {

    @Autowired
    private WorkoutPlanService planService;

    // LIST của user đăng nhập (dùng listPlansByUserPaged)
    @GetMapping("/plans")
    public String list(Model model,
                       Principal principal,
                       @RequestParam(value = "page", required = false) Integer page,
                       @RequestParam(value = "pageSize", required = false) Integer pageSize,
                       @RequestParam(value = "kw", required = false) String kw) {

        Map<String,Object> res = planService.listPlansByUserPaged(principal.getName(), page, pageSize, kw);

        model.addAttribute("items", res.get("items"));
        model.addAttribute("page", res.get("page"));
        model.addAttribute("pageSize", res.get("pageSize"));
        model.addAttribute("totalPages", res.get("totalPages"));
        model.addAttribute("totalElements", res.get("totalElements"));
        model.addAttribute("kw", kw);

        return "plans-list";
    }

    @GetMapping("/plans/new")
    public String createForm(Model model) {
        model.addAttribute("plan", new WorkoutPlanCreateRequest());
        return "plans-form";
    }

    @GetMapping("/plans/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        model.addAttribute("planView", planService.getPlan(id));
        return "plans-form";
    }

    @PostMapping("/plans")
    public String create(@Valid @ModelAttribute("plan") WorkoutPlanCreateRequest req,
                         BindingResult br,
                         Principal principal,
                         RedirectAttributes ra) {
        if (br.hasErrors()) return "plans-form";
        planService.createPlan(principal.getName(), req);
        ra.addFlashAttribute("msg", "Tạo kế hoạch thành công");
        return "redirect:/plans";
    }

    @PostMapping("/plans/{planId}/details")
    public String addDetail(@PathVariable Integer planId,
                            @Valid @ModelAttribute("detail") PlanDetailItemDTO detail,
                            BindingResult br,
                            RedirectAttributes ra) {
        if (br.hasErrors()) {
            ra.addFlashAttribute("err", "Dữ liệu chi tiết không hợp lệ");
            return "redirect:/plans/" + planId;
        }
        planService.addDetail(planId, detail);
        ra.addFlashAttribute("msg", "Đã thêm bài tập vào kế hoạch");
        return "redirect:/plans/" + planId;
    }
}
