package com.example.signup.service;

/*Java Object ↔ JSON
Needed because Gemini API communicates using JSON. */
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.example.signup.repository.AppSettingsRepository;
import com.example.signup.entity.AppSettings;

import java.util.*;

@Service
public class AIContentValidatorService {

    private static final Logger log = LoggerFactory.getLogger(AIContentValidatorService.class);//Creates logger object.//Used for logs/debugging.

    @Value("${ai.model.url:http://127.0.0.1:5000}")
    private String aiModelUrl;

    @PostConstruct
    //Automatically start Flask AI server.
    public void startLocalAIServer() {
        if (!isLocalAiEndpoint()) {
            log.info("🌐 Using remote AI service at {}", getFlaskUrl());
            return;
        }

        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "python","D:/Final Project/uuzipped/ALLDONE/FINAL/ME WORKING/update(30)/AI Model/app.py");
                    //"D:/VTECH/update(27)/AI Model/app.py");
            pb.start();

            log.info("🚀 Starting Flask AI server...");

            // 🔥 WAIT UNTIL SERVER IS READY
            boolean isRunning = false;//Tracks Flask status.
            int retries = 0;//Prevents infinite waiting.

            //Keep checking until:server starts OR 10 attempts complete
            while (!isRunning && retries < 10) {
                try {
                    Thread.sleep(1000);//Wait 1 second.

                    restTemplate.getForObject("http://127.0.0.1:5000/", String.class);//Sends GET request to Flask.//Is Flask alive?
                    isRunning = true;

                } catch (Exception e) {
                    retries++;
                    log.warn("⏳ Waiting for Flask... attempt {}", retries);
                }
            }

