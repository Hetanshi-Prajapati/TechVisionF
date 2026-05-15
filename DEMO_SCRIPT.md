# 📺 LIVE DEMO SCRIPT — Project Presentation
**Total time: ~5–6 minutes**

---

## **SETUP (Before starting)**
- ✅ Spring Boot running: `mvn spring-boot:run` (port 8080)
- ✅ Browser open, DevTools ready (F12 → Network + Console)
- ✅ MySQL running, DB populated with test data
- ✅ Have curl or Postman ready as backup

---

## **PART 1: INTRO & APP OVERVIEW (0:00–0:30)**

### What to say:
> "This is a **social feed platform** built with **Spring Boot 3** and **MySQL**. 
> Users can **sign up, login, upload profile pictures, share posts with images**, 
> and follow each other. The backend uses **session-based authentication** with 
> secure cookies, **OAuth2 for GitHub login**, and **AI content moderation**.
>
> Let me walk through the core flow: how users authenticate, how sessions work, 
> and how data is stored and displayed."

### What to show:
- Open browser to `http://localhost:8080/api/auth/home` (or landing page)
- If not logged in, show login page briefly
- Point out: login form, GitHub OAuth button, signup link

**Talking point:**
"Right now the app is running in **session mode** — when you login, the server creates a session and stores your user data server-side, not in a token."

---

## **PART 2: LOGIN FLOW & SESSION CREATION (0:30–1:45)**

### Demo step 1: Open DevTools Network tab
- Press **F12** → **Network** tab
- Filter: `Fetch/XHR`

### Demo step 2: Perform login
**What to say:**
> "Let me login with a test account. I'll use username 'alice' and password 'secret'. 
> Watch the Network tab as I submit the login form."

**Action:**
- Navigate to `http://localhost:8080/api/auth/login` in new tab (or use login form if available)
- Open DevTools Network tab
- Do the login (manually or via curl if form is not available):
  ```bash
  curl -X POST http://localhost:8080/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"loginIdentifier":"alice","password":"secret"}' \
    -v
  ```
- Show the output or the Network request/response

### What you'll see in response:
```json
{
  "id": 1,
  "username": "alice",
  "email": "alice@example.com",
  "token": "550e8400-e29b-41d4-a716-446655440000",
  "tokenType": "Bearer",
  "authMode": "SESSION",
  "isAdmin": false,
  "role": "USER"
}
```

### What to say while showing response:
> "Notice the response includes a **`token` field**, but look at the **`authMode`** — 
> it says **`SESSION`**, not JWT. This token is just a convenience identifier. 
> The real authentication happens **server-side via the HttpSession**, which is managed 
> by the **JSESSIONID cookie**."

### Demo step 3: Show cookies
- Open DevTools → **Application** (or Storage) → **Cookies** → `http://localhost:8080`
- Point to:
  - ✅ **`JSESSIONID`** cookie (contains session id)
  - ✅ **`XSRF-TOKEN`** cookie (for CSRF protection)
  - (If remember-me checked) ✅ **`remember_me`** cookie

### What to say:
> "See these cookies? The **`JSESSIONID`** is the session ID. When you make any request, 
> the browser sends this cookie back to the server. On the server side, there's a filter 
> called `SessionAuthenticationFilter` that reads the session from memory and populates 
> the security context for that request. **That's how the server knows who you are** — 
> not from a token in the Authorization header, but from the server-side session."

---

## **PART 3: ACCESS PROTECTED ENDPOINT (1:45–2:30)**

### Demo step 4: Call `/api/auth/users/me`

**What to say:**
> "Now that we're logged in, let's access a **protected endpoint** that requires authentication. 
> I'll call `/api/auth/users/me` which returns the current user's profile data."

**Action (curl with cookies):**
```bash
curl http://localhost:8080/api/auth/users/me \
  -H "Cookie: JSESSIONID=<paste-your-JSESSIONID-here>" \
  -v
```

Or use browser dev console:
```javascript
fetch('http://localhost:8080/api/auth/users/me', {
  credentials: 'include'
})
.then(r => r.json())
.then(d => console.log(d))
```

### What you'll see:
```json
{
  "id": 1,
  "username": "alice",
  "email": "alice@example.com",
  "fullName": "Alice User",
  "bio": "Software developer",
  "profilePic": "/uploads/profile/1_1234567890.jpeg",
  "followers": 5,
  "following": 12,
  "postCount": 3,
  "isAdmin": false,
  "hasRememberToken": false
}
```

