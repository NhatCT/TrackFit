package com.ntn.controllers;

import com.ntn.dto.PlanDetailItemDTO;
import com.ntn.dto.WorkoutPlanCreateRequest;
import com.ntn.dto.WorkoutPlanResponseDTO;
import com.ntn.services.WorkoutPlanService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
public class PlansPageController {

    @Autowired
    private WorkoutPlanService planService;

    // ===== LIST (admin) =====
    @GetMapping("/plans")
    public String list(Model model,
                       @RequestParam(value = "page", required = false) Integer page,
                       @RequestParam(value = "pageSize", required = false) Integer pageSize,
                       @RequestParam(value = "kw", required = false) String kw) {

        Map<String, Object> res = planService.listAllPlansPaged(page, pageSize, kw);

        model.addAttribute("plans", res.get("items"));
        model.addAttribute("page", res.get("page"));
        model.addAttribute("pageSize", res.get("pageSize"));
        model.addAttribute("totalPages", res.get("totalPages"));
        model.addAttribute("totalElements", res.get("totalElements"));
        model.addAttribute("kw", kw);

        return "plans-list";
    }

    // ===== CREATE (GET) =====
    @GetMapping("/plans/new")
    public String createForm(Model model) {
        model.addAttribute("form", new WorkoutPlanCreateRequest());
        model.addAttribute("mode", "create");
        return "plans-form";
    }

    // ===== CREATE (POST) =====
    @PostMapping("/plans")
    public String createSubmit(@Valid @ModelAttribute("form") WorkoutPlanCreateRequest form,
                               BindingResult br,
                               RedirectAttributes ra,
                               Model model) {
        if (br.hasErrors()) {
            model.addAttribute("mode", "create");
            return "plans-form";
        }
        if (form.getUserId() == null) {
            br.rejectValue("userId", "userId.null", "Vui lòng chọn người dùng");
            model.addAttribute("mode", "create");
            return "plans-form";
        }

        WorkoutPlanResponseDTO dto = planService.createPlanForUser(form.getUserId(), form);
        ra.addFlashAttribute("msg", "Đã tạo kế hoạch");
        return "redirect:/plans/" + dto.getPlanId();
    }

    // ===== EDIT (GET) =====
    @GetMapping("/plans/{id}")
    public String editForm(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("form", planService.getPlan(id)); // DTO cho view/edit
        model.addAttribute("mode", "edit");
        model.addAttribute("planId", id);
        // Không addAttribute("detail") để tránh yêu cầu BindingResult/target 'detail'
        return "plans-form";
    }

    // ===== EDIT (POST) =====
    // ===== EDIT (POST) =====
@PostMapping("/plans/{id}")
public String updateSubmit(@PathVariable("id") Integer id,
                           @Valid @ModelAttribute("form") WorkoutPlanCreateRequest form,
                           BindingResult br,
                           RedirectAttributes ra,
                           Model model) {
    if (br.hasErrors()) {
        model.addAttribute("mode", "edit");
        model.addAttribute("planId", id);
        return "plans-form";
    }
    planService.updatePlanAdmin(id, form);
    ra.addFlashAttribute("msg", "Đã cập nhật kế hoạch");
    // 👉 Sau khi cập nhật, redirect về danh sách thay vì trang chi tiết
    return "redirect:/plans";
}


    // ===== ADD DETAIL (POST) =====
    // Không đặt tên @ModelAttribute để bind theo name= trong form (exerciseId, dayOfWeek, duration)
    @PostMapping("/plans/{planId}/details")
    public String addDetail(@PathVariable("planId") Integer planId,
                            @Valid PlanDetailItemDTO detail,
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

    // ===== DELETE PLAN (POST) =====
    @PostMapping("/plans/{planId}/delete")
    public String deletePlan(@PathVariable("planId") Integer planId,
                             RedirectAttributes ra) {
        planService.deletePlanAdmin(planId);
        ra.addFlashAttribute("msg", "Đã xoá kế hoạch");
        return "redirect:/plans";
    }
}
