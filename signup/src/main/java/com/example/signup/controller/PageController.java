package com.example.signup.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.signup.entity.User;
import jakarta.servlet.http.HttpSession;

@Controller
public class PageController {

    @GetMapping("/admin")
    public String adminPage(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !user.isAdmin()) {
            return "redirect:/api/auth/login";
        }
        return "admin";
    }

    @GetMapping("/home")
    public String homePage(HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/api/auth/login";
        }
        return "Home";
    }
}
