package com.example.signup.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.signup.entity.FiveYearPlan;
import com.example.signup.entity.User;
import com.example.signup.entity.YearGoal;
import com.example.signup.repository.FiveYearPlanRepository;
import com.example.signup.repository.YearGoalRepository;
import com.example.signup.service.DynamicPlanService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/auth")
public class DynamicPlanController {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Path PLAN_NOTES_DIR = Paths.get("uploads", "plan-notes");
    private static final Path PLAN_FILES_DIR = Paths.get("uploads", "plan-files");
    private static final Path PLAN_TIMELINE_DIR = Paths.get("uploads", "plan-timeline");

    @Autowired
    private FiveYearPlanRepository fiveYearPlanRepository;

    @Autowired
    private YearGoalRepository yearGoalRepository;

    @Autowired
    private DynamicPlanService dynamicPlanService;

    @GetMapping("/five-year-plan")
    public ResponseEntity<?> getFiveYearPlan(
            @RequestParam String domain,
            @RequestParam String language,
            @RequestParam String level,
            HttpSession session) {
        Long userId = getSessionUserId(session);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }

        Optional<FiveYearPlan> plan = fiveYearPlanRepository.findByUserIdAndDomainAndLanguageAndLevel(userId, domain,
                language, level);
        if (plan.isPresent()) {
            List<YearGoal> goals = yearGoalRepository.findByPlanIdOrderByYearNumberAsc(plan.get().getId());
            Map<String, Object> response = buildPlanResponse(plan.get(), goals);
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.ok(null);
    }