            if (isRunning) {
                log.info("✅ Flask AI server is READY");
            } else {
                log.error("❌ Flask server did NOT start properly");
            }

        } catch (Exception e) {
            log.error("❌ Failed to start Flask server", e);
        }
    }

    private boolean isLocalAiEndpoint() {
        String normalized = aiModelUrl == null ? "" : aiModelUrl.trim().toLowerCase(Locale.ROOT);
        return normalized.isEmpty() || normalized.contains("127.0.0.1") || normalized.contains("localhost");
    }

    private String getFlaskUrl() {
        String baseUrl = aiModelUrl == null ? "" : aiModelUrl.trim();
        if (baseUrl.isEmpty()) {
            baseUrl = "http://127.0.0.1:5000";
        }
        return baseUrl.endsWith("/") ? baseUrl + "predict" : baseUrl + "/predict";
    }

    @Value("${gemini.api.key}")
    private String API_KEY;//Injects API key from properties file.

    private final RestTemplate restTemplate;//Used for API calls.
    private final AppSettingsRepository appSettingsRepository;//Database access.
    private final ObjectMapper objectMapper = new ObjectMapper();//JSON conversion.

    public enum ContentCategory {
        FRONTEND, BACKEND, FULLSTACK, MOBILE, DEVOPS, DATA, AI, ML, SECURITY, CLOUD,
        GAMEDEV, GENERAL;//Defines fixed categories.

        public static ContentCategory fromString(String value) {//Converts String → Enum safely.
            if (value == null)
                return GENERAL;
            try {
                return valueOf(value.toUpperCase());
            } catch (IllegalArgumentException e) {
                return GENERAL;
            }
        }

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    private static final String API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent";

    //Spring automatically injects dependencies.
    public AIContentValidatorService(RestTemplate restTemplate,
            AppSettingsRepository appSettingsRepository) {
        this.restTemplate = restTemplate;//Assign parameter → class variable.
        this.appSettingsRepository = appSettingsRepository;//Stores repository reference.
    }

    // DTOs for Gemini API
    private static class GeminiRequest {
        @JsonProperty("contents")
        public List<Content> contents;

        public static GeminiRequest of(String prompt) {//Easy object creation.
            GeminiRequest req = new GeminiRequest();
            Content content = new Content();
            Part part = new Part();
            part.text = prompt;
            content.parts = List.of(part);
            req.contents = List.of(content);
            return req;
        }

        static class Content {//Represents Gemini content block.
            @JsonProperty("parts")
            public List<Part> parts;//Stores multiple parts.
        }

        static class Part {//Represents text block.
            @JsonProperty("text")
            public String text;
        }
    }

    private static class GeminiResponse {//private static class GeminiResponse
        @JsonProperty("candidates")
        public List<Candidate> candidates;

        static class Candidate {
            @JsonProperty("content")
            public Content content;
        }

        static class Content {
            @JsonProperty("parts")
            public List<Part> parts;
        }

        static class Part {
            @JsonProperty("text")
            public String text;
        }
    }

    // ================= IMAGE VALIDATION =================
    public boolean isTechnicalImage(byte[] imageBytes) {

        boolean localResult = false;
        double localConfidence = 0.0;

        boolean geminiResult = false;
        double geminiConfidence = 0.0;

        // 🔹 LOCAL MODEL
        try {
            /*Images cannot be directly sent in JSON.
            Convert image → text format. */
            String base64 = Base64.getEncoder().encodeToString(imageBytes);

            Map<String, String> body = new HashMap<>();
            body.put("text", "");
            body.put("image", base64);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);//Request format = JSON

            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(//Sends POST request.
                    getFlaskUrl(),
                    request,
                    Map.class);

            if (response.getBody() != null) {
                String result = (String) response.getBody().get("result");
                localConfidence = Double.parseDouble(response.getBody().get("confidence").toString());

                localResult = "Allowed".equalsIgnoreCase(result);

                log.info("LOCAL → {} (confidence: {})", localResult, localConfidence);
            }
            log.info("DEBUG → localResult={}, localConfidence={}", localResult, localConfidence);

        } catch (Exception e) {
            log.error("Local AI failed", e);
        }

        // 🔹 GEMINI
        try {
            Map<String, Object> gemini = validateWithGeminiAdvanced(imageBytes);

            geminiResult = (boolean) gemini.get("result");
            geminiConfidence = (double) gemini.get("confidence");

            log.info("GEMINI → {} (confidence: {})", geminiResult, geminiConfidence);

        } catch (Exception e) {
            log.error("Gemini failed", e);
        }

        // 🔥 FINAL DECISION

        // SAME RESULT
        if (localResult == geminiResult) {
            log.info("FINAL → BOTH AGREE → {}", localResult);
            return localResult;
        }

        // ONE FAILED
        if (localConfidence == 0.0) {
            log.warn("LOCAL failed → using GEMINI");
            return geminiResult;
        }

        if (geminiConfidence == 0.0) {
            log.warn("GEMINI failed → using LOCAL");
            return localResult;
        }

        // CONFLICT → COMPARE CONFIDENCE
        if (localConfidence >= geminiConfidence) {
            log.info("FINAL → LOCAL wins");
            return localResult;
        } else {
            log.info("FINAL → GEMINI wins");
            return geminiResult;
        }
    }

    private Map<String, Object> validateWithGeminiAdvanced(byte[] imageBytes) {
        try {
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            String prompt = """
                    Classify this image STRICTLY.

                    Return TECHNICAL if:
                    - Code (Java, Python, C++, etc.)
                    - IDE or laptop screens
                    - Charts, graphs, dashboards
                    - Data structures (array, stack, queue, tree, linked list)
                    - Algorithms (sorting, searching)
                    - Handwritten technical notes
                    - Whiteboard explanations
                    - Mathematical / analytical diagrams

                    Return NOT_TECHNICAL if:
                    - Selfies / people / faces
                    - Nature / random photos
                    - Logos / brands / posters

                    Reply ONLY:
                    TECHNICAL or NOT_TECHNICAL
                    """;

            GeminiImageRequest request = new GeminiImageRequest(prompt, base64Image);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<GeminiImageRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<GeminiResponse> response = restTemplate.postForEntity(
                    API_URL + "?key=" + API_KEY,
                    entity,
                    GeminiResponse.class);

            if (response.getBody() == null || response.getBody().candidates == null
                    || response.getBody().candidates.isEmpty()) {

                log.warn("Empty Gemini response → allowing fallback");
                Map<String, Object> output = new HashMap<>();
                output.put("result", false);
                output.put("confidence", 0.0);
                return output;
            }

            String result = response.getBody().candidates.get(0).content.parts.get(0).text;

            if (result == null) {
                log.warn("Gemini returned null → allowing fallback");
                Map<String, Object> output = new HashMap<>();
                output.put("result", false);
                output.put("confidence", 0.0);
                return output;
            }

            boolean isTechnical = result.trim().equalsIgnoreCase("TECHNICAL");

            // fake confidence
            double confidence = isTechnical ? 0.85 : 0.6;

            Map<String, Object> output = new HashMap<>();
            output.put("result", isTechnical);
            output.put("confidence", confidence);

            return output;
        } 
        catch (Exception e) {
            log.error("Gemini failed → allowing fallback", e);
            Map<String, Object> output = new HashMap<>();
            output.put("result", false);
            output.put("confidence", 0.0);
            return output;
        }
    }

    public boolean isTechnicalText(String text) {

        boolean localResult = false;
        double localConfidence = 0.0;

        boolean geminiResult = false;
        double geminiConfidence = 0.0;

        // 🔹 LOCAL (FLASK)
        try {
            Map<String, String> body = new HashMap<>();
            body.put("text", text);
            body.put("image", "");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    getFlaskUrl(),
                    request,
                    Map.class);

            if (response.getBody() != null) {
                String result = (String) response.getBody().get("result");
                localConfidence = Double.parseDouble(response.getBody().get("confidence").toString());

                localResult = "Allowed".equalsIgnoreCase(result);
            }

        } catch (Exception e) {
            log.error("Local text failed", e);
        }

        // 🔹 GEMINI
        try {
            geminiResult = validateTextWithGemini(text);
            geminiConfidence = geminiResult ? 0.85 : 0.6;

        } catch (Exception e) {
            log.error("Gemini text failed", e);
        }

        // 🔥 FINAL DECISION

        if (localResult == geminiResult)
            return localResult;

        if (localConfidence == 0.0)
            return geminiResult;
        if (geminiConfidence == 0.0)
            return localResult;

        return localConfidence >= geminiConfidence ? localResult : geminiResult;
    }

    private boolean validateTextWithGemini(String content) {
        try {
            String prompt = """
                    Classify this content STRICTLY.

                    Return TECHNICAL only if:
                    - Programming, coding, development
                    - IT, software, AI, ML, data science

                    Return NOT_TECHNICAL if:
                    - Casual, non-tech content

                    Reply ONLY:
                    TECHNICAL or NOT_TECHNICAL

                    Content:
                    """ + content;

            GeminiRequest request = GeminiRequest.of(prompt);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<GeminiRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<GeminiResponse> response = restTemplate.postForEntity(
                    API_URL + "?key=" + API_KEY,
                    entity,
                    GeminiResponse.class);

            if (response.getBody() == null || response.getBody().candidates == null
                    || response.getBody().candidates.isEmpty()) {
                return true;
            }

            String result = response.getBody().candidates.get(0).content.parts.get(0).text;

            return result != null && result.trim().equalsIgnoreCase("TECHNICAL");

        } catch (Exception e) {
            log.error("Gemini text failed → allowing fallback", e);
            return false;
        }
    }

    // DTO for Gemini Image Request
    private static class GeminiImageRequest {
        @JsonProperty("contents")
        public List<Content> contents;

        public GeminiImageRequest(String prompt, String base64Image) {
            Content content = new Content();

            TextPart textPart = new TextPart();
            textPart.text = prompt;

            ImagePart imagePart = new ImagePart();
            imagePart.inlineData = new InlineData();
            imagePart.inlineData.mimeType = "image/jpeg";
            imagePart.inlineData.data = base64Image;

            content.parts = List.of(textPart, imagePart);
            this.contents = List.of(content);
        }

        static class Content {
            @JsonProperty("parts")
            public List<Object> parts;
        }

        static class TextPart {
            @JsonProperty("text")
            public String text;
        }

        static class ImagePart {
            @JsonProperty("inline_data")
            public InlineData inlineData;
        }

        static class InlineData {
            @JsonProperty("mime_type")
            public String mimeType;
            @JsonProperty("data")
            public String data;
        }
    }
}

