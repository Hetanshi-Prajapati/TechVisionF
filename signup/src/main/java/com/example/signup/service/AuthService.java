package com.example.signup.service;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.signup.dto.LoginRequest;
import com.example.signup.dto.LoginResponse;
import com.example.signup.entity.User;
import com.example.signup.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request) {

        Optional<User> userOptional;

        if (request.getEmailOrUsername().contains("@")) {
            userOptional = userRepository.findByEmail(request.getEmailOrUsername());
        } else {
            userOptional = userRepository.findByUsername(request.getEmailOrUsername());
        }

        if (userOptional.isEmpty()) {
            throw new RuntimeException("Invalid credentials");
        }

        User user = userOptional.get();

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
        // // ✅ Check if BCrypt
        // if (user.getPassword().startsWith("$2a$") ||
        // user.getPassword().startsWith("$2b$")
        // || user.getPassword().startsWith("$2y$")) {

        // if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
        // throw new RuntimeException("Invalid credentials");
        // }
        // } else {
        // // ✅ Plain text case
        // if (!request.getPassword().equals(user.getPassword())) {
        // throw new RuntimeException("Invalid credentials");
        // }

        // // 🔥 Convert to BCrypt (IMPORTANT)
        // String encoded = passwordEncoder.encode(request.getPassword());
        // user.setPassword(encoded);
        // userRepository.save(user);
        // }

        // Dummy token (no JWT yet)
        String token = "dummy-jwt-token";

        return new LoginResponse(token, user);
    }
}
