package com.example.signup.controller;

import java.util.*;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;

import com.example.signup.entity.*;
import com.example.signup.repository.*;
import com.example.signup.service.ProfileImageService;
import com.example.signup.dto.SignupRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:8080", allowCredentials = "true")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostReportRepository postReportRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private ProfileImageService profileImageService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ================= UTIL =================
    private String getProfilePicOrDefault(User u) {
        if (u.getProfilePic() != null && !u.getProfilePic().trim().isEmpty()) {
            return u.getProfilePic();
        }
        return "https://api.dicebear.com/7.x/adventurer/svg?seed=" + u.getUsername();
    }

    private Map<String, Object> buildLoginResponse(User user, String sessionToken) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", user.getId());
        body.put("username", user.getUsername());
        body.put("email", user.getEmail());
        body.put("fullName", user.getFullName() == null ? "" : user.getFullName());
        body.put("bio", user.getBio() == null ? "" : user.getBio());
        body.put("profilePic", user.getProfilePic() == null ? "" : user.getProfilePic());
        body.put("primarySkill", user.getPrimarySkill() == null ? "" : user.getPrimarySkill());
        body.put("githubUsername", user.getGithubUsername() == null ? "" : user.getGithubUsername());
        body.put("isAdmin", user.isAdmin());
        body.put("role", user.isAdmin() ? "ADMIN" : "USER");
        body.put("token", sessionToken);
        body.put("tokenType", "Bearer");
        body.put("authMode", "SESSION");
        body.put("hasRememberToken", user.getRememberToken() != null);
        return body;
    }

    // ================= PAGE ROUTES =================

    @GetMapping("/login")
    public String loginPage() {
        return "Login";
    }

    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }

    @GetMapping("/oauth2/start/{provider}")
    public String oauth2Start(@PathVariable String provider) {
        String normalizedProvider = provider == null ? "" : provider.trim().toLowerCase();
        if (!normalizedProvider.equals("google") && !normalizedProvider.equals("github")) {
            return "redirect:/api/auth/login";
        }
        return "redirect:/oauth2/authorization/" + normalizedProvider;
    }

    @GetMapping("/terms")
    public String termsPage() {
        return "Terms";
    }

    @GetMapping("/privacy")
    public String privacyPage() {
        return "Privacy";
    }

    @GetMapping("/about")
    public String aboutPage() {
        return "About";
    }

    @GetMapping("/contact")
    public String contactPage() {
        return "Contact";
    }

    @GetMapping("/reset")
    public String resetPage() {
        return "Reset";
    }

    @GetMapping("/home")
    public String home(HttpSession session) {
        return (session.getAttribute("user") == null)
                ? "redirect:/api/auth/login"
                : "Home";
    }

    @GetMapping("/profile")
    public String profile(HttpSession session) {
        return (session.getAttribute("user") == null)
                ? "redirect:/api/auth/login"
                : "Profile";
    }

    @GetMapping("/explore")
    public String explore(HttpSession session) {
        return (session.getAttribute("user") == null)
                ? "redirect:/api/auth/login"
                : "Explore";
    }

    @GetMapping("/search")
    public String search(HttpSession session) {
        return (session.getAttribute("user") == null)
                ? "redirect:/api/auth/login"
                : "Search";
    }

    @GetMapping("/settings")
    public String settings(HttpSession session) {
        return (session.getAttribute("user") == null)
                ? "redirect:/api/auth/login"
                : "Settings";
    }

    @GetMapping("/complete-profile")
    public String completeProfilePage(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null)
            return "redirect:/api/auth/login";
        if (user.isProfileComplete())
            return "redirect:/api/auth/home";
        return "CompleteProfile";
    }

    @PostMapping("/complete-profile")
    public ResponseEntity<?> completeProfile(@RequestBody Map<String, String> body, HttpSession session) {
        User me = (User) session.getAttribute("user");
        if (me == null)
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));

        User user = userRepository.findById(me.getId()).orElse(null);
        if (user == null)
            return ResponseEntity.badRequest().body(Map.of("message", "User not found"));

        String username = body.get("username");
        String primarySkill = body.get("primarySkill");
        String githubUsername = body.get("githubUsername");
        String fullName = body.get("fullName");

        if (username == null || username.trim().length() < 3) {
            return ResponseEntity.badRequest().body(Map.of("message", "Username must be at least 3 characters"));
        }
        username = username.trim().toLowerCase().replaceAll("[^a-z0-9_]", "");
        if (!username.equals(user.getUsername()) && userRepository.existsByUsername(username)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Username already taken"));
        }

        if (fullName != null && !fullName.isBlank())
            user.setFullName(fullName.trim());
        user.setUsername(username);
        if (primarySkill != null && !primarySkill.isBlank())
            user.setPrimarySkill(primarySkill.trim());
        if (githubUsername != null && !githubUsername.isBlank())
            user.setGithubUsername(githubUsername.trim());
        user.setProfileComplete(true);

        userRepository.save(user);

        boolean logoutAfterComplete = Boolean.parseBoolean(body.getOrDefault("logoutAfterComplete", "false"));
        if (logoutAfterComplete) {
            session.invalidate();
            return ResponseEntity.ok(Map.of("status", "ok", "next", "/api/auth/login"));
        }

        session.setAttribute("user", user);
        return ResponseEntity.ok(Map.of("status", "ok", "next", "/api/auth/home"));
    }

    // ================= SIGNUP =================
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest data,
            BindingResult bindingResult,
            HttpServletRequest request,
            HttpSession session) {

        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Validation failed",
                    "errors", errors));
        }

        String fullName = data.getFullName().trim();
        String username = data.getUsername().trim().toLowerCase();
        String email = data.getEmail().trim().toLowerCase();
        String password = data.getPassword();

        // Preserve old behavior while guarding against sanitized empties.
        username = username.replaceAll("[^a-z0-9_]", "");
        if (username.length() < 3) {
            return ResponseEntity.badRequest().body(Map.of("message", "Username must be at least 3 characters"));
        }

        System.out.println("Processing signup for: " + username + " (" + email + ")");

        if (userRepository.existsByEmail(email)) {
            System.out.println("Signup failed: Email already registered (" + email + ")");
            return ResponseEntity.badRequest().body(Map.of("message", "Email already registered"));
        }

        if (userRepository.existsByUsername(username)) {
            System.out.println("Signup failed: Username already taken (" + username + ")");
            return ResponseEntity.badRequest().body(Map.of("message", "Username already taken"));
        }

        try {
            User user = new User();
            user.setFullName(fullName);
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setPrimarySkill(data.getPrimarySkill());
            user.setGithubUsername(data.getGithubUsername());

                userRepository.save(user);
                System.out.println("User saved successfully: " + username);

                // Signup should not auto-login. Ensure no authenticated session is carried over.
                if (session != null) {
                session.invalidate();
                }

            return ResponseEntity.ok(Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "email", user.getEmail(),
                    "message", "Signup successful. Please login."));
        } catch (Exception e) {
            System.err.println("Signup Exception: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Database error while creating account"));
        }
    }

    // ================= LOGIN (SESSION BASED) =================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> data,
            HttpServletRequest request,
            HttpSession session) {

        String identifier = data.get("loginIdentifier");
        if (identifier == null)
            identifier = data.get("email");

        String password = data.get("password");

        if (identifier == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid credentials"));
        }

        identifier = identifier.trim().toLowerCase();

        User user = userRepository.findByUsernameOrEmail(identifier, identifier).orElse(null);

        // if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
        // return ResponseEntity.badRequest().body(Map.of("message", "Invalid
        // credentials"));
        // }
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid credentials"));
        }

        boolean passwordMatches = false;

        // ✅ Check if BCrypt
        if (user.getPassword() != null && (user.getPassword().startsWith("$2a$") ||
                user.getPassword().startsWith("$2b$") ||
                user.getPassword().startsWith("$2y$"))) {

            passwordMatches = passwordEncoder.matches(password, user.getPassword());
        } else {
            // ✅ Plain text case
            passwordMatches = password.equals(user.getPassword());

            // 🔥 Convert to BCrypt if matches (IMPORTANT)
            if (passwordMatches) {
                String encoded = passwordEncoder.encode(password);
                user.setPassword(encoded);
                userRepository.save(user);
            }
        }

        if (!passwordMatches) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid credentials"));
        }

        session.invalidate(); // clear old session
        HttpSession newSession = request.getSession(true);
        newSession.setAttribute("user", user);
        String sessionToken = UUID.randomUUID().toString();
        newSession.setAttribute("sessionToken", sessionToken);

        if (Boolean.parseBoolean(data.get("rememberMe"))) {
            String token = UUID.randomUUID().toString();
            user.setRememberToken(token);
            user.setRememberTokenExpiry(LocalDateTime.now().plusDays(30));
            userRepository.save(user);

            ResponseCookie cookie = ResponseCookie.from("remember_me", token)
                    .httpOnly(true)
                    .secure(request.isSecure())
                    .path("/")
                    .maxAge(30L * 24 * 60 * 60)
                    .sameSite("Lax")
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(buildLoginResponse(user, sessionToken));
        }

        return ResponseEntity.ok(buildLoginResponse(user, sessionToken));
    }

    // ================= LOGOUT =================
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session, HttpServletResponse response) {
        if (session != null) {
            session.invalidate();
        }
        ResponseCookie cookie = ResponseCookie.from("remember_me", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .build();
        // also clear the session cookie (JSESSIONID) by setting it empty
        ResponseCookie jsess = ResponseCookie.from("JSESSIONID", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .header(HttpHeaders.SET_COOKIE, jsess.toString())
                .body(Map.of("status", "logged out"));
    }

    // ================= REMEMBER ME / CHECK SESSION =================
    @GetMapping("/remember")
    public ResponseEntity<?> remember(HttpServletRequest request, HttpSession session) {
        User u = (User) session.getAttribute("user");
        if (u != null) {
            return ResponseEntity.ok(Map.of("id", u.getId(), "username", u.getUsername(), "email", u.getEmail()));
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("remember_me".equals(c.getName())) {
                    String token = c.getValue();
                    User user = userRepository.findByRememberToken(token).orElse(null);
                    if (user != null && user.getRememberTokenExpiry() != null &&
                            user.getRememberTokenExpiry().isAfter(LocalDateTime.now())) {
                        session.setAttribute("user", user);
                        return ResponseEntity.ok(
                                Map.of("id", user.getId(), "username", user.getUsername(), "email", user.getEmail()));
                    }
                }
            }
        }
        return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
    }

    // ================= CURRENT USER =================
    @GetMapping("/users/me")
    public ResponseEntity<?> me(HttpServletRequest request, HttpSession session) {

        User user = (User) session.getAttribute("user");

        // ✅ Auto-restore from cookie if session is empty
        if (user == null) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie c : cookies) {
                    if ("remember_me".equals(c.getName())) {
                        String token = c.getValue();
                        user = userRepository.findByRememberToken(token).orElse(null);
                        if (user != null && user.getRememberTokenExpiry() != null &&
                                user.getRememberTokenExpiry().isAfter(LocalDateTime.now())) {
                            session.setAttribute("user", user);
                        } else {
                            user = null; // Token invalid or expired
                        }
                        break;
                    }
                }
            }
        }

        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }

        user = userRepository.findById(user.getId()).orElse(user);
        session.setAttribute("user", user);

        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("fullName", user.getFullName());
        data.put("email", user.getEmail());
        data.put("bio", user.getBio());
        data.put("githubUsername", user.getGithubUsername());
        data.put("primarySkill", user.getPrimarySkill());
        data.put("profilePic", getProfilePicOrDefault(user));
        data.put("followers", followRepository.countByFollowingId(user.getId()));
        data.put("following", followRepository.countByFollowerId(user.getId()));
        data.put("postCount", postRepository.countByAuthorIdAndDeletedFalse(user.getId()));
        data.put("isAdmin", user.isAdmin());
        data.put("hasRememberToken", user.getRememberToken() != null);

        return ResponseEntity.ok(data);
    }

    @Transactional
    @PutMapping("/users/me")
    public ResponseEntity<?> update(@RequestBody Map<String, String> body,
            HttpSession session) {

        User me = (User) session.getAttribute("user");

        if (me == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }

        User user = userRepository.findById(me.getId()).orElse(null);

        String oldUsername = user.getUsername();
        boolean usernameChanged = false;

        if (body.containsKey("fullName"))
            user.setFullName(body.get("fullName"));
        if (body.containsKey("username")) {
            String newUsername = body.get("username").trim().toLowerCase();
            if (!newUsername.isEmpty() && !newUsername.equals(oldUsername)) {
                user.setUsername(newUsername);
                usernameChanged = true;
            }
        }
        if (body.containsKey("bio"))
            user.setBio(body.get("bio"));
        if (body.containsKey("githubUsername"))
            user.setGithubUsername(body.get("githubUsername"));
        if (body.containsKey("primarySkill"))
            user.setPrimarySkill(body.get("primarySkill"));
        if (body.containsKey("profilePic"))
            user.setProfilePic(body.get("profilePic"));

        userRepository.saveAndFlush(user);

        if (usernameChanged) {
            System.out.println("[SYNC DEBUG] Username changed: " + oldUsername + " -> " + user.getUsername());
            log.info("Username changed from {} to {}. Synchronizing all posts and reports.", oldUsername,
                    user.getUsername());

            postRepository.updateAuthorUsername(user.getId(), user.getUsername());
            postReportRepository.updateReporterUsername(user.getId(), user.getUsername());
            postReportRepository.updatePostAuthorUsername(user.getId(), user.getUsername());

            System.out.println("[SYNC DEBUG] Synchronization commands sent to database.");
        }

        session.setAttribute("user", user); // Update session

        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "fullName", user.getFullName() == null ? "" : user.getFullName(),
                "bio", user.getBio() == null ? "" : user.getBio(),
                "profilePic", user.getProfilePic() == null ? "" : user.getProfilePic(),
                "primarySkill", user.getPrimarySkill() == null ? "" : user.getPrimarySkill(),
                "githubUsername", user.getGithubUsername() == null ? "" : user.getGithubUsername(),
                "isAdmin", user.isAdmin(),
                "hasRememberToken", user.getRememberToken() != null));
    }

    @PostMapping("/users/me/revoke-remember")
    public ResponseEntity<?> revokeRemember(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }

        user.setRememberToken(null);
        user.setRememberTokenExpiry(null);
        userRepository.save(user);

        ResponseCookie cookie = ResponseCookie.from("remember_me", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of("message", "Automatic login disabled"));
    }

    // ================= FOLLOW =================
    @Transactional
    @PostMapping("/users/{id}/follow")
    public ResponseEntity<?> follow(@PathVariable Long id, HttpSession session) {

        User me = (User) session.getAttribute("user");

        if (me == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }

        if (me.getId().equals(id)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Cannot follow yourself"));
        }

        if (followRepository.findByFollowerIdAndFollowingId(me.getId(), id).isEmpty()) {

            Follow f = new Follow();
            f.setFollowerId(me.getId());
            f.setFollowingId(id);
            f.setCreatedAt(LocalDateTime.now());
            followRepository.save(f);

            me.setFollowing(me.getFollowing() + 1);

            userRepository.findById(id).ifPresent(target -> {
                target.setFollowers(target.getFollowers() + 1);
                userRepository.save(target);
            });

            userRepository.save(me);
        }

        return ResponseEntity.ok(Map.of("status", "followed"));
    }

    // ================= UNFOLLOW =================
    @Transactional
    @DeleteMapping("/users/{id}/follow")
    public ResponseEntity<?> unfollow(@PathVariable Long id, HttpSession session) {

        User me = (User) session.getAttribute("user");

        if (me == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }

        followRepository.deleteByFollowerIdAndFollowingId(me.getId(), id);

        me.setFollowing(Math.max(0, me.getFollowing() - 1));

        userRepository.findById(id).ifPresent(target -> {
            target.setFollowers(Math.max(0, target.getFollowers() - 1));
            userRepository.save(target);
        });

        userRepository.save(me);

        return ResponseEntity.ok(Map.of("status", "unfollowed"));
    }

    // ================= FOLLOWING IDS =================
    @GetMapping("/users/me/followingIds")
    public ResponseEntity<?> followingIds(HttpSession session) {

        User me = (User) session.getAttribute("user");

        if (me == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }

        List<Follow> list = followRepository.findByFollowerId(me.getId());

        Set<Long> ids = new HashSet<>();
        for (Follow f : list) {
            ids.add(f.getFollowingId());
        }

        return ResponseEntity.ok(Map.of("ids", ids));
    }

    // ================= PROFILE PIC UPLOAD =================
    @PostMapping(value = "/users/me/profile-pic", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file,
            HttpSession session) {

        User me = (User) session.getAttribute("user");

        if (me == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }

        String path = profileImageService.saveProfileImage(me, file);
        session.setAttribute("user", me); // Update session to include new path

        return ResponseEntity.ok(Map.of("path", path));
    }

    // ================= GET USER BY ID =================
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "User not found"));
        }
        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("fullName", user.getFullName());
        data.put("email", user.getEmail());
        data.put("bio", user.getBio());
        data.put("githubUsername", user.getGithubUsername());
        data.put("primarySkill", user.getPrimarySkill());
        data.put("profilePic", getProfilePicOrDefault(user));
        data.put("followers", followRepository.countByFollowingId(user.getId()));
        data.put("following", followRepository.countByFollowerId(user.getId()));
        data.put("postCount", postRepository.countByAuthorIdAndDeletedFalse(user.getId()));
        return ResponseEntity.ok(data);
    }

    // ================= FOLLOWERS LIST =================
    @GetMapping("/users/{id}/followers")
    public ResponseEntity<?> followers(@PathVariable Long id, @RequestParam(required = false) Long cursor) {
        List<Follow> list;
        if (cursor == null) {
            list = followRepository.findTop10ByFollowingIdOrderByIdDesc(id);
        } else {
            list = followRepository.findTop10ByFollowingIdAndIdLessThanOrderByIdDesc(id, cursor);
        }

        List<Map<String, Object>> usersList = new ArrayList<>();
        Long nextCursor = null;

        for (Follow f : list) {
            userRepository.findById(f.getFollowerId()).ifPresent(u -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id", u.getId());
                m.put("username", u.getUsername());
                m.put("fullName", u.getFullName());
                m.put("profilePic", getProfilePicOrDefault(u));
                usersList.add(m);
            });
            nextCursor = f.getId();
        }

        boolean hasMore = list.size() == 10;
        return ResponseEntity.ok(Map.of(
                "users", usersList,
                "nextCursor", nextCursor != null ? nextCursor : null,
                "hasMore", hasMore,
                "count", followRepository.countByFollowingId(id)));
    }

    // ================= FOLLOWING LIST =================
    @GetMapping("/users/{id}/following")
    public ResponseEntity<?> following(@PathVariable Long id, @RequestParam(required = false) Long cursor) {
        List<Follow> list;
        if (cursor == null) {
            list = followRepository.findTop10ByFollowerIdOrderByIdDesc(id);
        } else {
            list = followRepository.findTop10ByFollowerIdAndIdLessThanOrderByIdDesc(id, cursor);
        }

        List<Map<String, Object>> usersList = new ArrayList<>();
        Long nextCursor = null;

        for (Follow f : list) {
            userRepository.findById(f.getFollowingId()).ifPresent(u -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id", u.getId());
                m.put("username", u.getUsername());
                m.put("fullName", u.getFullName());
                m.put("profilePic", getProfilePicOrDefault(u));
                usersList.add(m);
            });
            nextCursor = f.getId();
        }

        boolean hasMore = list.size() == 10;
        return ResponseEntity.ok(Map.of(
                "users", usersList,
                "nextCursor", nextCursor != null ? nextCursor : null,
                "hasMore", hasMore,
                "count", followRepository.countByFollowerId(id)));
    }

    // ================= FORGOT PASSWORD =================
    @PostMapping("/forgot")
    public ResponseEntity<?> forgot(@RequestBody Map<String, String> body) {

        String email = body.get("email");

        User u = userRepository.findByEmail(email).orElse(null);

        if (u == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email not found"));
        }

        String token = UUID.randomUUID().toString();

        u.setResetToken(token);
        u.setResetTokenExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(u);

        // // 📧 Simulate sending email in console
        // System.out.println("========================================");
        // System.out.println("📧 SIMULATED EMAIL SENT TO: " + email);
        // System.out.println("🔗 Reset Link:
        // http://localhost:8080/api/auth/reset?token=" + token);
        // System.out.println("========================================");

        // 📧 Send actual email
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("dalwadiaksh@gmail.com"); // Matches application.properties
            message.setTo(email);
            message.setSubject("Password Reset Request | DevNetwork");
            message.setText("Click the link below to reset your password:\n\n" +
                    "http://localhost:8080/api/auth/reset?token=" + token + "\n\n" +
                    "This link will expire in 15 minutes.");
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("❌ FAILED TO SEND EMAIL: " + e.getMessage());
            // Fallback for debugging
            System.out.println("🔗 Reset Link (Console Fallback): http://localhost:8080/api/auth/reset?token=" + token);
        }

        return ResponseEntity
                .ok(Map.of("message", "If this email is registered, a reset link has been sent to your inbox."));
    }

    // ================= RESET PASSWORD =================
    @PostMapping("/reset")
    public ResponseEntity<?> reset(@RequestBody Map<String, String> body) {

        String token = body.get("token");
        String newPassword = body.get("newPassword");

        User u = userRepository.findByResetToken(token).orElse(null);

        if (u == null || u.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid token"));
        }

        u.setPassword(passwordEncoder.encode(newPassword));
        u.setResetToken(null);
        u.setResetTokenExpiry(null);

        userRepository.save(u);

        return ResponseEntity.ok(Map.of("status", "reset done"));
    }
}