// package com.example.signup.service;

// import com.fasterxml.jackson.annotation.JsonProperty;
// import com.fasterxml.jackson.databind.JsonNode;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.http.*;
// import org.springframework.stereotype.Service;
// import org.springframework.web.client.RestTemplate;
// import com.example.signup.repository.AppSettingsRepository;
// import com.example.signup.entity.AppSettings;

// import java.util.*;
// import java.util.regex.Matcher;
// import java.util.regex.Pattern;
// import java.io.IOException;

// @Service
// public class AIContentValidatorService {

// private static final Logger log =
// LoggerFactory.getLogger(AIContentValidatorService.class);

// @Value("${gemini.api.key}")
// private String API_KEY;

// private final RestTemplate restTemplate;
// private final AppSettingsRepository appSettingsRepository;
// private final ObjectMapper objectMapper = new ObjectMapper();

// public enum ContentCategory {
// FRONTEND, BACKEND, FULLSTACK, MOBILE, DEVOPS, DATA, AI, ML, SECURITY, CLOUD,
// GAMEDEV, GENERAL;

// public static ContentCategory fromString(String value) {
// if (value == null)
// return GENERAL;
// try {
// return valueOf(value.toUpperCase());
// } catch (IllegalArgumentException e) {
// return GENERAL;
// }
// }

