package com.example.signup;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.example.signup.controller.AdminController;
import com.example.signup.controller.AuthController;
import com.example.signup.controller.PostController;
import com.example.signup.controller.SearchController;
import com.example.signup.entity.AppSettings;
import com.example.signup.entity.Post;
import com.example.signup.entity.User;
import com.example.signup.repository.AppSettingsRepository;
import com.example.signup.repository.FollowRepository;
import com.example.signup.repository.LikeRepository;
import com.example.signup.repository.PostReportRepository;
import com.example.signup.repository.PostRepository;
import com.example.signup.repository.UserRepository;
import com.example.signup.service.AIContentValidatorService;
import com.example.signup.service.PostImageService;
import com.example.signup.service.ProfileImageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {
                AuthController.class,
                AdminController.class,
                SearchController.class,
                PostController.class
})
@AutoConfigureMockMvc(addFilters = false)
class ControllerFlowTests {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private UserRepository userRepository;

        @MockBean
        private FollowRepository followRepository;

        @MockBean
        private PostRepository postRepository;

        @MockBean
        private PostReportRepository postReportRepository;

        @MockBean
        private JavaMailSender mailSender;

        @MockBean
        private ProfileImageService profileImageService;

        @MockBean
        private PasswordEncoder passwordEncoder;

        @MockBean
        private PostImageService postImageService;

        @MockBean
        private LikeRepository likeRepository;

        @MockBean
        private AppSettingsRepository appSettingsRepository;

        @MockBean
        private AIContentValidatorService aiService;

        @MockBean
        private JdbcTemplate jdbcTemplate;

        @Test
        void signupValidation_MissingFields_Returns400() throws Exception {
                String payload = """
                                {
                                  "fullName": "Test User",
                                  "password": "1234"
                                }
                                """;

                mockMvc.perform(post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").value("Validation failed"))
                                .andExpect(jsonPath("$.errors").isArray());
        }

        @Test
        void signupSuccess_ReturnsUserPayload() throws Exception {
                when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
                when(userRepository.existsByUsername("testuser")).thenReturn(false);
                when(passwordEncoder.encode("1234")).thenReturn("encoded-1234");
                when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                        User u = inv.getArgument(0);
                        u.setId(10L);
                        return u;
                });

                String payload = """
                                {
                                  "fullName": "Test User",
                                  "username": "testuser",
                                  "email": "test@example.com",
                                  "password": "1234",
                                  "githubUsername": "test-gh",
                                  "primarySkill": "Java"
                                }
                                """;

                mockMvc.perform(post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(10))
                                .andExpect(jsonPath("$.username").value("testuser"))
                                .andExpect(jsonPath("$.email").value("test@example.com"));
        }

        @Test
        void loginSuccess_ReturnsAuthenticatedUser() throws Exception {
                User u = new User();
                u.setId(11L);
                u.setUsername("alpha");
                u.setEmail("alpha@example.com");
                u.setPassword("$2a$dummy");
                u.setAdmin(false);

                when(userRepository.findByUsernameOrEmail("alpha", "alpha")).thenReturn(Optional.of(u));
                when(passwordEncoder.matches("pass123", "$2a$dummy")).thenReturn(true);

                String payload = """
                                {
                                  "loginIdentifier": "alpha",
                                  "password": "pass123"
                                }
                                """;

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(11))
                                .andExpect(jsonPath("$.username").value("alpha"));
        }

        @Test
        void adminUsers_NoAdminSession_Returns401() throws Exception {
                mockMvc.perform(get("/api/admin/users"))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.message").value("Unauthorized"));
        }

        @Test
        void searchFlow_ReturnsUsersAndPosts() throws Exception {
                User u = new User();
                u.setId(21L);
                u.setUsername("springdev");
                u.setFullName("Spring Dev");

                Post p = new Post();
                p.setId(31L);
                p.setAuthorId(21L);
                p.setAuthor("springdev");
                p.setContent("Spring MVC tips");
                p.setLikeCount(5);
                p.setCreatedAt(LocalDateTime.now());
                p.setDeleted(false);

                when(userRepository.findByUsernameContainingIgnoreCase("spring")).thenReturn(List.of(u));
                when(userRepository.findByFullNameContainingIgnoreCase("spring")).thenReturn(List.of());
                when(postRepository
                                .findByContentContainingIgnoreCaseOrCodeContainingIgnoreCaseAndDeletedFalse("spring"))
                                .thenReturn(List.of(p));

                mockMvc.perform(get("/api/search").param("q", "spring"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.users[0].username").value("springdev"))
                                .andExpect(jsonPath("$.posts[0].id").value(31));
        }

        @Test
        void uploadFlow_ProfilePicUpload_ReturnsPath() throws Exception {
                User sessionUser = new User();
                sessionUser.setId(41L);
                sessionUser.setUsername("uploader");

                MockMultipartFile file = new MockMultipartFile(
                                "file",
                                "profile.png",
                                "image/png",
                                "fake-image".getBytes());

                when(profileImageService.saveProfileImage(any(User.class), any()))
                                .thenReturn("/uploads/profile/pic.png");

                mockMvc.perform(multipart("/api/auth/users/me/profile-pic")
                                .file(file)
                                .sessionAttr("user", sessionUser))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.path").value("/uploads/profile/pic.png"));
        }

        @Test
        void uploadFlow_PostCreateMultipart_ReturnsCreatedPost() throws Exception {
                User sessionUser = new User();
                sessionUser.setId(51L);
                sessionUser.setUsername("poster");

                AppSettings testMode = new AppSettings("app_mode", "TEST");
                when(appSettingsRepository.findBySettingKey("app_mode")).thenReturn(Optional.of(testMode));
                when(aiService.isTechnicalText(any(String.class))).thenReturn(true);
                when(aiService.isTechnicalImage(any())).thenReturn(true);
                when(postRepository.save(any(Post.class))).thenAnswer(inv -> {
                        Post p = inv.getArgument(0);
                        p.setId(61L);
                        return p;
                });

                mockMvc.perform(multipart("/api/posts")
                                .param("content", "My first technical post")
                                .param("category", "general")
                                .sessionAttr("user", sessionUser))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.post.id").value(61));
        }
}
