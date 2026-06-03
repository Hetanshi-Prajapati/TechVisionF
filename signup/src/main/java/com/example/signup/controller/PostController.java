package com.example.signup.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.signup.entity.Follow;
import com.example.signup.entity.Dislike;
import com.example.signup.entity.Like;
import com.example.signup.entity.Comment;
import com.example.signup.entity.Post;
import com.example.signup.entity.SavedPost;
import com.example.signup.entity.User;
import com.example.signup.repository.FollowRepository;
import com.example.signup.repository.DislikeRepository;
import com.example.signup.repository.LikeRepository;
import com.example.signup.repository.CommentRepository;
import com.example.signup.repository.PostRepository;
import com.example.signup.repository.SavedPostRepository;
import com.example.signup.repository.UserRepository;
import com.example.signup.service.AIContentValidatorService;
import com.example.signup.service.PostImageService;
import com.example.signup.repository.AppSettingsRepository;
import com.example.signup.entity.AppSettings;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/api/posts")
@CrossOrigin(origins = "http://localhost:8080", allowCredentials = "true")
public class PostController {

    private static final Logger log = LoggerFactory.getLogger(PostController.class);

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostImageService postImageService;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SavedPostRepository savedPostRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private DislikeRepository dislikeRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private AppSettingsRepository appSettingsRepository;

    @Autowired
    private AIContentValidatorService aiService;

    @GetMapping("/explore")
    public ResponseEntity<?> explore(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String category) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        if (category != null && !category.isEmpty() && !category.equalsIgnoreCase("all")) {
            return ResponseEntity.ok(
                    Map.of("posts", postRepository.findByCategoryAndDeletedFalse(category.toLowerCase(), pageable)
                            .getContent()));
        }