// @Override
// public String toString() {
// return name().toLowerCase();
// }
// }

// private static final String API_URL =
// "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent";

// public AIContentValidatorService(RestTemplate restTemplate,
// AppSettingsRepository appSettingsRepository) {
// this.restTemplate = restTemplate;
// this.appSettingsRepository = appSettingsRepository;
// }

// // DTOs for Gemini API
// private static class GeminiRequest {
// @JsonProperty("contents")
// public List<Content> contents;

// public static GeminiRequest of(String prompt) {
// GeminiRequest req = new GeminiRequest();
// Content content = new Content();
// Part part = new Part();
// part.text = prompt;
// content.parts = List.of(part);
// req.contents = List.of(content);
// return req;
// }

// static class Content {
// @JsonProperty("parts")
// public List<Part> parts;
// }

// static class Part {
// @JsonProperty("text")
// public String text;
// }
// }

// private static class GeminiResponse {
// @JsonProperty("candidates")
// public List<Candidate> candidates;

// static class Candidate {
// @JsonProperty("content")
// public Content content;
// }

// static class Content {
// @JsonProperty("parts")
// public List<Part> parts;
// }

// static class Part {
// @JsonProperty("text")
// public String text;
// }
// }

// public static class ValidationResult {
// private final boolean isTechnical;
// private final boolean isSpam;
// private final String category;

// public ValidationResult(boolean isTechnical, boolean isSpam, String category)
// {
// this.isTechnical = isTechnical;
// this.isSpam = isSpam;
// this.category = category;
// }

// public boolean isTechnical() {
// return isTechnical;
// }

// public boolean isSpam() {
// return isSpam;
// }

// public String getCategory() {
// return category;
// }

// public static ValidationResult fallback() {
// // Fail-open: allow content but mark as general
// return new ValidationResult(true, false, ContentCategory.GENERAL.toString());
// }
// }

// public ValidationResult validateContent(String content) {
// AppSettings modeSetting =
// appSettingsRepository.findBySettingKey("app_mode").orElse(null);
// String mode = (modeSetting != null) ? modeSetting.getSettingValue() :
// "PRODUCTION";

// if ("TEST".equalsIgnoreCase(mode)) {
// log.info("Current app_mode: TEST. Skipping real Gemini API and using
// mockresponse.");
// return ValidationResult.fallback();
// }

// log.info("Current app_mode: PRODUCTION. Calling real Gemini API.");

// int maxRetries = 2;
// int attempt = 0;

// while (attempt <= maxRetries) {
// try {
// return executeValidation(content);
// } catch (Exception e) {
// attempt++;
// log.warn("AI Validation attempt {} failed: {}", attempt, e.getMessage());
// if (attempt > maxRetries) {
// log.error("AI Validation failed after {} attempts. Throwing exception.",
// maxRetries);
// throw new RuntimeException("API_FAILED");
// }
// try {
// Thread.sleep(500 * attempt);
// } catch (InterruptedException ie) {
// Thread.currentThread().interrupt();
// }
// }
// }
// throw new RuntimeException("API_FAILED");
// }

// private ValidationResult executeValidation(String content) throws Exception {
// String prompt = """
// Analyze the following content and return ONLY valid JSON.
// Do not include explanations, markdown, or extra text.
// If you cannot determine, use the fallback values.

// Determine:
// - isTechnical (boolean): programming, IT, software related, AI/ML topics, or
// Data Science/Analytics (PowerBI, Tableau)
// - isSpam (boolean): spam, ads, offensive, or irrelevant content
// - category: one of frontend, backend, fullstack, mobile, devops, data, ai,
// ml, security, cloud, general

