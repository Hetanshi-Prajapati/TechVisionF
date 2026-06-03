package com.example.signup.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.Comparator;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.signup.entity.Post;
import com.example.signup.entity.User;
import com.example.signup.repository.PostRepository;
import com.example.signup.repository.UserRepository;

@Controller
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:8080", allowCredentials = "true")
public class SearchController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostRepository postRepository;

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam("q") String q) {
        String query = q == null ? "" : q.trim();
        if (query.isEmpty()) {
            return ResponseEntity.ok(Map.of("users", List.of(), "posts", List.of()));
        }
        String normalizedQuery = query.toLowerCase(Locale.ROOT);
        List<User> byUsername = userRepository.findByUsernameContainingIgnoreCase(query);
        List<User> byFullName = userRepository.findByFullNameContainingIgnoreCase(query);
        Map<Long, User> dedupedUsers = new LinkedHashMap<>();
        for (User u : byUsername) {
            dedupedUsers.put(u.getId(), u);
        }
        for (User u : byFullName) {
            dedupedUsers.put(u.getId(), u);
        }

        List<Map<String, Object>> usersOut = dedupedUsers.values().stream()
                .sorted(Comparator
                        .comparingInt((User u) -> userScore(u, normalizedQuery)).reversed()
                        .thenComparing(User::getUsername, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .map(u -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", u.getId());
                    m.put("username", u.getUsername());
                    m.put("fullName", u.getFullName());
                    return m;
                })
                .collect(Collectors.toList());

        List<Post> postMatches = postRepository.findByContentContainingIgnoreCaseOrCodeContainingIgnoreCaseAndDeletedFalse(query);
        List<Map<String, Object>> postsOut = postMatches.stream()
        .sorted(Comparator
                .comparingDouble((Post p) -> postScore(p, normalizedQuery)).reversed()
                .thenComparing(Post::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(Post::getLikeCount, Comparator.nullsLast(Comparator.reverseOrder())))
        .map(p -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", p.getId());
            m.put("authorId", p.getAuthorId());
            m.put("author", p.getAuthor());
            String content = p.getContent() == null ? "" : p.getContent();
            String preview = content.length() > 140 ? content.substring(0, 140) + "…" : content;
            m.put("content", preview);
            m.put("hasCode", p.getCode() != null && !p.getCode().isBlank());
            m.put("likeCount", p.getLikeCount());
            m.put("createdAt", p.getCreatedAt());
            return m;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("users", usersOut, "posts", postsOut));
    }

    private int userScore(User user, String query) {
        String username = normalize(user.getUsername());
        String fullName = normalize(user.getFullName());
        int score = 0;

        if (username.equals(query)) {
            score += 100;
        } else if (username.startsWith(query)) {
            score += 70;
        } else if (username.contains(query)) {
            score += 40;
        }

        if (fullName.equals(query)) {
            score += 80;
        } else if (fullName.startsWith(query)) {
            score += 50;
        } else if (fullName.contains(query)) {
            score += 25;
        }

        score += 3 * countOccurrences(username, query);
        score += 2 * countOccurrences(fullName, query);
        return score;
    }

    private double postScore(Post post, String query) {
        String content = normalize(post.getContent());
        String code = normalize(post.getCode());
        String combined = content + " " + code;

        double score = 0;
        if (content.equals(query) || code.equals(query)) {
            score += 100;
        } else if (content.startsWith(query) || code.startsWith(query)) {
            score += 70;
        } else if (combined.contains(query)) {
            score += 35;
        }

        score += 6 * countOccurrences(content, query);
        score += 4 * countOccurrences(code, query);

        Integer likeCount = post.getLikeCount();
        if (likeCount != null && likeCount > 0) {
            score += Math.log1p(likeCount);
        }

        return score;
    }

    private int countOccurrences(String text, String query) {
        if (text.isEmpty() || query.isEmpty()) {
            return 0;
        }

        int count = 0;
        int start = 0;
        while (true) {
            int idx = text.indexOf(query, start);
            if (idx < 0) {
                return count;
            }
            count++;
            start = idx + query.length();
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).trim();
    }
}
