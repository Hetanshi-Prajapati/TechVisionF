package com.example.signup.controller;

// public class ProfileController {

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.signup.entity.User;
import com.example.signup.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

    
// }

@Controller
public class ProfileController {

    @Autowired
    UserRepository userRepository;

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model){

        User user = (User) session.getAttribute("user");

        if(user == null){
            return "redirect:/api/auth/login";
        }

        model.addAttribute("user", user);

        return "profile";
    }

    @PostMapping("/update-profile")
    public String updateProfile(@RequestParam String bio,
                                @RequestParam String githubUsername,
                                @RequestParam String primarySkill,
                                HttpSession session){

        User user = (User) session.getAttribute("user");

        user.setBio(bio);
        user.setGithubUsername(githubUsername);
        user.setPrimarySkill(primarySkill);

        userRepository.save(user);

        session.setAttribute("user", user);

        return "redirect:/profile";
    }
}