// JSON structure:
// {"isTechnical": true, "isSpam": false, "category": "backend"}

// Content:
// """
// + content;

// GeminiRequest request = GeminiRequest.of(prompt);
// HttpHeaders headers = new HttpHeaders();
// headers.setContentType(MediaType.APPLICATION_JSON);

// HttpEntity<GeminiRequest> entity = new HttpEntity<>(request, headers);

// ResponseEntity<GeminiResponse> response = restTemplate.postForEntity(
// API_URL + "?key=" + API_KEY,
// entity,
// GeminiResponse.class);

// return parseGeminiResponse(response);
// }

// private ValidationResult parseGeminiResponse(ResponseEntity<GeminiResponse>
// response) throws Exception {
// if (response.getBody() == null || response.getBody().candidates == null
// || response.getBody().candidates.isEmpty()) {
// throw new RuntimeException("Empty response from Gemini API");
// }

// GeminiResponse.Candidate candidate = response.getBody().candidates.get(0);
// if (candidate.content == null || candidate.content.parts == null ||
// candidate.content.parts.isEmpty()) {
// throw new RuntimeException("Invalid candidate structure in Gemini response");
// }

// String rawText = candidate.content.parts.get(0).text;
// String jsonText = extractJson(rawText);

// JsonNode json = objectMapper.readTree(jsonText);
// boolean isTechnical = json.path("isTechnical").asBoolean(true);
// boolean isSpam = json.path("isSpam").asBoolean(false);
// String categoryStr = json.path("category").asText("general");

// ContentCategory category = ContentCategory.fromString(categoryStr);

// return new ValidationResult(isTechnical, isSpam, category.toString());
// }

// private String extractJson(String text) {
// if (text == null)
// return "{}";
// // Extract content between first { and last }
// Pattern pattern = Pattern.compile("\\{.*\\}", Pattern.DOTALL);
// Matcher matcher = pattern.matcher(text);
// if (matcher.find()) {
// return matcher.group();
// }
// return text.trim();
// }

// @Deprecated
// public boolean isTechnicalContent(String content) {
// return validateContent(content).isTechnical();
// }

// // ================= IMAGE VALIDATION =================
// /**
// * Validates if an image contains technical content using Gemini Vision API
// *
// * @param imageBytes The image file as byte array
// * @return true if image contains technical content, false otherwise
// * @throws IOException if image processing fails
// */
// /*
// * public boolean isTechnicalImage(byte[] imageBytes) throws IOException {
// * try {
// * String base64Image = Base64.getEncoder().encodeToString(imageBytes);
// *
// * // Create request for Gemini with image data
// * String prompt = """
// * Analyze this image and determine if it contains technical content
// * (programming, coding, software, technology, algorithms, laptops, servers,
// * code editors, etc.).
// * Respond with ONLY one word: TECHNICAL or NOT_TECHNICAL
// * """;
// *
// * GeminiImageRequest request = new GeminiImageRequest(prompt, base64Image);
// *
// * HttpHeaders headers = new HttpHeaders();
// * headers.setContentType(MediaType.APPLICATION_JSON);
// *
// * HttpEntity<GeminiImageRequest> entity = new HttpEntity<>(request, headers);
// *
// * ResponseEntity<GeminiResponse> response = restTemplate.postForEntity(
// * API_URL + "?key=" + API_KEY,
// * entity,
// * GeminiResponse.class);
// *
// * if (response.getBody() == null || response.getBody().candidates == null
// * || response.getBody().candidates.isEmpty()) {
// * log.error("Empty response from Gemini API while validating image");
// * return false;
// * }
// *
// * String result =
// * response.getBody().candidates.get(0).content.parts.get(0).text;
// *
// * String normalized = result.trim().toUpperCase();
// *
// * boolean isTechnical = normalized.equals("TECHNICAL");
// * // boolean isTechnical = result.toUpperCase().contains("TECHNICAL");
// *
// * log.info("Image validation result: {}", isTechnical ? "TECHNICAL" :
// * "NOT_TECHNICAL");
// * return isTechnical;
// *
// * } catch (Exception e) {
// * log.error("Image validation failed: {}", e.getMessage(), e);
// * return false;
// * }
// * }
// */
// public boolean isTechnicalImage(byte[] imageBytes) {

