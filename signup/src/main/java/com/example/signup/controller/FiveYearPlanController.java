package com.example.signup.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.signup.entity.FiveYearPlan;
import com.example.signup.entity.User;
import com.example.signup.entity.YearGoal;
import com.example.signup.repository.FiveYearPlanRepository;
import com.example.signup.repository.YearGoalRepository;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/api/auth")
public class FiveYearPlanController {

    private static final String[] STATUS_OPTIONS = { "", "NOT_STARTED", "IN_PROGRESS", "COMPLETED", "ON_HOLD" };

    @Autowired
    private FiveYearPlanRepository fiveYearPlanRepository;

    @Autowired
    private YearGoalRepository yearGoalRepository;

    @GetMapping("/my-five-year-plan")
    @org.springframework.web.bind.annotation.ResponseBody
    public org.springframework.http.ResponseEntity<?> myFiveYearPlan(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return org.springframework.http.ResponseEntity.status(401).body(java.util.Map.of("message", "Unauthorized"));
        }

        Optional<FiveYearPlan> plan = fiveYearPlanRepository.findByUserId(user.getId());
        List<YearGoal> goals = plan.map(value -> yearGoalRepository.findByPlanIdOrderByYearNumberAsc(value.getId()))
                .orElseGet(ArrayList::new);

        return org.springframework.http.ResponseEntity.ok(java.util.Map.of(
            "plan", plan.orElseGet(FiveYearPlan::new),
            "goals", goals,
            "statusOptions", STATUS_OPTIONS
        ));
    }

    @PostMapping("/my-five-year-plan")
    @Transactional
    public String saveMyFiveYearPlan(
            @RequestParam String title,
            @RequestParam String vision,
            @RequestParam(value = "goalTitle", required = false) String[] goalTitles,
            @RequestParam(value = "goalDescription", required = false) String[] goalDescriptions,
            @RequestParam(value = "status", required = false) String[] statuses,
            @RequestParam(value = "progressPercent", required = false) String[] progressValues,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/api/auth/login";
        }

        String cleanTitle = title == null ? "" : title.trim();
        String cleanVision = vision == null ? "" : vision.trim();

        List<YearGoal> draftGoals;
        try {
            draftGoals = buildDraftGoals(goalTitles, goalDescriptions, statuses, progressValues);
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/my-five-year-plan";
        }

        if (cleanTitle.isEmpty() || cleanVision.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Plan title and vision are required.");
            return "redirect:/my-five-year-plan";
        }

        FiveYearPlan plan = fiveYearPlanRepository.findByUserId(user.getId()).orElseGet(() -> {
            FiveYearPlan created = new FiveYearPlan();
            created.setUserId(user.getId());
            created.setCreatedAt(LocalDateTime.now());
            return created;
        });

        LocalDateTime now = LocalDateTime.now();
        plan.setTitle(cleanTitle);
        plan.setVision(cleanVision);
        plan.setUpdatedAt(now);
        if (plan.getCreatedAt() == null) {
            plan.setCreatedAt(now);
        }
        plan = fiveYearPlanRepository.save(plan);

        yearGoalRepository.deleteByPlanId(plan.getId());

        int yearNumber = 1;
        for (YearGoal goal : draftGoals) {
            normalizeProgressByStatus(goal);
            goal.setPlanId(plan.getId());
            goal.setYearNumber(yearNumber++);
            yearGoalRepository.save(goal);
        }

        // Calculate overall plan progress as average of all year goals
        int totalProgress = 0;
        for (YearGoal goal : draftGoals) {
            totalProgress += goal.getProgressPercent();
        }
        int overallProgress = draftGoals.isEmpty() ? 0 : totalProgress / draftGoals.size();
        plan.setProgress(overallProgress);
        fiveYearPlanRepository.save(plan);

        redirectAttributes.addFlashAttribute("successMessage", "Your 5-year plan was saved.");
        return "redirect:/my-five-year-plan";
    }

    private FiveYearPlan buildDraftPlan(Long userId, String title, String vision) {
        FiveYearPlan draft = new FiveYearPlan();
        draft.setUserId(userId);
        draft.setTitle(title);
        draft.setVision(vision);
        return draft;
    }

    private List<YearGoal> buildDraftGoals(String[] goalTitles, String[] goalDescriptions, String[] statuses,
            String[] progressValues) {
        List<YearGoal> goals = new ArrayList<>();
        int size = Math.max(Math.max(length(goalTitles), length(goalDescriptions)), Math.max(length(statuses), length(progressValues)));

        for (int i = 0; i < size; i++) {
            String title = valueAt(goalTitles, i);
            String description = valueAt(goalDescriptions, i);
            String status = valueAt(statuses, i);
            String progressRaw = valueAt(progressValues, i);

            boolean emptyRow = isBlank(title) && isBlank(description) && isBlank(status) && isBlank(progressRaw);
            if (emptyRow) {
                continue;
            }

            if (isBlank(title)) {
                throw new IllegalArgumentException("Each added year block needs a goal title. Remove the empty block or fill it in before saving.");
            }

            YearGoal goal = new YearGoal();
            goal.setGoalTitle(title.trim());
            goal.setGoalDescription(isBlank(description) ? "" : description.trim());
            goal.setStatus(isBlank(status) ? "NOT_STARTED" : status.trim());
            goal.setProgressPercent(parseProgress(progressRaw));
            goals.add(goal);
        }

        return goals;
    }

    private List<YearGoal> buildSafeGoals(String[] goalTitles, String[] goalDescriptions, String[] statuses,
            String[] progressValues) {
        List<YearGoal> goals = new ArrayList<>();
        int size = Math.max(Math.max(length(goalTitles), length(goalDescriptions)), Math.max(length(statuses), length(progressValues)));

        for (int i = 0; i < size; i++) {
            String title = valueAt(goalTitles, i);
            String description = valueAt(goalDescriptions, i);
            String status = valueAt(statuses, i);
            String progressRaw = valueAt(progressValues, i);

            if (isBlank(title) && isBlank(description) && isBlank(status) && isBlank(progressRaw)) {
                continue;
            }

            YearGoal goal = new YearGoal();
            goal.setGoalTitle(isBlank(title) ? "" : title.trim());
            goal.setGoalDescription(isBlank(description) ? "" : description.trim());
            goal.setStatus(isBlank(status) ? "" : status.trim());
            goal.setProgressPercent(parseProgress(progressRaw));
            goals.add(goal);
        }

        return goals;
    }

    private void normalizeProgressByStatus(YearGoal goal) {
        if (goal == null) {
            return;
        }

        String status = goal.getStatus() == null ? "" : goal.getStatus().trim();
        if ("NOT_STARTED".equals(status)) {
            goal.setProgressPercent(0);
        } else if ("COMPLETED".equals(status)) {
            goal.setProgressPercent(100);
        }
    }

    private int parseProgress(String progressRaw) {
        if (isBlank(progressRaw)) {
            return 0;
        }

        try {
            int value = Integer.parseInt(progressRaw.trim());
            return Math.max(0, Math.min(100, value));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private int length(String[] values) {
        return values == null ? 0 : values.length;
    }

    private String valueAt(String[] values, int index) {
        if (values == null || index >= values.length) {
            return "";
        }
        return values[index] == null ? "" : values[index];
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}