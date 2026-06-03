package com.example.signup.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.signup.entity.User;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;

@Service
public class PostImageService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PostImageService.class);

    static {
        // Required for AWT processing on headless server environments
        System.setProperty("java.awt.headless", "true");
    }

    //private final Path root = Path.of("uploads", "posts");
    private final Path root = Path.of("signup", "uploads", "posts");

    // Constructor
    public PostImageService() {
    }

    /**
     * Processes image: center crops to 1:1 and resizes to max 1080px
     */
    public byte[] processImage(byte[] imageBytes, String contentType) throws IOException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes)) {
            BufferedImage originalImage = ImageIO.read(bais);
            if (originalImage == null) {
                throw new IOException("Failed to read image content");
            }

            int width = originalImage.getWidth();
            int height = originalImage.getHeight();
            log.info("Processing image: {}x{}", width, height);

            // 1. Calculate Target Dimensions (Keep Aspect Ratio)
            int maxWidth = 1080;
            int maxHeight = 1080;
            int targetWidth = width;
            int targetHeight = height;

            if (width > maxWidth || height > maxHeight) {
                double scale = Math.min((double) maxWidth / width, (double) maxHeight / height);
                targetWidth = (int) (width * scale);
                targetHeight = (int) (height * scale);
                log.info("Resizing from {}x{} to {}x{} (scale: {})", width, height, targetWidth, targetHeight, scale);
            }

            // 2. Prepare Resized Image (Preserve proportions)
            BufferedImage finalImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = finalImage.createGraphics();

            // Set high quality rendering hints
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
            g.dispose();

            // 3. Convert back to bytes
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                String format = "jpg";
                if (contentType != null) {
                    if (contentType.toLowerCase().contains("png"))
                        format = "png";
                    else if (contentType.toLowerCase().contains("webp"))
                        format = "png"; // Fallback to PNG for storage compatibility
                }

                ImageIO.write(finalImage, format, baos);
                return baos.toByteArray();
            }
        }
    }

    /**
     * Original method compatibility. Now processes image by default.
     */
    public String savePostImage(User user, MultipartFile file) {
        try {
            byte[] processed = processImage(file.getBytes(), file.getContentType());
            return savePostImageBytes(user, file.getOriginalFilename(), file.getContentType(), processed);
        } catch (IOException e) {
            log.warn("Processing failed, saving raw image: {}", e.getMessage());
            try {
                return savePostImageBytes(user, file.getOriginalFilename(), file.getContentType(), file.getBytes());
            } catch (IOException ie) {
                throw new RuntimeException("Failed to save image: " + ie.getMessage());
            }
        }
    }

    public String savePostImageBytes(User user, String originalName, String contentType, byte[] processedBytes) {
        if (user == null)
            throw new IllegalArgumentException("Unauthorized");
        if (processedBytes == null || processedBytes.length == 0)
            throw new IllegalArgumentException("Image content is empty");

        String ct = contentType == null ? "" : contentType.toLowerCase();
        String ext = ".jpg";
        if (ct.contains("png"))
            ext = ".png";
        else if (ct.contains("gif"))
            ext = ".gif";
        else if (ct.contains("webp"))
            ext = ".webp";

        String name = user.getId() + "_" + Instant.now().toEpochMilli() + ext;

        try {
            Files.createDirectories(root);
            Path target = root.resolve(name);
            Files.write(target, processedBytes);
            return "/uploads/posts/" + name;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save post image: " + e.getMessage());
        }
    }
}