### What to say:
> "The server returned all the user data **because the session is valid**. 
> The `SessionAuthenticationFilter` checked the `JSESSIONID` cookie, found the session, 
> built a Spring Security `Authentication` object with role `ROLE_USER`, 
> and the controller method knew the user was authenticated."
>
> "If I **remove the JSESSIONID cookie** and call this again, the server will return **401 Unauthorized**."

**Demo (optional):** make the same call with bad/missing JSESSIONID to show 401.

---

## **PART 4: OAUTH2 & REMEMBER-ME (2:30–3:45)**

### Demo step 5: Show OAuth2 button

**What to say:**
> "The app also supports **GitHub OAuth2 login**. When a user clicks the GitHub button, 
> they're redirected to GitHub for authentication. GitHub returns user data (email, name, profile pic), 
> and we store that in the database and in the session."

**Action:**
- Point to the GitHub OAuth button on login page (or show the code)
- Optionally, show the URL: `http://localhost:8080/oauth2/authorization/github`
- Explain: "The `CustomOAuth2SuccessHandler` creates a new `User` record if needed, 
  stores it in the session, and redirects to home."

### Demo step 6: Explain remember-me (without actually triggering OAuth)

**What to say:**
> "If a user checks **'Remember me'** at login, the server creates a long-lived token, 
> stores it in the database (`User.rememberToken`), and sets a `remember_me` cookie in the browser. 
> Next time they visit, even if the `JSESSIONID` expires, they can auto-restore 
> their session from the `remember_me` token via the `/api/auth/remember` endpoint."

**Show in code (optional):**
- Open `AuthController.java` login method → point to lines that handle `rememberMe` flag
- Show DB schema: `User` table has `remember_token` and `remember_token_expiry` columns

---

## **PART 5: FILE UPLOAD & PROFILE PICTURE (3:45–4:45)**

### Demo step 7: Upload & display profile picture

**What to say:**
> "Now let's see how file uploads work. When a user uploads a profile picture, 
> the server saves it to disk under the `uploads/` directory, stores the path in the database, 
> and displays it in the UI."

