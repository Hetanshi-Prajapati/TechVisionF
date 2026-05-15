# System Architecture — One-slide Overview

## Quick verbal intro
This slide shows the runtime components and the main data/auth flows. Emphasize: session-based auth (JSESSIONID cookie), server-side HttpSession, role-based authorization, file uploads served from `/uploads/**`, and AI moderation as a separate service.

---

```mermaid
flowchart LR
  Browser["Browser / Client\n(Thymeleaf + JS)"] -->|HTTP (cookies: JSESSIONID, XSRF-TOKEN, remember_me)| App["Spring Boot App\nControllers + Services"]
  App --> DB["MySQL\n(users, posts, tokens)"]
  App --> FS["Filesystem: ./uploads/\n(profile, posts, plan-files)"]
  App --> OAuth["OAuth2 Providers\n(GitHub / Google)"]
  App --> AI["AI Moderation Service\n(Flask + Keras)"]

  subgraph Security
    App --> SessionFilter["SessionAuthenticationFilter\n(reads HttpSession -> sets Authentication)"]
    App --> CSRF["CookieCsrfTokenRepository + CsrfCookieFilter"]
  end

  Browser -.->|OAuth redirect| OAuth
  Browser -->|POST /api/auth/login| App
  App -->|POST image bytes| AI
  AI -->|response (technical|non)| App
```
```

---

ASCII fallback (speak this while showing the slide):

Browser (client)
  - sends requests with cookies: JSESSIONID, XSRF-TOKEN, remember_me
      |
      v
Spring Boot App (Java 17)
  - Controllers: AuthController, PostController, AdminController
  - Security: SecurityConfig, SessionAuthenticationFilter, CsrfCookieFilter
  - Services: AuthService, PostImageService, ProfileImageService, AIContentValidatorService
  - Static mapping: WebConfig -> maps `/uploads/**` to filesystem
      |        |        \
      |        |         \--> Filesystem: ./uploads/profile, ./uploads/posts, ./uploads/plan-files
      |        |
      |        \--> MySQL: users, posts, remember_token
      |
      \--> External: OAuth2 (GitHub/Google) for social login
      \--> AI Service (Flask + Keras) for image classification

Key flows to highlight verbally:
- Login: Browser -> POST /api/auth/login -> AuthController validates -> create HttpSession -> server sets JSESSIONID cookie
- Request auth: SessionAuthenticationFilter reads HttpSession -> builds Authentication -> controllers see authenticated user
- Post creation: PostController -> process image -> call AI service -> save image to ./uploads/ -> store URL in DB
- Admin access: SecurityConfig restricts `/api/admin/**` to ROLE_ADMIN

---

Notes for slide:
- Keep sentences short; point to cookies and session box when explaining auth.
- If asked about scaling: mention Spring Session + Redis and using S3/CDN for uploads.