    @PostMapping("/generate-plan")
    public ResponseEntity<?> generatePlan(@RequestBody Map<String, Object> payload, HttpSession session) {
        try {
            System.out.println("Incoming request: " + payload);

            Long userId = null;
            if (payload.containsKey("userId") && payload.get("userId") != null) {
                userId = Long.valueOf(payload.get("userId").toString());
            } else {
                userId = getSessionUserId(session);
            }

            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
            }

            String domain = payload.get("domain") == null ? null
                    : payload.get("domain").toString().trim().toLowerCase();
            String language = payload.get("language") == null ? null
                    : payload.get("language").toString().trim().toLowerCase();
            String level = payload.get("level") == null ? null : payload.get("level").toString().trim().toLowerCase();

            if (domain == null || language == null || level == null || domain.isEmpty() || language.isEmpty()
                    || level.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Missing fields"));
            }

            // Check if plan already exists for this domain + language + level
            Optional<FiveYearPlan> existingPlan = fiveYearPlanRepository.findByUserIdAndDomainAndLanguageAndLevel(
                    userId,
                    domain, language, level);
            if (existingPlan.isPresent()) {
                // Return existing plan instead of creating duplicate
                List<YearGoal> goals = yearGoalRepository.findByPlanIdOrderByYearNumberAsc(existingPlan.get().getId());
                Map<String, Object> response = buildPlanResponse(existingPlan.get(), goals);
                return ResponseEntity.ok(response);
            }

            FiveYearPlan generated = dynamicPlanService.generatePlan(userId, domain, language, level);
            List<YearGoal> goals = yearGoalRepository.findByPlanIdOrderByYearNumberAsc(generated.getId());

            Map<String, Object> response = buildPlanResponse(generated, goals);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Failed to generate plan",
                    "message", e.getMessage()));
        }
    }

    @PostMapping("/update-progress")
    public ResponseEntity<?> updateProgress(@RequestBody Map<String, Object> body, HttpSession session) {
        Long planId = Long.valueOf(body.get("planId").toString());
        int yearNumber = Integer.parseInt(body.get("yearNumber").toString());
        int progress = Integer.parseInt(body.get("progressPercent").toString());

        Optional<FiveYearPlan> planOpt = getAuthorizedPlan(planId, session);
        if (planOpt.isEmpty()) {
            return ResponseEntity.status(403).body(Map.of("message", "Unauthorized or plan not found"));
        }

        YearGoal goal = yearGoalRepository.findByPlanIdAndYearNumber(planId, yearNumber);
        if (goal == null) {
            return ResponseEntity.status(404).body(Map.of("message", "Year goal not found"));
        }

        goal.setProgressPercent(progress);
        goal.setStatus(progress == 100 ? "COMPLETED" : "IN_PROGRESS");
        yearGoalRepository.save(goal);

        List<YearGoal> allGoals = yearGoalRepository.findByPlanIdOrderByYearNumberAsc(planId);
        int avg = (int) Math.round(allGoals.stream().mapToInt(YearGoal::getProgressPercent).average().orElse(0));

        FiveYearPlan plan = planOpt.get();
        plan.setProgress(avg);
        plan.setUpdatedAt(LocalDateTime.now());
        fiveYearPlanRepository.save(plan);

        appendTimelineEvent(planId, String.format("Updated progress for Year %d to %d%%.", yearNumber, progress));

        return ResponseEntity.ok(Map.of("message", "updated", "progress", avg));
    }

    @PostMapping("/save-notes")
    public ResponseEntity<?> saveNotes(@RequestBody Map<String, Object> body, HttpSession session) {
        Long planId = Long.valueOf(body.get("planId").toString());
        int yearNumber = Integer.parseInt(body.get("yearNumber").toString());
        String notes = body.get("notes") == null ? "" : body.get("notes").toString();

        if (getAuthorizedPlan(planId, session).isEmpty()) {
            return ResponseEntity.status(403).body(Map.of("message", "Unauthorized or plan not found"));
        }

        try {
            saveNotesForYear(planId, yearNumber, notes);
            appendTimelineEvent(planId, String.format("Saved notes for Year %d.", yearNumber));
            return ResponseEntity.ok(Map.of("message", "Notes saved", "notes", notes));
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("message", "Unable to save notes"));
        }
    }

    @PostMapping("/upload-file")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file,
            @RequestParam Long planId,
            @RequestParam Integer yearNumber,
            HttpSession session) {
        if (getAuthorizedPlan(planId, session).isEmpty()) {
            return ResponseEntity.status(403).body(Map.of("message", "Unauthorized or plan not found"));
        }

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "No file provided"));
        }

        try {
            String original = StringUtils.cleanPath(file.getOriginalFilename());
            if (original.contains("..")) {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid file name"));
            }

            Path uploadDir = PLAN_FILES_DIR.resolve(String.valueOf(planId)).resolve(String.valueOf(yearNumber));
            Files.createDirectories(uploadDir);

            String storedFileName = UUID.randomUUID().toString() + "_" + original;
            Path saved = uploadDir.resolve(storedFileName);
            Files.copy(file.getInputStream(), saved, StandardCopyOption.REPLACE_EXISTING);

            String fileUrl = "/uploads/plan-files/" + planId + "/" + yearNumber + "/" + storedFileName;
            appendTimelineEvent(planId, String.format("Uploaded attachment '%s' for Year %d.", original, yearNumber));

            return ResponseEntity
                    .ok(Map.of("message", "File uploaded", "fileName", storedFileName, "fileUrl", fileUrl));
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("message", "Failed to upload file"));
        }
    }

    @PostMapping("/delete-file")
    public ResponseEntity<?> deleteFile(@RequestBody Map<String, Object> body, HttpSession session) {
        Long planId = Long.valueOf(body.get("planId").toString());
        int yearNumber = Integer.parseInt(body.get("yearNumber").toString());
        String fileName = body.get("fileName") == null ? "" : body.get("fileName").toString();

        if (getAuthorizedPlan(planId, session).isEmpty()) {
            return ResponseEntity.status(403).body(Map.of("message", "Unauthorized or plan not found"));
        }

        if (fileName.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Missing file name"));
        }

        try {
            Path target = PLAN_FILES_DIR.resolve(String.valueOf(planId)).resolve(String.valueOf(yearNumber))
                    .resolve(Path.of(fileName).getFileName().toString());
            if (!Files.exists(target)) {
                return ResponseEntity.status(404).body(Map.of("message", "File not found"));
            }
            Files.delete(target);
            appendTimelineEvent(planId, String.format("Deleted attachment '%s' for Year %d.", fileName, yearNumber));
            return ResponseEntity.ok(Map.of("message", "File deleted"));
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("message", "Unable to delete file"));
        }
    }

    @GetMapping("/all-plans")
    public ResponseEntity<?> getAllPlans(HttpSession session) {
        Long userId = getSessionUserId(session);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }

        List<FiveYearPlan> plans = fiveYearPlanRepository.findAllByUserId(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (FiveYearPlan plan : plans) {
            Map<String, Object> planSummary = new HashMap<>();
            planSummary.put("plan_id", plan.getId());
            planSummary.put("domain", plan.getDomain());
            planSummary.put("language", plan.getLanguage());
            planSummary.put("level", plan.getLevel());
            planSummary.put("title", plan.getTitle());
            planSummary.put("progress", plan.getProgress());
            result.add(planSummary);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/five-year-plan-by-id")
    public ResponseEntity<?> getFiveYearPlanById(@RequestParam Long planId, HttpSession session) {
        Optional<FiveYearPlan> planOpt = getAuthorizedPlan(planId, session);
        if (planOpt.isEmpty()) {
            return ResponseEntity.status(403).body(Map.of("message", "Unauthorized or plan not found"));
        }

        List<YearGoal> goals = yearGoalRepository.findByPlanIdOrderByYearNumberAsc(planId);
        Map<String, Object> response = buildPlanResponse(planOpt.get(), goals);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/delete-plan")
    public ResponseEntity<?> deletePlan(@RequestParam Long planId, HttpSession session) {
        Optional<FiveYearPlan> planOpt = getAuthorizedPlan(planId, session);
        if (planOpt.isEmpty()) {
            return ResponseEntity.status(403).body(Map.of("message", "Unauthorized or plan not found"));
        }

        try {
            // Delete associated year goals (cascade)
            yearGoalRepository.deleteByPlanId(planId);

            // Delete the plan
            fiveYearPlanRepository.delete(planOpt.get());

            return ResponseEntity.ok(Map.of("message", "Plan deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Failed to delete plan"));
        }
    }

    private Map<String, Object> buildPlanResponse(FiveYearPlan plan, List<YearGoal> goals) {
        Map<String, Object> response = new HashMap<>();
        response.put("plan_id", plan.getId());
        response.put("title", plan.getTitle());
        response.put("vision", plan.getVision());
        response.put("domain", plan.getDomain());
        response.put("language", plan.getLanguage());
        response.put("level", plan.getLevel());
        response.put("progress", plan.getProgress());
        response.put("updatedAt", plan.getUpdatedAt() != null ? plan.getUpdatedAt().toString() : null);
        response.put("years", mapYearGoals(goals));
        return response;
    }

    private List<Map<String, Object>> mapYearGoals(List<YearGoal> goals) {
        List<Map<String, Object>> years = new ArrayList<>();
        for (YearGoal goal : goals) {
            Map<String, Object> year = new HashMap<>();
            year.put("yearNumber", goal.getYearNumber());
            year.put("goalTitle", goal.getGoalTitle());
            year.put("goalDescription", goal.getGoalDescription());
            year.put("goal", goal.getGoalTitle() + " - " + goal.getGoalDescription());
            year.put("progressPercent", goal.getProgressPercent());
            year.put("status", goal.getStatus());
            year.put("notes", loadNotesForYear(goal.getPlanId(), goal.getYearNumber()));
            year.put("uploadedFiles", listUploadedFiles(goal.getPlanId(), goal.getYearNumber()));
            years.add(year);
        }
        return years;
    }

    private String loadNotesForYear(Long planId, Integer yearNumber) {
        Path noteFile = PLAN_NOTES_DIR.resolve(String.valueOf(planId)).resolve(yearNumber + ".txt");
        try {
            if (Files.exists(noteFile)) {
                return Files.readString(noteFile, StandardCharsets.UTF_8);
            }
        } catch (IOException ignored) {
        }
        return "";
    }

    private void saveNotesForYear(Long planId, int yearNumber, String notes) throws IOException {
        Path notesDir = PLAN_NOTES_DIR.resolve(String.valueOf(planId));
        Files.createDirectories(notesDir);
        Path noteFile = notesDir.resolve(yearNumber + ".txt");
        Files.writeString(noteFile, notes == null ? "" : notes, StandardCharsets.UTF_8);
    }

    private List<Map<String, String>> listUploadedFiles(Long planId, Integer yearNumber) {
        List<Map<String, String>> files = new ArrayList<>();
        Path dir = PLAN_FILES_DIR.resolve(String.valueOf(planId)).resolve(String.valueOf(yearNumber));
        if (!Files.exists(dir)) {
            return files;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path child : stream) {
                if (Files.isRegularFile(child)) {
                    String filename = child.getFileName().toString();
                    files.add(Map.of(
                            "fileName", filename,
                            "fileUrl", "/uploads/plan-files/" + planId + "/" + yearNumber + "/" + filename));
                }
            }
        } catch (IOException ignored) {
        }
        return files;
    }

    private List<Map<String, String>> readTimelineForPlan(Long planId) {
        Path timelineFile = PLAN_TIMELINE_DIR.resolve(planId + ".json");
        if (!Files.exists(timelineFile)) {
            return new ArrayList<>();
        }

        try {
            return OBJECT_MAPPER.readValue(timelineFile.toFile(), new TypeReference<List<Map<String, String>>>() {
            });
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    private void appendTimelineEvent(Long planId, String description) {
        try {
            Path timelineFile = PLAN_TIMELINE_DIR.resolve(planId + ".json");
            Files.createDirectories(timelineFile.getParent());
            List<Map<String, String>> events = readTimelineForPlan(planId);
            events.add(Map.of(
                    "timestamp", LocalDateTime.now().toString(),
                    "description", description));
            OBJECT_MAPPER.writeValue(timelineFile.toFile(), events);
        } catch (IOException ignored) {
        }
    }

    private Long getSessionUserId(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object userObject = session.getAttribute("user");
        if (!(userObject instanceof User)) {
            return null;
        }
        return ((User) userObject).getId();
    }

    private Optional<FiveYearPlan> getAuthorizedPlan(Long planId, HttpSession session) {
        if (planId == null) {
            return Optional.empty();
        }
        Optional<FiveYearPlan> planOpt = fiveYearPlanRepository.findById(planId);
        if (planOpt.isEmpty() || session == null) {
            return Optional.empty();
        }
        Object userObject = session.getAttribute("user");
        if (!(userObject instanceof User)) {
            return Optional.empty();
        }
        User user = (User) userObject;
        return planOpt.filter(plan -> plan.getUserId().equals(user.getId()));
    }
}