**Action:**
- Navigate to user profile page (or settings page with profile pic upload)
- Upload a test image (or show the image that's already there)
- Show the image displayed on the page

**What to say while image loads:**
> "In the browser, the `<img>` tag is pointing to `/uploads/profile/1_1234567890.jpeg`. 
> The `WebConfig` class maps `/uploads/**` URLs to the actual filesystem directory. 
> The `ProfileImageService` handles saving the file with a **BCrypt encrypted filename** 
> to prevent collisions, and returns the URL path."

### Demo step 8: Show uploaded file in filesystem (optional)

**What to say:**
> "Let me show you where the file actually lives on disk."

**Action (in terminal):**
```bash
ls -la ./uploads/profile/
# Output: 1_1234567890.jpeg (or similar)
```

---

## **PART 6: CREATE POST WITH IMAGE & AI MODERATION (4:45–5:30)**

### Demo step 9: Create a post

**What to say:**
> "Users can create posts with text, code, images, or links. The backend has an AI system 
> that validates content — it checks if the image is technical (code, diagrams) and if 
> the text is spam or technical. Non-technical content is rejected."

**Action:**
- Navigate to create post page
- Fill in: title, category (e.g., "python"), optional text or code
- Upload an image (e.g., a screenshot of code)
- Submit

**What happens behind the scenes (explain):**
> "The server runs the image through an AI model (Keras MobileNetV2) to classify it 
> as 'technical' or 'non-technical'. If it's a logo or selfie, it gets rejected. 
> Valid posts are saved to the database, the image is stored in `/uploads/posts/`, 
> and the post appears in the feed."

### Demo step 10: Show the post in feed

**What to say:**
> "Here's the post we just created — the image is loaded from the server, 
> and other users can like, comment, and share."

---

## **PART 7: ADMIN PANEL & AUTHORIZATION (5:30–6:00)**

### Demo step 11: Try to access `/api/admin`

**What to say:**
> "Let me show you how **authorization** works. Some endpoints require special roles. 
> The `/api/admin/**` routes require the `ROLE_ADMIN` role."

**Action:**
- As logged-in user (alice), try to access: `http://localhost:8080/api/admin`
- Expected: **403 Forbidden** or **Access Denied** JSON

**What to say:**
> "Alice is a regular user, so she gets a **403 Forbidden** error. 
> But if an admin were logged in, they would see the admin dashboard with user counts, 
> post counts, and report management."

### Demo step 12: Show the authorization in code (optional)

**Point to:**
- `SecurityConfig.java` → `authorizeHttpRequests()` → `"/api/admin/**".hasRole("ADMIN")`
- `SessionAuthenticationFilter` → sets `ROLE_ADMIN` authority if `user.isAdmin()` is true

---

## **WRAP-UP (6:00–6:15)**

### Final talking points:

> "To summarize the **authentication architecture**:
> 1. **Login** creates a server-side `HttpSession` with the user object.
> 2. The browser stores the `JSESSIONID` cookie.
> 3. Each request includes the cookie; the server reads the session.
> 4. The `SessionAuthenticationFilter` populates Spring Security's `Authentication`.
> 5. Controllers check if the user is authenticated via Spring annotations or session checks.
> 6. **CSRF protection** uses a cookie-based token that the frontend must include in POST requests.
> 7. **OAuth2** integrates seamlessly — GitHub login creates a user and a session.
> 8. **Files** are uploaded to disk, URLs stored in the database, mapped by `WebConfig`.
> 9. **AI moderation** validates images and text before posts are published.
> 10. **Role-based authorization** restricts admin features to admin users."

---

## **BACKUP PLAN (if something fails)**

| Failure | Backup |
|---------|--------|
| Login form doesn't work | Use curl to demo login and show response |
| Image doesn't load | Explain it's a path mapping issue (would be fixed in production) |
| DB not connected | Say "DB is configured to sync with Docker; in demo it's running locally" |
| OAuth button unavailable | Show the code in `CustomOAuth2SuccessHandler` and explain flow |
| Admin page broken | Show the error in console, explain it's because user is not admin |

---

## **QUICK REFERENCE — DEMO URLs**

```
# Login (form)
http://localhost:8080/api/auth/login

# Login (API call)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"loginIdentifier":"alice","password":"secret"}'

# Get current user
curl http://localhost:8080/api/auth/users/me \
  -H "Cookie: JSESSIONID=<your-session-id>"

# Home/feed
http://localhost:8080/api/auth/home

# Create post
http://localhost:8080/api/auth/create-post (or form on page)

# Profile
http://localhost:8080/api/auth/profile

# Admin (will fail for non-admin)
http://localhost:8080/api/admin

# OAuth2 GitHub
http://localhost:8080/oauth2/authorization/github

# Logout
curl -X POST http://localhost:8080/api/auth/logout
```

---

## **DEMO CHECKLIST**

- [ ] Spring Boot running (`mvn spring-boot:run`)
- [ ] DB connected and populated with test users (alice, bob, etc.)
- [ ] Browser DevTools ready (Network, Application/Storage)
- [ ] Have curl or Postman as backup
- [ ] Test users created (alice/secret, bob/secret, etc.)
- [ ] Sample images ready for upload (or use existing posts)
- [ ] Admin account ready to show authorization difference
- [ ] Practice timing (aim for 6 minutes)
- [ ] Have this script visible (print or second monitor)

---

## **KEY THINGS TO EMPHASIZE DURING DEMO**

1. **Session-based, not token-based** — point to `JSESSIONID` cookie and explain server-side storage
2. **CSRF protection** — show CSRF token cookie in DevTools
3. **OAuth2 simplicity** — one-click GitHub login without managing extra passwords
4. **File uploads** — URL mapping from `/uploads/**` to filesystem
5. **AI integration** — AI validates images and text before publishing
6. **Authorization** — different endpoints for different roles (ROLE_USER vs ROLE_ADMIN)
7. **Remember-me** — persistent login token in DB, cookie in browser

---

## **COMMON QUESTIONS DURING DEMO**

| Q | Quick answer |
|---|---|
| Where is session data stored? | Server memory (Tomcat's session store); in production, use Redis for distributed sessions |
| What if user closes browser? | `JSESSIONID` is a session cookie (expires when browser closes). Use `remember_me` for persistence. |
| Why not JWT? | Session is simpler for this use case; JWT would be stateless but harder to invalidate quickly. |
| How is password stored? | BCrypt hashed, not plain text. Old plain-text passwords are upgraded to BCrypt on first login. |
| Can a third-party call the API? | Currently session-based, so no. Would need to add JWT or API-key auth for that. |
| What about CORS? | We use `@CrossOrigin` on some controllers; cookies require `allowCredentials=true`. |

