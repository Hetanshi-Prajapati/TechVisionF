# 🎯 VIVA Q&A CHEAT-SHEET
**Memorize these answers. Keep this visible during viva. ~15 minutes total viva time.**

---

## **TIER 1: MUST-KNOW (6 questions — almost certainly asked)**

### **Q1: Is your authentication token-based or session-based? Explain.**

**Answer (30 seconds):**
> "**Session-based.** When a user logs in, the server creates an `HttpSession`, stores the user object in it, 
> and sends a `JSESSIONID` cookie to the browser. Every request includes this cookie; the server reads the session. 
> The login response does include a `token` field, but it's just a random string for convenience — **it's not a JWT**. 
> Real authentication happens server-side via the session, not token validation."

**Key phrases to drop:**
- "Server-side session management"
- "HttpSession stores the user object"
- "JSESSIONID cookie proves identity"
- "Token in response is just a convenience identifier"

**If asked "Why session and not JWT?":**
> "Session is simpler for this use case; we have server-side control, easier integration with OAuth2 and Thymeleaf templates. 
> JWT would be stateless and better for mobile/third-party APIs, but we're a browser-based app."

---

### **Q2: Walk me through the login flow. What happens step-by-step?**

**Answer (45 seconds):**
> "1. User enters username and password in the form.
> 2. Frontend POSTs `/api/auth/login` with credentials.
> 3. `AuthController.login()` queries the DB for the user by email or username.
> 4. We check the password using `passwordEncoder.matches()` (BCrypt). If plain-text, we upgrade it to BCrypt.
> 5. If password matches, we **invalidate the old session** and **create a new `HttpSession`**.
> 6. We store the `User` object and a `sessionToken` (UUID) in the session.
> 7. Server sends back a JSON response with user data and the `tokenType: Bearer, authMode: SESSION`.
> 8. Browser receives the response and the `JSESSIONID` cookie (automatically set by the servlet container).
> 9. Next request, browser sends the `JSESSIONID` cookie; server reads the session and knows the user is authenticated."

**Diagram to draw (if asked):**
```
Browser                              Server
  |                                    |
  |-- POST /api/auth/login ---------->|
  |                                    ├─ Find user in DB
  |                                    ├─ Check password (BCrypt)
  |                                    ├─ Create new HttpSession
  |                                    ├─ Store User object in session
  |                                    |
  |<-- 200 OK + JSESSIONID cookie ----|
  |    (+ user data JSON)              |
  |                                    |
  |-- GET /api/auth/users/me -------->| (includes JSESSIONID)
  |    (with JSESSIONID)               ├─ SessionAuthenticationFilter reads session
  |                                    ├─ SecurityContext.setAuthentication()
  |                                    |
  |<-- 200 OK + user data ------------|
```

---

### **Q3: How does the `SessionAuthenticationFilter` work?**