// try {
// String base64Image = Base64.getEncoder().encodeToString(imageBytes);

// String prompt = """
// Classify this image to determine if it is TECHNICAL.
// TECHNICAL content includes:

// - Programming, code, software, IDEs, terminals.
// - Hardware, laptops, servers, circuits, networking gear.
// - Artificial Intelligence (AI), Machine Learning, Neural Networks.
// - Data Analytics, Dashboards, PowerBI, Tableau, Charts, or SQL tools.
// - Robots, automation, futuristic technology, or AI-themed posters.
// - Screenshots of technical documentation, tools, or tech objects.

// Reply ONLY:
// TECHNICAL
// or
// NOT_TECHNICAL
// """;
// GeminiImageRequest request = new GeminiImageRequest(prompt, base64Image);

// HttpHeaders headers = new HttpHeaders();
// headers.setContentType(MediaType.APPLICATION_JSON);

// HttpEntity<GeminiImageRequest> entity = new HttpEntity<>(request, headers);

// ResponseEntity<GeminiResponse> response = restTemplate.postForEntity(
// API_URL + "?key=" + API_KEY,
// entity,
// GeminiResponse.class);

// if (response.getBody() == null
// || response.getBody().candidates == null
// || response.getBody().candidates.isEmpty()) {

// log.error("Empty Gemini response for image validation");
// return false;
// }

// String result =
// response.getBody().candidates.get(0).content.parts.get(0).text;

// log.info("Gemini RAW IMAGE RESPONSE: {}", result);

// String normalized = result.trim().toUpperCase();

// boolean isTechnical = normalized.contains("TECHNICAL") &&
// !normalized.contains("NOT_TECHNICAL");

// log.info("Image classified as: {}", normalized);

// return isTechnical;

// } catch (Exception e) {
// log.error("Image validation failed", e);

// // Fail-open (recommended for social apps)
// return true;
// }
// }

// // DTO for Gemini Image Request
// /*
// * private static class GeminiImageRequest {
// *
// * @JsonProperty("contents")
// * public List<ImageContent> contents;
// *
// * public GeminiImageRequest(String prompt, String base64Image) {
// * ImageContent content = new ImageContent();
// *
// * // Add text part
// * TextPart textPart = new TextPart();
// * textPart.text = prompt;
// *
// * // Add image part
// * ImagePart imagePart = new ImagePart();
// * imagePart.inlineData = new InlineData();
// * imagePart.inlineData.mimeType = "image/jpeg";
// * imagePart.inlineData.data = base64Image;
// *
// * content.parts = List.of(textPart, imagePart);
// * this.contents = List.of(content);
// * }
// *
// * static class ImageContent {
// *
// * @JsonProperty("parts")
// * public List<?> parts;
// * }
// *
// * static class TextPart {
// *
// * @JsonProperty("text")
// * public String text;
// * }
// *
// * static class ImagePart {
// *
// * @JsonProperty("inline_data")
// * public InlineData inlineData;
// * }
// *
// * static class InlineData {
// *
// * @JsonProperty("mime_type")
// * public String mimeType;
// *
// * @JsonProperty("data")
// * public String data;
// * }
// * }
// */

// private static class GeminiImageRequest {

// @JsonProperty("contents")
// public List<Content> contents;

// public GeminiImageRequest(String prompt, String base64Image) {

// Content content = new Content();

// TextPart textPart = new TextPart();
// textPart.text = prompt;

// ImagePart imagePart = new ImagePart();
// imagePart.inlineData = new InlineData();
// imagePart.inlineData.mimeType = "image/jpeg";
// imagePart.inlineData.data = base64Image;

// content.parts = List.of(textPart, imagePart);
// this.contents = List.of(content);
// }

// static class Content {
// @JsonProperty("parts")
// public List<Object> parts;
// }

// static class TextPart {
// @JsonProperty("text")
// public String text;
// }

// static class ImagePart {

// // ✅ CORRECT FIELD NAME
// @JsonProperty("inline_data")
// public InlineData inlineData;
// }

// static class InlineData {

// // ✅ CORRECT FIELD NAME
// @JsonProperty("mime_type")
// public String mimeType;

// @JsonProperty("data")
// public String data;
// }
// }

// }