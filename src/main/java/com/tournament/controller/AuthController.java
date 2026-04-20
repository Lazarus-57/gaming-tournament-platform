package com.tournament.controller;

import com.tournament.model.enums.RoleType;
import com.tournament.service.AuthManager;
import com.tournament.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;
    private final AuthManager authManager;

    public AuthController(UserService userService, AuthManager authManager) {
        this.userService = userService;
        this.authManager = authManager;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("roles", RoleType.values());
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String name,
                           @RequestParam String email,
                           @RequestParam String password,
                           @RequestParam RoleType role,
                           @RequestParam(required = false, defaultValue = "") String extra,
                           RedirectAttributes redirectAttributes) {
        try {
            userService.registerUser(role, name, email, password, extra);
            redirectAttributes.addFlashAttribute("success", "Registration successful! Please login.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    @PostMapping("/verify-email")
    public String verifyEmail(@RequestParam Integer userId,
                              @RequestParam String token,
                              RedirectAttributes redirectAttributes) {
        try {
            authManager.verifyEmail(userId, token);
            redirectAttributes.addFlashAttribute("success", "Email verified. You can now log in.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/login";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam Integer userId,
                            @RequestParam String otp,
                            RedirectAttributes redirectAttributes) {
        try {
            authManager.verifyOtp(userId, otp);
            redirectAttributes.addFlashAttribute("success", "OTP verified. Login complete.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/login";
    }
}
