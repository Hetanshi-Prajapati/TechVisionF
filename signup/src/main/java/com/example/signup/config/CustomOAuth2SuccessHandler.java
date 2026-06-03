package com.example.signup.config;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.example.signup.entity.User;
import com.example.signup.repository.UserRepository;

@Component
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = token.getPrincipal();

        String provider = token.getAuthorizedClientRegistrationId(); // "google" or "github"

        // --- Extract email ---
        String email = oAuth2User.getAttribute("email");
        if (email == null || email.isBlank()) {
            String login = oAuth2User.getAttribute("login");
            email = (login != null ? login : "user") + "@github.oauth";
        }

        // --- Extract name & picture ---
        String name = oAuth2User.getAttribute("name");
        if (name == null || name.isBlank()) {
            name = oAuth2User.getAttribute("login"); // GitHub fallback
        }

        String picture = oAuth2User.getAttribute("picture");      // Google
        if (picture == null) picture = oAuth2User.getAttribute("avatar_url"); // GitHub

        // --- GitHub login username ---
        String githubLogin = "github".equals(provider) ? oAuth2User.getAttribute("login") : null;

        boolean isNewUser = false;
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            // New OAuth user
            isNewUser = true;
            user = new User();
            user.setEmail(email);
            user.setFullName(name != null ? name : "");
            user.setProfilePic(picture);
            user.setProfileComplete(false); // needs onboarding

            // Auto-generate a temporary username from email
            String baseUsername = email.split("@")[0].toLowerCase().replaceAll("[^a-z0-9_]", "");
            if (baseUsername.length() < 3) baseUsername += "usr";
            String username = baseUsername;
            int counter = 1;
            while (userRepository.existsByUsername(username)) {
                username = baseUsername + counter++;
            }
            user.setUsername(username);

            // Pre-fill GitHub username if signed in via GitHub
            if (githubLogin != null) user.setGithubUsername(githubLogin);

            user.setPassword(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
                    .encode(UUID.randomUUID().toString()));

            userRepository.save(user);
        } else {
            // Returning OAuth user — update profile pic / github name if missing
            boolean changed = false;
            if (picture != null && (user.getProfilePic() == null || user.getProfilePic().isBlank())) {
                user.setProfilePic(picture);
                changed = true;
            }
            if (githubLogin != null && (user.getGithubUsername() == null || user.getGithubUsername().isBlank())) {
                user.setGithubUsername(githubLogin);
                changed = true;
            }
            if (changed) userRepository.save(user);
        }

        HttpSession session = request.getSession(true);
        session.setAttribute("user", user);

        // New OAuth signups should complete profile first, then proceed to login.
        String targetUrl = isNewUser ? "/api/auth/complete-profile?next=login" : "/api/auth/home";
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write("<!DOCTYPE html>" +
                "<html><head><title>OAuth Success</title></head><body style=\"font-family: sans-serif; display: flex; align-items: center; justify-content: center; height: 100vh; margin: 0;\">" +
                "<div style=\"text-align: center; max-width: 420px;\">" +
                "<h1 style=\"margin-bottom: 16px;\">Authentication successful</h1>" +
                "<p style=\"margin-bottom: 24px; color: #555;\">This window will close automatically and return you to the app.</p>" +
                "<p style=\"font-size: 0.95rem; color: #777;\">If the popup does not close, <a href=\"" + targetUrl + "\" target=\"_top\">click here</a>.</p>" +
                "</div>" +
                "<script>" +
                "const target = '" + targetUrl + "';" +
                "if (window.opener && !window.opener.closed) { window.opener.location.href = target; }" +
                "setTimeout(() => { window.close(); }, 500);" +
                "</script></body></html>");
    }
}
