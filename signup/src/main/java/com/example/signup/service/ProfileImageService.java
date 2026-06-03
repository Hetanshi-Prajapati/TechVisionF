package com.example.signup.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.signup.entity.User;
import com.example.signup.repository.UserRepository;

@Service
public class ProfileImageService {
    private final UserRepository userRepository;
    //private final Path root = Path.of("uploads", "profile");
    private final Path root = Path.of("signup", "uploads", "profile");
    private final long maxSize = 1024 * 1024;

    public ProfileImageService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String saveProfileImage(User user, MultipartFile file) {
        if (user == null)
            throw new IllegalArgumentException("Unauthorized");
        if (file == null || file.isEmpty())
            throw new IllegalArgumentException("File required");
        if (file.getSize() > maxSize)
            throw new IllegalArgumentException("File too large");
        
        // Store content type once to avoid multiple calls (and potential NPE)
        String contentType = file.getContentType();
        String ct = contentType == null ? "" : contentType.toLowerCase();
        boolean okType = ct.equals("image/jpeg") || ct.equals("image/jpg") || ct.equals("image/png");
        if (!okType)
            throw new IllegalArgumentException("Invalid file type");
        String ext = ct.equals("image/png") ? ".png" : ".jpeg";
        String name = user.getId() + "_" + Instant.now().toEpochMilli() + ext;
        try {
            Files.createDirectories(root);
            Path target = root.resolve(name);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            String rel = "/uploads/profile/" + name;
            user.setProfilePic(rel);
            userRepository.save(user);
            return rel;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save image");
        }
    }
}