        return ResponseEntity.ok(
                Map.of("posts", postRepository.findExplorePosts(pageable).getContent()));
    }
    // public ResponseEntity<?> explore(@RequestParam(value = "page", defaultValue =
    // "0") int page,
    // @RequestParam(value = "size", defaultValue = "20") int size,
    // HttpSession session) {

    // User user = (User) session.getAttribute("user");
    // Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC,
    // "createdAt"));

    // // Let's log if there are ANY posts at all in the DB
    // long allPostsCount = postRepository.count();
    // log.info("Explore: Total posts in database: {}", allPostsCount);

    // List<Post> resultPosts;
    // if (user == null) {
    // log.info("Explore: No user session found, showing all posts");
    // resultPosts = postRepository.findVisiblePosts(pageable).getContent();
    // // resultPosts = postRepository.findByDeletedFalse(pageable).getContent();
    // } else {
    // Set<Long> excludeIds = new HashSet<>();
    // excludeIds.add(user.getId());

    // try {
    // List<Follow> follows = followRepository.findByFollowerId(user.getId());
    // if (follows != null) {
    // for (Follow f : follows) {
    // if (f.getFollowingId() != null) {
    // excludeIds.add(f.getFollowingId());
    // }
    // }
    // }
    // } catch (Exception e) {
    // log.error("Explore: Error fetching follows: {}", e.getMessage());
    // }

    // log.info("Explore: User {} is excluding {} authors", user.getId(),
    // excludeIds.size());

    // // If the repository method fails for some reason, let's have a fallback
    // try {
    // resultPosts = postRepository.findByAuthorIdNotInAndDeletedFalse(new
    // ArrayList<>(excludeIds), pageable)
    // .getContent();
    // } catch (Exception e) {
    // log.error("Explore: Repository query failed: {}", e.getMessage());
    // // Fallback to in-memory filtering for robustness
    // resultPosts =
    // postRepository.findByDeletedFalse(pageable).getContent().stream()
    // .filter(p -> !excludeIds.contains(p.getAuthorId()))
    // .collect(Collectors.toList());
    // }
    // }

    // log.info("Explore: Returning {} posts", resultPosts.size());
    // return ResponseEntity.ok(Map.of("posts", resultPosts));
    // }

    @GetMapping("/user/{id}")
    public ResponseEntity<?> postsByUser(@org.springframework.web.bind.annotation.PathVariable Long id) {
        List<Post> posts = postRepository.findByAuthorIdAndDeletedFalseOrderByCreatedAtDesc(id);
        return ResponseEntity.ok(Map.of("posts", posts));
    }

    @GetMapping("/feed")
    public ResponseEntity<?> feed(@RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }

        Set<Long> includeIds = new HashSet<>();
        includeIds.add(user.getId());

        try {
            List<Follow> follows = followRepository.findByFollowerId(user.getId());
            if (follows != null) {
                for (Follow f : follows) {
                    if (f.getFollowingId() != null) {
                        includeIds.add(f.getFollowingId());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Feed: Error fetching follows: {}", e.getMessage());
        }

        log.info("Feed: User {} is including {} authors", user.getId(), includeIds.size());

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Post> resultPosts;
        try {
            resultPosts = postRepository.findByAuthorIdInAndDeletedFalse(new ArrayList<>(includeIds), pageable)
                    .getContent();
        } catch (Exception e) {
            log.error("Feed: Repository query failed: {}", e.getMessage());
            // Fallback to in-memory filtering for robustness
            resultPosts = postRepository.findByDeletedFalse(pageable).getContent().stream()
                    .filter(p -> includeIds.contains(p.getAuthorId())).collect(Collectors.toList());
        }

        // For brand-new users (or empty personalized feed), seed page 0 with
        // personalized suggestions first, then backfill from explore posts.
        if (page == 0 && (resultPosts == null || resultPosts.size() < 5)) {
            String preferredCategory = mapPrimarySkillToCategory(user.getPrimarySkill());

            List<Post> personalized = new ArrayList<>();
            Set<Long> seenPostIds = new HashSet<>();

            // ðŸ”¥ 1. Category-based posts
            if (preferredCategory != null) {
                List<Post> byCategory = postRepository.findByCategoryAndDeletedFalse(preferredCategory, pageable)
                        .getContent();
                for (Post p : byCategory) {
                    if (seenPostIds.add(p.getId())) {
                        personalized.add(p);
                    }
                }
            }

            // ðŸ”¥ 2. Trending posts (likes based)
            List<Post> trending = postRepository
                    .findByDeletedFalse(PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "likeCount")))
                    .getContent();
            for (Post p : trending) {
                if (personalized.size() >= size)
                    break;
                if (seenPostIds.add(p.getId())) {
                    personalized.add(p);
                }
            }

            // ðŸ”¥ 3. Explore fallback
            List<Post> explore = postRepository.findExplorePosts(pageable).getContent();

            for (Post p : explore) {
                if (personalized.size() >= size)
                    break;
                if (seenPostIds.add(p.getId())) {
                    personalized.add(p);
                }
            }
            resultPosts = personalized;
        }
        log.info("Feed fallback used for user {} â†’ {} posts", user.getId(), resultPosts.size());

        return ResponseEntity.ok(Map.of("posts", resultPosts));
    }

    private double calculateScore(Post post, User user) {
        double score = 0;

        // ðŸ”¥ 1. Technical boost
        if ("ai".equals(post.getCategory()) || "ml".equals(post.getCategory()) || "backend".equals(post.getCategory())
                || "frontend".equals(post.getCategory())) {
            score += 50;
        }

        // ðŸ”¥ 2. Likes weight
        score += post.getLikeCount() * 2;

        // ðŸ”¥ 3. Comments weight
        score += post.getCommentCount() * 3;

        // ðŸ”¥ 4. Freshness (recent posts)
        long minutes = java.time.Duration.between(post.getCreatedAt(), java.time.LocalDateTime.now()).toMinutes();
        score += Math.max(0, 100 - minutes);

        // ðŸ”¥ 5. User interest match
        if (user.getPrimarySkill() != null && post.getCategory().contains(user.getPrimarySkill().toLowerCase())) {
            score += 40;
        }

        // ðŸ”¥ 6. Engagement ratio (NEW)
        int totalReactions = post.getLikeCount() + post.getDislikeCount();
        if (totalReactions > 0) {
            double ratio = (double) post.getLikeCount() / totalReactions;
            score += ratio * 50;
        }
        return score;
    }

    @PostMapping(consumes = { "multipart/form-data" })
    public ResponseEntity<?> create(
            @RequestParam("content") String content,
            @RequestParam(value = "code", required = false, defaultValue = "") String code,
            @RequestParam(value = "category", required = false, defaultValue = "general") String category,
            @RequestParam(value = "imageUrl", required = false, defaultValue = "") String imageUrl,
            @RequestParam(value = "linkUrl", required = false, defaultValue = "") String linkUrl,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            HttpSession session) {
        byte[] processedBytes = null;
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }

        content = content.trim();
        code = code.trim();
        imageUrl = imageUrl.trim();
        linkUrl = linkUrl.trim();
        category = category.trim().toLowerCase();

        // ====== DEBUG: Log incoming request summary for troubleshooting ======
        try {
            log.info(
                    "[POST DEBUG] create() â†’ userId={}, contentLen={}, codeLen={}, category={}, imageFilePresent={}, imageFileName={}, linkUrlLen={}",
                    user != null ? user.getId() : null,
                    content != null ? content.length() : 0,
                    code != null ? code.length() : 0,
                    category,
                    (imageFile != null && !imageFile.isEmpty()),
                    (imageFile != null ? imageFile.getOriginalFilename() : ""),
                    linkUrl != null ? linkUrl.length() : 0);
        } catch (Exception e) {
            log.warn("[POST DEBUG] failed to log incoming request summary", e);
        }

        // ðŸ”¥ Handle Local Image Upload
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                byte[] rawBytes = imageFile.getBytes();
                // âœ… PROCESS IMAGE: Crop to Square and Resize to 1080px
                processedBytes = postImageService.processImage(rawBytes, imageFile.getContentType());

                // âœ… NEW SAFETY CHECK (IMPORTANT)
                if (processedBytes == null || processedBytes.length == 0) {
                    return ResponseEntity.badRequest().body(Map.of("message", "Invalid image"));
                }

                String localPath = postImageService.savePostImageBytes(user, imageFile.getOriginalFilename(),
                        imageFile.getContentType(), processedBytes);
                if (localPath != null) {
                    imageUrl = localPath;
                }
            } catch (Exception e) {
                log.error("Image validation or processing failed: {}", e.getMessage());
                return ResponseEntity.badRequest().body(Map.of("message", "Image processing/validation failed"));
            }
        }

        java.util.Set<String> allowed = java.util.Set.of("frontend", "backend", "fullstack", "mobile", "devops", "data",
                "ai", "ml", "security", "cloud", "gamedev", "general");
        if (category.isEmpty() || !allowed.contains(category)) {
            category = "general";
        }
        if (content.isEmpty() && code.isEmpty() && imageUrl.isEmpty() && linkUrl.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Content required"));
        }

        // ðŸ”¥ Combine all inputs for AI check

        String finalCategory = category;

        String fullText = (content + " " + code).trim();
        boolean hasText = !fullText.isEmpty();
        boolean hasImage = imageFile != null && !imageFile.isEmpty();

        boolean isTechnicalText = true;
        boolean isTechnicalImage = true;

        try {

            // âœ… TEXT ONLY
            if (hasText && !hasImage) {

                isTechnicalText = aiService.isTechnicalText(fullText);

                if (!isTechnicalText) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "message", "âŒ Only technical text content allowed"));
                }
            }

            // âœ… IMAGE ONLY
            else if (!hasText && hasImage) {

                isTechnicalImage = aiService.isTechnicalImage(processedBytes);

                if (!isTechnicalImage) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "message", "âŒ Only technical image allowed"));
                }
                System.out.println("TEXT TECH: " + isTechnicalText);
                System.out.println("IMAGE TECH: " + isTechnicalImage);
            }

            // âœ… BOTH TEXT + IMAGE
            else if (hasText && hasImage) {

                isTechnicalText = aiService.isTechnicalText(fullText);
                isTechnicalImage = aiService.isTechnicalImage(processedBytes);

                if (!isTechnicalText && !isTechnicalImage) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "message", "âŒ Both text and image are non-technical"));
                }

                if (!isTechnicalText) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "message", "âš ï¸ Please improve technical description"));
                }

                if (!isTechnicalImage) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "message", "ðŸ–¼ï¸ Please upload technical image"));
                }
            }

        } catch (Exception e) {
            log.error("AI validation failed", e);
            return ResponseEntity.status(500).body(Map.of("message", "Validation failed"));
        }

        Post post = new Post();
        post.setAuthorId(user.getId());
        post.setAuthor(user.getUsername() != null ? user.getUsername() : user.getFullName());

        post.setContent(content);
        post.setCategory(finalCategory);
        if (!code.isEmpty())
            post.setCode(code);
        if (!imageUrl.isEmpty())
            post.setImageUrl(imageUrl);
        if (!linkUrl.isEmpty())
            post.setLinkUrl(linkUrl);
        post.setCreatedAt(LocalDateTime.now());
        post.setLikes(0);
        post.setLikeCount(0);
        post.setDislikes(0);
        post.setDislikeCount(0);
        post.setCommentCount(0);
        post.setDeleted(false);

        post = postRepository.save(post);
        log.info("Post created: ID={}, AuthorID={}, Content={}", post.getId(), post.getAuthorId(),
                post.getContent().substring(0, Math.min(20, post.getContent().length())));

        user.setTotalPosts(user.getTotalPosts() + 1);
        userRepository.save(user);

        String successMsg = "Post created successfully!";
        return ResponseEntity.ok(Map.of("message", successMsg, "post", post));
    }

    @org.springframework.transaction.annotation.Transactional
    @PostMapping("/{id}/like")
    public ResponseEntity<?> like(@org.springframework.web.bind.annotation.PathVariable Long id, HttpSession session) {
        User me = (User) session.getAttribute("user");
        if (me == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
        Post post = postRepository.findById(id).orElse(null);
        if (post == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Post not found"));
        }
        if (dislikeRepository.findByPostIdAndUserId(id, me.getId()).isPresent()) {
            dislikeRepository.deleteForPostAndUser(id, me.getId());
        }
        if (likeRepository.findByPostIdAndUserId(id, me.getId()).isEmpty()) {
            Like like = new Like();
            like.setPostId(id);
            like.setUserId(me.getId());
            like.setCreatedAt(LocalDateTime.now());
            likeRepository.save(like);
        }
        syncReactionCounts(post, id);

        me.setTotalLikes(me.getTotalLikes() + 1);
        userRepository.save(me);
        return ResponseEntity.ok(reactionResponse(id, true, false));
    }

    @org.springframework.transaction.annotation.Transactional
    @org.springframework.web.bind.annotation.DeleteMapping("/{id}/like")
    public ResponseEntity<?> unlike(@org.springframework.web.bind.annotation.PathVariable Long id,
            HttpSession session) {
        User me = (User) session.getAttribute("user");
        if (me == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
        Post post = postRepository.findById(id).orElse(null);
        if (post == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Post not found"));
        }
        if (likeRepository.findByPostIdAndUserId(id, me.getId()).isPresent()) {
            likeRepository.deleteForPostAndUser(id, me.getId());
        }
        syncReactionCounts(post, id);
        return ResponseEntity.ok(reactionResponse(id, false, false));
    }

    @org.springframework.transaction.annotation.Transactional
    @PostMapping("/{id}/dislike")
    public ResponseEntity<?> dislike(@org.springframework.web.bind.annotation.PathVariable Long id,
            HttpSession session) {
        User me = (User) session.getAttribute("user");
        if (me == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
        Post post = postRepository.findById(id).orElse(null);
        if (post == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Post not found"));
        }
        if (likeRepository.findByPostIdAndUserId(id, me.getId()).isPresent()) {
            likeRepository.deleteForPostAndUser(id, me.getId());
        }
        if (dislikeRepository.findByPostIdAndUserId(id, me.getId()).isEmpty()) {
            Dislike dislike = new Dislike();
            dislike.setPostId(id);
            dislike.setUserId(me.getId());
            dislike.setCreatedAt(LocalDateTime.now());
            dislikeRepository.save(dislike);
        }
        syncReactionCounts(post, id);
        return ResponseEntity.ok(reactionResponse(id, false, true));
    }

    @org.springframework.transaction.annotation.Transactional
    @org.springframework.web.bind.annotation.DeleteMapping("/{id}/dislike")
    public ResponseEntity<?> undislike(@org.springframework.web.bind.annotation.PathVariable Long id,
            HttpSession session) {
        User me = (User) session.getAttribute("user");
        if (me == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
        Post post = postRepository.findById(id).orElse(null);
        if (post == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Post not found"));
        }
        if (dislikeRepository.findByPostIdAndUserId(id, me.getId()).isPresent()) {
            dislikeRepository.deleteForPostAndUser(id, me.getId());
        }
        syncReactionCounts(post, id);
        return ResponseEntity.ok(reactionResponse(id, false, false));
    }

    @GetMapping("/likedIds")
    public ResponseEntity<?> likedIds(HttpSession session) {
        User me = (User) session.getAttribute("user");
        if (me == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
        java.util.List<Like> likes = likeRepository.findByUserId(me.getId());
        java.util.Set<Long> liked = likes.stream().map(Like::getPostId).collect(java.util.stream.Collectors.toSet());
        return ResponseEntity.ok(Map.of("ids", liked));
    }

    @GetMapping("/dislikedIds")
    public ResponseEntity<?> dislikedIds(HttpSession session) {
        User me = (User) session.getAttribute("user");
        if (me == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
        java.util.List<Dislike> dislikes = dislikeRepository.findByUserId(me.getId());
        java.util.Set<Long> disliked = dislikes.stream().map(Dislike::getPostId)
                .collect(java.util.stream.Collectors.toSet());
        return ResponseEntity.ok(Map.of("ids", disliked));
    }

    @GetMapping("/liked")
    public ResponseEntity<?> likedPosts(HttpSession session) {
        User me = (User) session.getAttribute("user");
        if (me == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
        java.util.List<Like> likes = likeRepository.findByUserId(me.getId());
        java.util.Set<Long> postIds = likes.stream().map(Like::getPostId).collect(java.util.stream.Collectors.toSet());

        java.util.List<Post> posts = postIds.isEmpty() ? java.util.List.of()
                : postRepository.findByIdInAndDeletedFalseOrderByCreatedAtDesc(postIds);

        return ResponseEntity.ok(Map.of("posts", posts));
    }

    @GetMapping("/disliked")
    public ResponseEntity<?> dislikedPosts(HttpSession session) {
        User me = (User) session.getAttribute("user");
        if (me == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
        java.util.List<Dislike> dislikes = dislikeRepository.findByUserId(me.getId());
        java.util.Set<Long> postIds = dislikes.stream().map(Dislike::getPostId)
                .collect(java.util.stream.Collectors.toSet());

        java.util.List<Post> posts = postIds.isEmpty() ? java.util.List.of()
                : postRepository.findByIdInAndDeletedFalseOrderByCreatedAtDesc(postIds);

        return ResponseEntity.ok(Map.of("posts", posts));
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@org.springframework.web.bind.annotation.PathVariable Long id,
            HttpSession session) {
        User me = (User) session.getAttribute("user");
        if (me == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
        Post post = postRepository.findById(id).orElse(null);
        if (post == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Post not found"));
        }
        if (!post.getAuthorId().equals(me.getId())) {
            return ResponseEntity.status(403).body(Map.of("message", "Forbidden: You can only delete your own posts"));
        }
        commentRepository.deleteByPostId(id);
        post.setDeleted(true);
        post.setCommentCount(0);
        postRepository.save(post);
        log.info("Post deleted: ID={}, AuthorID={}", id, me.getId());
        return ResponseEntity.ok(Map.of("message", "Post deleted successfully"));
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<?> commentsByPost(
            @org.springframework.web.bind.annotation.PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpSession session) {
        User me = (User) session.getAttribute("user");
        if (me == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }

        Post post = postRepository.findById(id).orElse(null);
        if (post == null || post.isDeleted()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Post not found"));
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtDesc(id, pageable).getContent();
        return ResponseEntity.ok(Map.of("comments", comments));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<?> addComment(
            @org.springframework.web.bind.annotation.PathVariable Long id,
            @RequestParam("content") String content,
            HttpSession session) {
        User me = (User) session.getAttribute("user");
        if (me == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }

        Post post = postRepository.findById(id).orElse(null);
        if (post == null || post.isDeleted()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Post not found"));
        }

        String normalized = content == null ? "" : content.trim();

        // âŒ Minimum length
        if (normalized.length() < 10) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "âŒ Please write a meaningful comment"));
        }

        // âŒ Low quality words
        String lower = normalized.toLowerCase();
        String[] lowWords = { "ok", "nice", "wow", "good", "hmm" };

        for (String w : lowWords) {
            if (lower.equals(w)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "âŒ Low-quality comment not allowed"));
            }
        }

        // ðŸ”¥ BAD WORD CHECK
        String[] badWords = { "idiot", "stupid", "fuck", "shit", "dumb", "bitch", "asshole", "bastard", "sex", "porn",
                "nude", "xxx" };

        for (String w : badWords) {
            if (lower.matches(".*\\b" + w + "\\b.*")) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "âŒ Inappropriate language not allowed"));
            }
        }

        // ðŸ”¥ TOO SHORT
        if (normalized.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Comment cannot be empty"));
        }

        String[] words = normalized.split("\\s+");

        // spam check
        if (words.length > 3) {
            Set<String> unique = new HashSet<>(Arrays.asList(words));
            if (unique.size() <= 2) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "âŒ Spam comment not allowed"));
            }
        }

        if (words.length < 2) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "âŒ Comment too short"));
        }
        if (normalized.length() > 1000) {
            return ResponseEntity.badRequest().body(Map.of("message", "Comment is too long"));
        }

        Comment comment = new Comment();
        comment.setPostId(id);
        comment.setUserId(me.getId());
        String commentUsername = me.getUsername() != null && !me.getUsername().isBlank()
                ? me.getUsername()
                : (me.getFullName() != null && !me.getFullName().isBlank() ? me.getFullName() : "user");
        comment.setUsername(commentUsername);
        comment.setContent(normalized);
        comment.setCreatedAt(LocalDateTime.now());
        comment = commentRepository.save(comment);

        syncCommentCount(post, id);

        log.info("Comment added by user {} on post {}: {}", me.getId(), id, normalized);

        me.setFollowers(me.getFollowers()); // dummy touch to track activity
        userRepository.save(me);

        me.setTotalComments(me.getTotalComments() + 1);
        userRepository.save(me);
        return ResponseEntity.ok(Map.of(
                "message", "Comment added",
                "comment", comment,
                "commentCount", post.getCommentCount()));
    }

    @org.springframework.transaction.annotation.Transactional
    @org.springframework.web.bind.annotation.DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<?> deleteComment(
            @org.springframework.web.bind.annotation.PathVariable Long postId,
            @org.springframework.web.bind.annotation.PathVariable Long commentId,
            HttpSession session) {
        User me = (User) session.getAttribute("user");
        if (me == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }

        Post post = postRepository.findById(postId).orElse(null);
        if (post == null || post.isDeleted()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Post not found"));
        }

        Comment comment = commentRepository.findById(commentId).orElse(null);
        if (comment == null || !postId.equals(comment.getPostId())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Comment not found"));
        }

        boolean canDelete = me.getId().equals(comment.getUserId()) || me.getId().equals(post.getAuthorId());
        if (!canDelete) {
            return ResponseEntity.status(403).body(Map.of("message", "Forbidden"));
        }

        commentRepository.delete(comment);
        syncCommentCount(post, postId);

        return ResponseEntity.ok(Map.of(
                "message", "Comment deleted",
                "commentCount", post.getCommentCount()));
    }

    private String mapPrimarySkillToCategory(String primarySkill) {
        if (primarySkill == null || primarySkill.isBlank()) {
            return null;
        }

        String s = primarySkill.trim().toLowerCase();

        if (s.contains("front"))
            return "frontend";
        if (s.contains("back"))
            return "backend";
        if (s.contains("full"))
            return "fullstack";
        if (s.contains("mobile") || s.contains("android") || s.contains("ios"))
            return "mobile";
        if (s.contains("devops") || s.contains("kubernetes") || s.contains("docker") || s.contains("ci/cd"))
            return "devops";
        if (s.contains("data"))
            return "data";
        if (s.contains("machine learning") || s.equals("ml"))
            return "ml";
        if (s.contains("ai") || s.contains("artificial intelligence"))
            return "ai";
        if (s.contains("security") || s.contains("cyber"))
            return "security";
        if (s.contains("cloud") || s.contains("aws") || s.contains("azure") || s.contains("gcp"))
            return "cloud";
        if (s.contains("game"))
            return "gamedev";

        return null;
    }

    private void syncReactionCounts(Post post, Long postId) {
        int likeCount = likeRepository.countByPostId(postId);
        int dislikeCount = dislikeRepository.countByPostId(postId);
        post.setLikes(likeCount);
        post.setLikeCount(likeCount);
        post.setDislikes(dislikeCount);
        post.setDislikeCount(dislikeCount);
        postRepository.save(post);
    }

    private void syncCommentCount(Post post, Long postId) {
        int commentCount = commentRepository.countByPostId(postId);
        post.setCommentCount(commentCount);
        postRepository.save(post);
    }

    private Map<String, Object> reactionResponse(Long postId, boolean liked, boolean disliked) {
        int likeCount = likeRepository.countByPostId(postId);
        int dislikeCount = dislikeRepository.countByPostId(postId);
        return Map.of(
                "likes", likeCount,
                "dislikes", dislikeCount,
                "liked", liked,
                "disliked", disliked);
    }

    @org.springframework.transaction.annotation.Transactional
    @PostMapping("/{postId}/save")
    public ResponseEntity<?> savePost(@PathVariable Long postId, HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        Optional<SavedPost> existing = savedPostRepository.findByUserIdAndPostId(user.getId(), postId);

        if (existing.isPresent()) {
            savedPostRepository.deleteByUserIdAndPostId(user.getId(), postId);
            return ResponseEntity.ok("Post removed from saved");
        }

        SavedPost saved = new SavedPost();
        saved.setUserId(user.getId());
        saved.setPostId(postId);
        saved.setSavedAt(java.time.LocalDateTime.now());

        savedPostRepository.save(saved);

        return ResponseEntity.ok("Post saved");
    }

    @GetMapping("/saved")
    public ResponseEntity<?> getSavedPosts(HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        List<SavedPost> savedList = savedPostRepository.findByUserId(user.getId());

        List<Post> posts = savedList.stream()
                .map(s -> postRepository.findById(s.getPostId()).orElse(null))
                .filter(p -> p != null)
                .toList();

        return ResponseEntity.ok(Map.of("posts", posts));
    }

    @GetMapping("/savedIds")
    public ResponseEntity<?> savedIds(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
        java.util.List<SavedPost> savedList = savedPostRepository.findByUserId(user.getId());
        java.util.Set<Long> savedIds = savedList.stream().map(SavedPost::getPostId)
                .collect(java.util.stream.Collectors.toSet());
        return ResponseEntity.ok(Map.of("ids", savedIds));
    }

}
