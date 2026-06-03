package com.example.signup.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import com.example.signup.entity.PlanTask;
import com.example.signup.entity.User;
import com.example.signup.repository.PlanTaskRepository;
import com.example.signup.repository.UserRepository;

@RestController
@RequestMapping("/api/plan")
public class PlanTaskController {

    @Autowired
    private PlanTaskRepository planTaskRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/task")
    public ResponseEntity<?> addTask(@RequestBody PlanTask task, @AuthenticationPrincipal String username) {
        if (username == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
        
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }

        task.setUserId(user.getId());

        if (task.getYear() == null && task.getWeek() != null) {
            task.setYear(task.getWeek());
        }
        if ((task.getTask() == null || task.getTask().isBlank()) && task.getTitle() != null && !task.getTitle().isBlank()) {
            task.setTask(task.getTitle());
        }
        if ((task.getDescription() == null || task.getDescription().isBlank()) && task.getTaskType() != null
                && "PROJECT".equalsIgnoreCase(task.getTaskType()) && task.getProofUrl() != null) {
            task.setDescription("Proof uploaded: " + task.getProofUrl());
        }

        PlanTask savedTask = planTaskRepository.save(task);
        return ResponseEntity.ok(savedTask);
    }

    @GetMapping("/tasks")
    public ResponseEntity<?> getTasks(@AuthenticationPrincipal String username, @RequestParam(required = false) Long userId) {
        if (username == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
        
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }

        List<PlanTask> tasks = planTaskRepository.findByUserId(user.getId());
        return ResponseEntity.ok(tasks);
    }

    @PutMapping("/task/{id}/complete")
    public ResponseEntity<?> toggleTaskComplete(@PathVariable Long id, @AuthenticationPrincipal String username) {
        if (username == null) return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));

        PlanTask task = planTaskRepository.findById(id).orElse(null);
        if (task == null || !task.getUserId().equals(user.getId())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Task not found"));
        }

        task.setCompleted(!task.isCompleted());
        PlanTask savedTask = planTaskRepository.save(task);
        return ResponseEntity.ok(savedTask);
    }

    @PutMapping("/task/{id}")
    public ResponseEntity<?> toggleTask(@PathVariable Long id, @RequestBody Map<String, Boolean> body, @AuthenticationPrincipal String username) {
        if (username == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
        
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }

        PlanTask task = planTaskRepository.findById(id).orElse(null);
        if (task == null || !task.getUserId().equals(user.getId())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Task not found"));
        }

        task.setCompleted(body.get("completed"));
        PlanTask savedTask = planTaskRepository.save(task);
        return ResponseEntity.ok(savedTask);
    }

    @DeleteMapping("/task/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Long id, @AuthenticationPrincipal String username) {
        if (username == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
        
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }

        PlanTask task = planTaskRepository.findById(id).orElse(null);
        if (task == null || !task.getUserId().equals(user.getId())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Task not found"));
        }

        planTaskRepository.delete(task);
        return ResponseEntity.ok(Map.of("message", "Deleted"));
    }

    @PostMapping("/quiz")
    public ResponseEntity<?> saveQuizResult(@RequestBody Map<String, Object> body, @AuthenticationPrincipal String username) {
        if (username == null) return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));

        Integer week = (Integer) body.get("week");
        Integer score = (Integer) body.get("score");
        String domain = (String) body.get("domain");
        String language = (String) body.get("language");
        String quizQuestion = (String) body.get("quizQuestion");
        String optionA = (String) body.get("optionA");
        String optionB = (String) body.get("optionB");
        String optionC = (String) body.get("optionC");
        String optionD = (String) body.get("optionD");

        PlanTask task = new PlanTask();
        task.setUserId(user.getId());
        task.setYear(week);
        task.setWeek(week);
        task.setDomain(domain);
        task.setLanguage(language);
        task.setTaskType("QUIZ");
        task.setQuizScore(score);
        task.setTitle("Quiz Result - Week " + week);
        task.setTask("Quiz Result - Week " + week);
        task.setDescription(score != null ? "Score: " + score + "%" : "Quiz submitted");
        task.setQuizQuestion(quizQuestion);
        task.setOptionA(optionA);
        task.setOptionB(optionB);
        task.setOptionC(optionC);
        task.setOptionD(optionD);
        task.setCompleted(score != null && score >= 60);
        planTaskRepository.save(task);
        
        return ResponseEntity.ok(task);
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file, @AuthenticationPrincipal String username) {
        if (username == null) return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        
        try {
            Path uploadDir = Paths.get("uploads", "plan");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            
            String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            String fileUrl = "/uploads/plan/" + filename;
            System.out.println("File uploaded successfully: " + filePath.toAbsolutePath().toString());
            return ResponseEntity.ok(Map.of("fileUrl", fileUrl));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Upload error: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("message", "Failed to upload file: " + e.getMessage()));
        }
    }

    @PostMapping("/link")
    public ResponseEntity<?> saveLink(@RequestBody Map<String, Object> body, @AuthenticationPrincipal String username) {
        if (username == null) return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));

        Long taskId = Long.valueOf(body.get("taskId").toString());
        String url = (String) body.get("url");

        PlanTask task = planTaskRepository.findById(taskId).orElse(null);
        if (task == null || !task.getUserId().equals(user.getId())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Task not found"));
        }

        task.setProofUrl(url);
        planTaskRepository.save(task);
        
        return ResponseEntity.ok(task);
    }
}