**Answer (30 seconds):**
> "It's a custom Spring filter registered in `SecurityConfig`. 
> **For every request:**
> 1. It checks if Spring Security's `SecurityContext` already has an Authentication.
> 2. If not, it gets the `HttpSession` and looks for a `User` object in the session attribute.
> 3. If found, it builds a `UsernamePasswordAuthenticationToken` with the user's username and authorities 
>    (ROLE_USER if regular, ROLE_ADMIN if admin).
> 4. It sets this authentication in `SecurityContextHolder`.
> 5. At the end of the request, it **clears the SecurityContext** (because sessions are per-thread, 
>    and we're using a servlet container with thread pooling).
> 
> **Result:** Controllers can use `@AuthenticationPrincipal`, `@RequestParam`, or `HttpSession` to access the user."

**Code reference:** `SessionAuthenticationFilter.java` line 28–45 (the `doFilterInternal` method).

---

### **Q4: How is password security handled? Plain-text, hashing, salting?**

**Answer (30 seconds):**
> "**Passwords are hashed using BCrypt** via `BCryptPasswordEncoder`. BCrypt includes:
> - Salt generation (per password)
> - Rounds (default 10) — makes brute-force slow
> - Verification: `passwordEncoder.matches(plaintext, hashedPassword)` compares safely.
>
> **Legacy support:** If an old user's password is plain-text (from before we switched to BCrypt), 
> we detect it in login (not a valid BCrypt format), compare plain-text, and if correct, 
> we **upgrade it to BCrypt** and save it. Over time, all passwords become hashed.
>
> **Storage:** DB column `User.password` stores the BCrypt hash (e.g., `$2a$10$...`)."

**If asked about salt:**
> "BCrypt automatically generates a unique salt per password. The salt is embedded in the hash itself, 
> so even identical passwords produce different hashes."

---

### **Q5: Explain the CSRF protection mechanism.**

**Answer (30 seconds):**
> "We use **`CookieCsrfTokenRepository`** from Spring Security.
> 
> **How it works:**
> 1. Server generates a CSRF token and stores it in a cookie (name: `XSRF-TOKEN` by default, non-httpOnly).
> 2. Browser reads the cookie (JavaScript can access it since it's not httpOnly).
> 3. For state-changing requests (POST, PUT, DELETE), the frontend reads the CSRF cookie and includes it 
>    in the `X-XSRF-TOKEN` header.
> 4. Server checks if the header value matches the cookie value. If mismatch, request is rejected (403).
>
> **Why?** CSRF attacks can't forge cross-site requests because they don't have access to the CSRF cookie 
> (browsers enforce same-origin on cookies). Our frontend must explicitly include it in the header.
>
> **Our implementation:** `CsrfCookieFilter` ensures the token is loaded and available for the response."

**If asked about JavaScript:**
```javascript
// Frontend code to include CSRF token
const csrfToken = document.querySelector('meta[name="_csrf"]')?.content 
  || document.cookie.split('; ').find(row => row.startsWith('XSRF-TOKEN='))?.split('=')[1];

fetch('/api/posts', {
  method: 'POST',
  headers: { 'X-XSRF-TOKEN': csrfToken },
  body: JSON.stringify(postData)
});
```

---

### **Q6: How does OAuth2 login work? What happens on GitHub auth?**

**Answer (45 seconds):**
> "We have `oauth2Login` configured in `SecurityConfig` pointing to `CustomOAuth2SuccessHandler`.
>
> **Flow:**
> 1. User clicks 'Login with GitHub' → redirected to `/oauth2/authorization/github`.
> 2. Spring OAuth2 initiates an authorization code flow: sends user to GitHub with our client ID.
> 3. GitHub displays login/consent screen.
> 4. User approves → GitHub redirects back to our app with an authorization code.
> 5. Backend exchanges the code for an access token (to GitHub's servers).
> 6. Using the token, we fetch the user's profile (email, name, avatar).
> 7. **`CustomOAuth2SuccessHandler.onAuthenticationSuccess()`** is called:
>    - Check if user exists in our DB by email.
>    - If new user: create a `User` record, auto-generate a username, set profile pic.
>    - If returning user: update profile pic if missing.
>    - **Store the `User` object in the `HttpSession`** (same session-based flow).
>    - Redirect to home page or complete-profile page.
> 
> **Result:** User is logged in via the session, just like manual login."

**Key config:** `application.properties` has `spring.security.oauth2.client.registration.github.*` properties.

---

## **TIER 2: LIKELY FOLLOW-UPS (4 questions)**

### **Q7: You mentioned 'remember me'. How does that work?**

**Answer (30 seconds):**
> "It's a persistent login token stored in the DB.
> 
> **On login, if 'Remember me' checkbox is checked:**
> 1. Generate a random UUID token.
> 2. Store it in `User.rememberToken` and set `User.rememberTokenExpiry` to 30 days from now.
> 3. Set a `remember_me` cookie in the browser with this token (httpOnly=true, maxAge=30d).
>
> **On subsequent visits:**
> 1. If `HttpSession` exists, use it (normal flow).
> 2. If session expired but `remember_me` cookie exists, call `/api/auth/remember`.
> 3. Query DB for user with matching token and check expiry.
> 4. If valid and not expired, create a new `HttpSession` with the user.
> 5. If expired, clear the cookie and redirect to login.
>
> **Security:** Token is random (not predictable), stored hashed in DB (? — or plain text, clarify), and set to httpOnly so JavaScript can't steal it."

**Code reference:** `AuthController.login()` lines 222–238 and `AuthController.remember()` lines 431–456.

---

### **Q8: How are files (uploads) stored and served? Where do profile pictures live?**

**Answer (45 seconds):**
> "Files are stored on the **server filesystem** under the `uploads/` directory.
>
> **Architecture:**
> 1. When a user uploads a profile picture, `ProfileImageService.saveProfileImage()` is called.
> 2. We validate the file (JPEG/PNG only, max 1MB).
> 3. We generate a unique filename: `{userId}_{timestamp}.{ext}` (e.g., `1_1715234567890.jpeg`).
> 4. Save the file to disk: `./uploads/profile/1_1715234567890.jpeg`.
> 5. Store the **relative URL** in the DB: `User.profilePic = '/uploads/profile/1_1715234567890.jpeg'`.
>
> **Serving:**
> 1. `WebConfig` maps `/uploads/**` URLs to the filesystem directory:
>    ```java
>    registry.addResourceHandler("/uploads/**")
>            .addResourceLocations("file:" + uploadPath + "/");
>    ```
> 2. Browser requests `GET /uploads/profile/1_1715234567890.jpeg`.
> 3. Spring serves the file from disk.
>
> **Locations:**
> - Profile pics: `./uploads/profile/`
> - Post images: `./uploads/posts/`
> - Plan files: `./uploads/plan-files/`
> - Other uploads: `./uploads/...`"

**If asked about security:**
> "We validate MIME type and file size. We don't execute uploaded files. 
> Filenames are obscured (not user-provided names). In production, we'd use a CDN or S3 for better scale."

---

### **Q9: What's the difference between authorization and authentication in your app?**

**Answer (30 seconds):**
> "**Authentication:** Verifying who the user is. In our app:
> - Login form → password check → create session → user is **authenticated**.
> - `SessionAuthenticationFilter` reads the session and populates Spring Security's `Authentication`.
> - Checked via `HttpSession` or Spring annotations like `@AuthenticationPrincipal`.
>
> **Authorization:** Verifying what the user can do. In our app:
> - Endpoints protected by role checks in `SecurityConfig.authorizeHttpRequests()`.
> - Example: `/api/admin/**` requires `.hasRole(\"ADMIN\")`.
> - If user is authenticated but not admin, they get **403 Forbidden**.
> - `SessionAuthenticationFilter` sets authorities: ROLE_USER for regular users, ROLE_ADMIN for admins.
>
> **In one line:** Authentication = 'Are you Alice?' (YES). Authorization = 'Can Alice access /api/admin?' (NO, she's not admin)."

---

### **Q10: How would you scale sessions to multiple servers? What are the issues?**

**Answer (45 seconds):**
> "**Current issue:** Sessions are stored in **JVM memory** (Tomcat's default session store). 
> If the app restarts, all sessions are lost. If we have multiple servers, each has its own in-memory sessions.
>
> **Solution 1: Sticky Sessions (Load Balancer)**
> - Configure load balancer to route all requests from a user to the **same server**.
> - Problem: Single server failure loses that user's session. Uneven load distribution.
>
> **Solution 2: Distributed Session Store (Redis) ← Recommended**
> - Replace in-memory sessions with **Spring Session + Redis**.
> - Add dependency: `spring-session-data-redis`.
> - Configure: `spring.session.store-type=redis` and point to Redis instance.
> - All servers read/write sessions to a central Redis. Sessions are shared.
> - On restart, sessions persist in Redis (if configured for durability).
>
> **Solution 3: Switch to JWT (Stateless)**
> - Move away from session-based to token-based auth.
> - Server doesn't store state; client sends JWT with each request.
> - No session sharing needed, but JWT can't be invalidated quickly.
>
> **Our recommendation:** Redis + Spring Session for scalability while keeping session control."

**Code hint:**
```xml
<!-- Add to pom.xml -->
<dependency>
  <groupId>org.springframework.session</groupId>
  <artifactId>spring-session-data-redis</artifactId>
</dependency>
```

---

## **TIER 3: DEEP-DIVES (If interviewer is probing)**

### **Q11: How do you prevent password attacks (brute-force, rainbow tables)?**

**Answer (30 seconds):**
> "- **BCrypt rounds:** Each hash is slow (10 rounds by default, takes ~100ms per check). Brute-force is impractical.
> - **Salt:** Unique per password, embedded in hash. Rainbow tables are useless.
> - **No rate limiting (currently):** We don't limit failed attempts. ⚠️ Could add: lock account after 5 failed attempts.
> - **Password requirements:** We don't enforce complexity in the code. ⚠️ Could add validation.
> - **HTTPS:** Passwords transmitted encrypted (in production, not localhost).
> - **Hashing on login:** We never log or print passwords; only hashes."

**If asked what to add:**
> "We should add rate limiting per IP/username (e.g., max 5 failed attempts in 15 minutes, then lock). 
> Spring Security has `FailureHandler` for this."

---

### **Q12: What if a user's session cookie is stolen?**

**Answer (30 seconds):**
> "**Attack:** Attacker copies the `JSESSIONID` cookie and impersonates the user.
>
> **Mitigations we have:**
> - `httpOnly=true` on `JSESSIONID` → JavaScript can't steal it, only network interception.
> - `Secure` flag (in production HTTPS) → cookie only sent over HTTPS.
> - `SameSite=Lax` → cookie not sent on cross-origin requests (CSRF mitigation).
>
> **If stolen despite this:**
> - Session is valid until timeout (default 30min in our config: `server.servlet.session.timeout=30m`).
> - User could call `/api/auth/logout` to invalidate session immediately.
> - Server could implement session binding (IP + User-Agent check), but we don't currently.
>
> **Could add:** Implement session binding — store IP and User-Agent with session, reject if mismatch."

---

### **Q13: How does your AI content moderation work?**

**Answer (30 seconds):**
> "We have two classifiers:
>
> **Text classifier (rule-based):**
> - Check if post is spam (all words are the same → reject).
> - Check if post contains code keywords (def, public, SELECT, console.log, {}, ;, <div> → likely code).
> - Simple keyword matching, not ML-based.
>
> **Image classifier (Keras MobileNetV2):**
> - Trained model on 2,500 images (technical vs. non-technical).
> - Classes: technical (code, diagrams, dashboards, infographics), non-technical (logos, nature, selfies).
> - Input: 224x224 image, output: confidence score for each class.
> - Accept if technical confidence > 0.75, reject otherwise.
> - Logo detection: hardcoded heuristic (aspect ratio, color histogram) to catch logos.
>
> **Integration:**
> - `AIContentValidatorService.isTechnicalImage()` and `isTechnicalText()` called on post creation.
> - If both are non-technical, reject the post.
> - Response: `'❌ Only technical content allowed'`.
>
> **Limitations:** Small dataset (2,500 images), simple rules, no context understanding."

---

## **QUICK REFERENCE — KEY CODE LOCATIONS**

| Topic | File | Lines |
|-------|------|-------|
| Login flow | `AuthController.java` | 261–340 |
| Session filter | `SessionAuthenticationFilter.java` | 24–42 |
| OAuth2 handler | `CustomOAuth2SuccessHandler.java` | 27–100 |
| Security config | `SecurityConfig.java` | 49–177 |
| CSRF filter | `CsrfCookieFilter.java` | 17–23 |
| Profile upload | `ProfileImageService.java` | 26–48 |
| Post creation (auth check) | `PostController.java` | 308–400 |
| Remember-me | `AuthController.login()` | 222–238 |
| Authorization check | `SecurityConfig.authorizeHttpRequests()` | 112–152 |

---

## **MEMORY AIDS — QUICK 1-LINERS TO MEMORIZE**

| Concept | 1-liner |
|---------|---------|
| Auth type | "Session-based: `JSESSIONID` cookie + server-side `HttpSession`" |
| SessionAuthenticationFilter | "Reads session from cookie, builds Authentication, clears after request" |
| CSRF | "Token in cookie + token in header, must match" |
| OAuth2 | "Redirect to GitHub, get code, exchange for token, create user session" |
| Remember-me | "Random token in DB + cookie, auto-restore on next visit" |
| Uploads | "Filesystem under `uploads/`, URL stored in DB, served by `WebConfig`" |
| Authorization | "`/api/admin/**` requires `.hasRole(\"ADMIN\")`" |
| Password | "BCrypt with salt, 10 rounds, ~100ms per check" |
| Scaling | "Replace in-memory sessions with Redis via Spring Session" |
| Limitations | "Session-based not suitable for mobile/APIs; consider JWT for stateless auth" |

---

## **COMMON GOTCHAS — What NOT to say**

❌ **Don't say:** "The token is a JWT."  
✅ **Do say:** "The token is a random session identifier; real auth is via the server-side session."

❌ **Don't say:** "Passwords are stored plain-text."  
✅ **Do say:** "Passwords are hashed with BCrypt; plain-text ones are upgraded on first successful login."

❌ **Don't say:** "Sessions scale automatically."  
✅ **Do say:** "Sessions are in-memory by default; for multiple servers, use Redis or sticky sessions."

❌ **Don't say:** "CSRF doesn't matter if we use cookies."  
✅ **Do say:** "CSRF is why we store the token in a cookie and require it in the header."

❌ **Don't say:** "OAuth2 makes your app stateless."  
✅ **Do say:** "OAuth2 handles initial auth with GitHub; after that, we use sessions like normal login."

---

## **FINAL CHECKLIST BEFORE VIVA**

- [ ] Memorize Q1–Q6 (TIER 1) word-for-word
- [ ] Understand Q7–Q10 (TIER 2) — can explain with 30 seconds prep
- [ ] Skim Q11–Q13 (TIER 3) — only if asked
- [ ] Know code file locations (for "show me in the code")
- [ ] Practice the login flow diagram (draw it from memory)
- [ ] Practice the 1-liners (can rattle them off)
- [ ] Have this doc visible during viva (tabs or printout)
- [ ] Avoid the "gotchas" — reread them before viva

---

## **DELIVERY TIPS**

1. **Answer concisely first** (20 seconds), then wait for follow-up.
2. **Draw diagrams** if asked "how does X work?" — flows are easier to understand visually.
3. **Cite code locations** when you reference implementation ("It's in `AuthController.java`, line 264").
4. **Pause and ask** "Does that answer your question, or would you like more detail?"
5. **If stuck:** "Let me think for a moment..." (better than a wrong answer immediately).
6. **Admit limitations** (e.g., "We don't have rate limiting, but we could add it with X").

---

**Good luck! You've got this. 💪**

