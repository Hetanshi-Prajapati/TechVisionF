package com.example.signup.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.signup.entity.FiveYearPlan;
import com.example.signup.entity.YearGoal;
import com.example.signup.repository.FiveYearPlanRepository;
import com.example.signup.repository.YearGoalRepository;

@Service
public class DynamicPlanService {

    @Autowired
    private FiveYearPlanRepository fiveYearPlanRepository;

    @Autowired
    private YearGoalRepository yearGoalRepository;

    @Transactional
    public FiveYearPlan generatePlan(Long userId, String domain, String language, String level) {
        domain = domain == null ? null : domain.trim().toLowerCase();
        language = language == null ? null : language.trim().toLowerCase();
        level = level == null ? null : level.trim().toLowerCase();

        // Check if plan already exists
        Optional<FiveYearPlan> existingPlan = fiveYearPlanRepository.findByUserIdAndDomainAndLanguageAndLevel(userId,
                domain, language, level);
        if (existingPlan.isPresent()) {
            return existingPlan.get();
        }

        FiveYearPlan plan = new FiveYearPlan();

        plan.setUserId(userId);
        plan.setDomain(domain);
        plan.setLanguage(language);
        plan.setLevel(level);
        plan.setProgress(0);
        plan.setTitle(language + " Mastery Roadmap");
        plan.setVision("To become an expert in " + domain + " using " + language);

        LocalDateTime now = LocalDateTime.now();
        plan.setUpdatedAt(now);
        plan.setCreatedAt(now);

        try {
            plan = fiveYearPlanRepository.save(plan);
        } catch (Exception e) {
            // If duplicate happens, fetch existing instead of crashing
            Optional<FiveYearPlan> existing = fiveYearPlanRepository.findByUserIdAndDomainAndLanguageAndLevel(
                    userId, domain, language, level);

            if (existing.isPresent()) {
                return existing.get();
            }

            throw e; // if something else went wrong
        }

        List<YearGoal> goals = new ArrayList<>();
        boolean isAdvanced = "advanced".equalsIgnoreCase(level);

        if (isAdvanced) {
            goals.add(createYearGoal(plan.getId(), 1, "Build Advanced Projects",
                    "Build complex, scalable " + domain + " projects utilizing advanced " + language + " patterns."));
            goals.add(createYearGoal(plan.getId(), 2, "Open Source & Industry Contribution",
                    "Contribute to open-source " + language + " projects and secure a high-level internship."));
            goals.add(createYearGoal(plan.getId(), 3, "System Design & Architecture",
                    "Master large-scale system design and architectural patterns for " + domain + "."));
            goals.add(createYearGoal(plan.getId(), 4, "Senior Level Engineering",
                    "Lead technical projects and mentor junior developers in " + language + "."));
            goals.add(createYearGoal(plan.getId(), 5, "Industry Expert & Job Ready",
                    "Become an established expert in " + domain + " ready for senior and staff-level roles."));
        } else {
            goals.add(createYearGoal(plan.getId(), 1, "Learn " + language + " Basics",
                    "Master the fundamental syntax, data structures, and core concepts of " + language + "."));
            goals.add(createYearGoal(plan.getId(), 2, "Intermediate " + domain + " Concepts",
                    "Learn intermediate tools, frameworks, and APIs for " + domain + " using " + language + "."));
            goals.add(createYearGoal(plan.getId(), 3, "Build " + domain + " Projects",
                    "Apply your knowledge to build functional, end-to-end " + domain + " projects."));
            goals.add(createYearGoal(plan.getId(), 4, "Internship Experience",
                    "Secure and complete a software engineering internship focusing on " + domain + "."));
            goals.add(createYearGoal(plan.getId(), 5, "Job Ready",
                    "Refine interview skills, complete advanced projects, and land a full-time role."));
        }

        for (YearGoal goal : goals) {
            yearGoalRepository.save(goal);
        }

        return plan;
    }

    private YearGoal createYearGoal(Long planId, int yearNumber, String title, String description) {
        YearGoal goal = new YearGoal();
        goal.setPlanId(planId);
        goal.setYearNumber(yearNumber);
        goal.setGoalTitle(title);
        goal.setGoalDescription(description);
        goal.setStatus("NOT_STARTED");
        goal.setProgressPercent(0);
        return goal;
    }
}
