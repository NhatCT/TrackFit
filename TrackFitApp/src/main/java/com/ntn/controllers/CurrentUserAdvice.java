package com.ntn.configs;

import com.ntn.dto.UserResponseDTO;
import com.ntn.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

@Component
@ControllerAdvice
public class CurrentUserAdvice {

    @Autowired
    private UserService userService;

    @ModelAttribute
    public void addCurrentUser(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return;
        }

        String username = auth.getName();
        try {
            UserResponseDTO currentUser = userService.getUserByUsername(username);
            model.addAttribute("currentUser", currentUser);
        } catch (Exception ignore) {
        }
    }
}
