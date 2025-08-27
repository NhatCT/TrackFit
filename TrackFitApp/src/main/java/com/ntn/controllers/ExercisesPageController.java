package com.ntn.controllers;

import com.ntn.dto.ExerciseCreateUpdateDTO;
import com.ntn.dto.ExerciseDTO;
import com.ntn.services.ExercisesService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
public class ExercisesPageController {

    @Autowired
    private ExercisesService exercisesService;

    // LIST
    @GetMapping("/exercises")
    public String list(Model model,
                       @RequestParam(name = "page", defaultValue = "1") Integer page,
                       @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                       @RequestParam(name = "kw", required = false) String kw) {

        Map<String, Object> res = exercisesService.list(page, pageSize, kw);

        model.addAttribute("items",         res.get("items"));
        model.addAttribute("page",          res.get("page"));
        model.addAttribute("pageSize",      res.get("pageSize"));
        model.addAttribute("totalPages",    res.get("totalPages"));
        model.addAttribute("totalElements", res.get("totalElements"));
        model.addAttribute("kw", kw);

        return "exercises-list";
    }

    // CREATE FORM
    @GetMapping("/exercises/new")
    public String createForm(Model model) {
        model.addAttribute("form", new ExerciseCreateUpdateDTO());
        model.addAttribute("mode", "create");
        return "exercises-form";
    }

    // EDIT FORM
    @GetMapping("/exercises/{id}")
    public String editForm(@PathVariable("id") Integer id, Model model) {
        ExerciseDTO dto = exercisesService.get(id);

        ExerciseCreateUpdateDTO form = new ExerciseCreateUpdateDTO();
        form.setName(dto.getName());
        form.setTargetGoal(dto.getTargetGoal());
        form.setMuscleGroup(dto.getMuscleGroup());
        form.setVideoUrl(dto.getVideoUrl());
        form.setDescription(dto.getDescription());

        model.addAttribute("form", form);
        model.addAttribute("exerciseId", id);
        model.addAttribute("mode", "edit");
        return "exercises-form";
    }

    // SUBMIT CREATE
    @PostMapping("/exercises")
    public String create(@Valid @ModelAttribute("form") ExerciseCreateUpdateDTO form,
                         BindingResult br,
                         RedirectAttributes ra,
                         Model model) {
        if (br.hasErrors()) {
            model.addAttribute("mode", "create");
            return "exercises-form";
        }
        exercisesService.create(form);
        ra.addFlashAttribute("msg", "Thêm bài tập thành công");
        return "redirect:/exercises";
    }

    // SUBMIT UPDATE
    @PostMapping("/exercises/{id}")
    public String update(@PathVariable("id") Integer id,
                         @Valid @ModelAttribute("form") ExerciseCreateUpdateDTO form,
                         BindingResult br,
                         RedirectAttributes ra,
                         Model model) {
        if (br.hasErrors()) {
            model.addAttribute("mode", "edit");
            model.addAttribute("exerciseId", id);
            return "exercises-form";
        }
        exercisesService.update(id, form);
        ra.addFlashAttribute("msg", "Cập nhật bài tập thành công");
        return "redirect:/exercises";
    }

    // DELETE
    @PostMapping("/exercises/{id}/delete")
    public String delete(@PathVariable("id") Integer id, RedirectAttributes ra) {
        exercisesService.delete(id);
        ra.addFlashAttribute("msg", "Đã xoá bài tập");
        return "redirect:/exercises";
    }

    // GLOBAL ERROR -> quay về list với flash error
    @ExceptionHandler(Exception.class)
    public String handleError(Exception ex, RedirectAttributes ra) {
        ra.addFlashAttribute("error", "Đã xảy ra lỗi: " + ex.getMessage());
        return "redirect:/exercises";
    }
}
