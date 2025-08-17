// com/ntn/controllers/UsersPageController.java
package com.ntn.controllers;

import com.ntn.dto.AdminUserFormDTO;
import com.ntn.services.UserService;
import jakarta.validation.Valid; // <== import này
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UsersPageController {

    @Autowired
    private UserService userService;

    @GetMapping("/users")
    public String list(Model model) {
        model.addAttribute("users", userService.listAllUsers());
        return "users-list";
    }

    @GetMapping("/users/new")
    public String createForm(Model model) {
        model.addAttribute("form", new AdminUserFormDTO());
        model.addAttribute("mode", "create");
        return "users-form";
    }

    @GetMapping("/users/{id}")
    public String editForm(@PathVariable("id") Integer id, Model model) {
        var dto = userService.getUserById(id);
        AdminUserFormDTO form = new AdminUserFormDTO();
        form.setUsername(dto.getUsername());
        form.setEmail(dto.getEmail());
        form.setFirstName(dto.getFirstName());
        form.setLastName(dto.getLastName());
        form.setRole(dto.getRole());
        form.setGender(dto.getGender());      // service đã normalize
        form.setBirthDate(dto.getBirthDate());
        form.setAvatarUrl(dto.getAvatarUrl()); // để xem trước

        model.addAttribute("form", form);
        model.addAttribute("userId", id);
        model.addAttribute("mode", "edit");
        return "users-form";
    }

    @PostMapping("/users")
    public String create(@Valid @ModelAttribute("form") AdminUserFormDTO form,
            BindingResult br,
            RedirectAttributes ra,
            Model model) {
        if (br.hasErrors()) {
            model.addAttribute("mode", "create");
            return "users-form";
        }
        userService.createUserByAdmin(form);
        ra.addFlashAttribute("msg", "Thêm người dùng thành công");
        return "redirect:/users";
    }

    @PostMapping("/users/{id}")
    public String update(@PathVariable("id") Integer id,
            @ModelAttribute("form") AdminUserFormDTO form,
            BindingResult br,
            RedirectAttributes ra,
            Model model) {


        if (br.hasErrors()) {
            model.addAttribute("mode", "edit");
            model.addAttribute("userId", id);
            return "users-form";
        }
        userService.updateUserByAdmin(id, form);
        ra.addFlashAttribute("msg", "Cập nhật người dùng thành công");
        return "redirect:/users";
    }

    @PostMapping("/users/{id}/delete")
    public String delete(@PathVariable("id") Integer id, RedirectAttributes ra) {
        userService.deleteById(id);
        ra.addFlashAttribute("msg", "Xoá người dùng thành công");
        return "redirect:/users";
    }
}
