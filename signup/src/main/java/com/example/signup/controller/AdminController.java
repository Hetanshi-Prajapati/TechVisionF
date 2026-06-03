package com.example.signup.controller;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.Set;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.signup.entity.Post;
import com.example.signup.entity.PostReport;
import com.example.signup.entity.User;
import com.example.signup.entity.AppSettings;
import com.example.signup.repository.PostRepository;
import com.example.signup.repository.PostReportRepository;
import com.example.signup.repository.UserRepository;
import com.example.signup.repository.AppSettingsRepository;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:8080", allowCredentials = "true")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private AppSettingsRepository appSettingsRepository;

    @Autowired
    private PostReportRepository postReportRepository;

    private boolean isAdmin(HttpSession session) {
        User u = (User) session.getAttribute("user");
        return u != null && u.isAdmin();
    }

    // â”€â”€â”€ REPORT ENDPOINTS â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /** Any logged-in user: submit a report on a post */
    @PostMapping("/reports")
    public ResponseEntity<?> submitReport(@RequestBody Map<String, Object> body, HttpSession session) {
        User me = (User) session.getAttribute("user");
        if (me == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Not logged in"));
        }
        Long postId;
        try {
            postId = Long.valueOf(body.get("postId").toString());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid postId"));
        }
        String reasonStr = String.valueOf(body.getOrDefault("reason", "OTHER")).toUpperCase();
        PostReport.Reason reason;
        try {
            reason = PostReport.Reason.valueOf(reasonStr);
        } catch (IllegalArgumentException e) {
            reason = PostReport.Reason.OTHER;
        }
        // Duplicate check
        if (postReportRepository.existsByPostIdAndReporterId(postId, me.getId())) {
            return ResponseEntity.status(409).body(Map.of("message", "Already reported"));
        }
        Post post = postRepository.findById(postId).orElse(null);
        if (post == null || post.isDeleted()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Post not found"));
        }
        PostReport report = new PostReport();
        report.setPostId(postId);
        report.setReporterId(me.getId());
        report.setReporterUsername(me.getUsername());
        report.setPostAuthor(post.getAuthor());
        String snippet = post.getContent() != null && post.getContent().length() > 200
                ? post.getContent().substring(0, 200) + "..."
                : post.getContent();
        report.setPostContent(snippet);
        report.setReason(reason);
        report.setStatus(PostReport.Status.PENDING);
        report.setCreatedAt(LocalDateTime.now());
        postReportRepository.save(report);
        return ResponseEntity.ok(Map.of("message", "Report submitted"));
    }

    /** Admin: get all reports */
    @GetMapping("/reports")
    public ResponseEntity<?> getReports(@RequestParam(value = "status", defaultValue = "all") String status,
            HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
        List<PostReport> reports;
        if ("pending".equalsIgnoreCase(status)) {
            reports = postReportRepository.findByStatusOrderByCreatedAtDesc(PostReport.Status.PENDING);
        } else if ("ignored".equalsIgnoreCase(status)) {
            reports = postReportRepository.findByStatusOrderByCreatedAtDesc(PostReport.Status.IGNORED);
        } else if ("removed".equalsIgnoreCase(status)) {
            reports = postReportRepository.findByStatusOrderByCreatedAtDesc(PostReport.Status.REMOVED);
        } else {
            reports = postReportRepository.findAllByOrderByCreatedAtDesc();
        }
        long pendingCount = postReportRepository.countByStatus(PostReport.Status.PENDING);
        return ResponseEntity.ok(Map.of("reports", reports, "pendingCount", pendingCount));
    }

    /** Admin: ignore a report */
    @PutMapping("/reports/{id}/ignore")
    public ResponseEntity<?> ignoreReport(@PathVariable("id") Long id, HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
        PostReport report = postReportRepository.findById(id).orElse(null);
        if (report == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Report not found"));
        }
        report.setStatus(PostReport.Status.IGNORED);
        postReportRepository.save(report);
        return ResponseEntity.ok(Map.of("status", "ok", "message", "Report ignored"));
    }

    /** Admin: remove post via report (soft-delete post + mark report as REMOVED) */
    @PutMapping("/reports/{id}/remove")
    public ResponseEntity<?> removePostViaReport(@PathVariable("id") Long id, HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
        PostReport report = postReportRepository.findById(id).orElse(null);
        if (report == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Report not found"));
        }
        // Soft-delete the post
        Post post = postRepository.findById(report.getPostId()).orElse(null);
        if (post != null && !post.isDeleted()) {
            post.setDeleted(true);
            postRepository.save(post);
        }
        // Also update all reports for this post to REMOVED
        List<PostReport> allForPost = postReportRepository.findAllByOrderByCreatedAtDesc()
                .stream().filter(r -> r.getPostId().equals(report.getPostId())).toList();
        for (PostReport r : allForPost) {
            r.setStatus(PostReport.Status.REMOVED);
            postReportRepository.save(r);
        }
        return ResponseEntity.ok(Map.of("status", "ok", "message", "Post removed"));
    }

    /** Admin Dashboard: Returns JSON data for the admin panel */
    @GetMapping("")
    public ResponseEntity<?> adminPage(HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access Denied", "message",
                    "You do not have permission to access the admin panel"));
        }

        // Get dashboard stats
        List<User> allUsers = userRepository.findAll();
        List<Post> allPosts = postRepository.findAll();
        long pendingReports = postReportRepository.countByStatus(PostReport.Status.PENDING);
        long activeUsers = allUsers.stream().filter(u -> !u.isAdmin()).count();

        // Get app mode
        AppSettings modeSetting = appSettingsRepository.findBySettingKey("app_mode").orElse(null);
        String appMode = (modeSetting != null) ? modeSetting.getSettingValue() : "PRODUCTION";

        return ResponseEntity.ok(Map.of(
                "dashboard", Map.of(
                        "totalUsers", allUsers.size(),
                        "activeUsers", activeUsers,
                        "totalPosts", allPosts.size(),
                        "pendingReports", pendingReports,
                        "appMode", appMode),
                "message", "Admin dashboard data retrieved successfully"));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard(HttpSession session) {
        return adminPage(session);
    }

    @GetMapping("/users")
    public ResponseEntity<?> allUsers(@RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "q", required = false) String q,
            HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size,
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "id"));
        org.springframework.data.domain.Page<User> p;
        String query = q == null ? "" : q.trim();
        if (!query.isEmpty()) {
            p = userRepository.findByUsernameContainingIgnoreCase(query, pageable);
        } else {
            p = userRepository.findAll(pageable);
        }
        return ResponseEntity.ok(Map.of(
                "users", p.getContent(),
                "page", p.getNumber(),
                "size", p.getSize(),
                "totalElements", p.getTotalElements(),
                "totalPages", p.getTotalPages()));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable("id") Long id, HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
        if (id != null) {
            userRepository.deleteById(id);
        }
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> setAdminRole(@PathVariable("id") Long id, @RequestBody Map<String, Object> body,
            HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
        User u = userRepository.findById(id).orElse(null);
        if (u == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "User not found"));
        }
        boolean makeAdmin = Boolean.TRUE.equals(body.get("isAdmin"));
        u.setAdmin(makeAdmin);
        userRepository.save(u);
        return ResponseEntity.ok(Map.of("id", u.getId(), "isAdmin", u.isAdmin()));
    }

    @GetMapping("/posts")
    public ResponseEntity<?> allPosts(@RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "status", defaultValue = "all") String status,
            HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size,
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC,
                        "createdAt"));
        org.springframework.data.domain.Page<Post> p;
        String query = q == null ? "" : q.trim();
        if ("deleted".equalsIgnoreCase(status)) {
            p = query.isEmpty() ? postRepository.findByDeletedTrue(pageable)
                    : postRepository.findByDeletedTrueAndContentContainingIgnoreCase(query, pageable);
        } else if ("active".equalsIgnoreCase(status)) {
            p = query.isEmpty() ? postRepository.findByDeletedFalse(pageable)
                    : postRepository.findByDeletedFalseAndContentContainingIgnoreCase(query, pageable);
        } else {
            p = query.isEmpty() ? postRepository.findAll(pageable)
                    : postRepository.findByContentContainingIgnoreCase(query, pageable);
        }
        return ResponseEntity.ok(Map.of(
                "posts", p.getContent(),
                "page", p.getNumber(),
                "size", p.getSize(),
                "totalElements", p.getTotalElements(),
                "totalPages", p.getTotalPages()));
    }

    @GetMapping("/posts/{id}")
    public ResponseEntity<?> getPost(@PathVariable("id") Long id, HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
        Post p = postRepository.findById(id).orElse(null);
        if (p == null) {
            return ResponseEntity.status(404).body(Map.of("message", "Post not found"));
        }
        return ResponseEntity.ok(p);
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<?> deletePost(@PathVariable("id") Long id, HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
        Post p = postRepository.findById(id).orElse(null);
        if (p != null) {
            p.setDeleted(true);
            postRepository.save(p);
        }
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PutMapping("/posts/{id}/restore")
    public ResponseEntity<?> restorePost(@PathVariable("id") Long id, HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
        Post p = postRepository.findById(id).orElse(null);
        if (p == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Post not found"));
        }
        p.setDeleted(false);
        postRepository.save(p);
        return ResponseEntity.ok(Map.of("status", "ok", "message", "Post restored successfully"));
    }

    @GetMapping("/stats")
    public ResponseEntity<?> stats(@RequestParam(value = "days", defaultValue = "7") int days, HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
        int windowDays = Math.max(1, Math.min(days, 90));
        List<User> users = userRepository.findAll();
        List<Post> posts = postRepository.findAll().stream().filter(pp -> !pp.isDeleted()).toList();

        int totalUsers = users.size();
        int totalPosts = posts.size();

        LocalDateTime now = LocalDateTime.now();
        int newPosts24h = (int) posts.stream()
                .filter(p -> p.getCreatedAt() != null && p.getCreatedAt().isAfter(now.minusHours(24))).count();

        Set<Long> activeUserIdsWindow = new HashSet<>();
        posts.stream().filter(p -> p.getCreatedAt() != null && p.getCreatedAt().isAfter(now.minusDays(windowDays)))
                .forEach(p -> {
                    if (p.getAuthorId() != null)
                        activeUserIdsWindow.add(p.getAuthorId());
                });
        int activeUsersWindow = activeUserIdsWindow.size();

        Map<String, Object> activity = new LinkedHashMap<>();
        String[] labels = new String[windowDays];
        int[] postsPerDay = new int[windowDays];
        int[] activeUsersPerDay = new int[windowDays];
        for (int i = windowDays - 1; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays((windowDays - 1) - i);
            labels[i] = day.toString();
            Set<Long> dayAuthors = new HashSet<>();
            int countPosts = 0;
            for (Post p : posts) {
                if (p.getCreatedAt() != null && p.getCreatedAt().toLocalDate().equals(day)) {
                    countPosts++;
                    if (p.getAuthorId() != null)
                        dayAuthors.add(p.getAuthorId());
                }
            }
            postsPerDay[i] = countPosts;
            activeUsersPerDay[i] = dayAuthors.size();
        }
        activity.put("labels", labels);
        activity.put("postsPerDay", postsPerDay);
        activity.put("activeUsersPerDay", activeUsersPerDay);

        return ResponseEntity.ok(Map.of(
                "totalUsers", totalUsers,
                "totalPosts", totalPosts,
                "activeUsersWindow", activeUsersWindow,
                "newPosts24h", newPosts24h,
                "activity", activity));
    }

    @GetMapping("/mode")
    public ResponseEntity<?> getAppMode(HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
        AppSettings setting = appSettingsRepository.findBySettingKey("app_mode").orElse(null);
        String mode = (setting != null) ? setting.getSettingValue() : "PRODUCTION";
        return ResponseEntity.ok(Map.of("mode", mode));
    }

    @PostMapping("/mode")
    public ResponseEntity<?> setAppMode(@RequestBody Map<String, String> body, HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
        String mode = body.getOrDefault("mode", "PRODUCTION").toUpperCase();
        if (!mode.equals("TEST") && !mode.equals("PRODUCTION")) {
            mode = "PRODUCTION";
        }
        AppSettings setting = appSettingsRepository.findBySettingKey("app_mode")
                .orElse(new AppSettings("app_mode", mode));
        setting.setSettingValue(mode);
        appSettingsRepository.save(setting);
        return ResponseEntity.ok(Map.of("mode", mode, "message", "Mode updated successfully"));
    }
}
