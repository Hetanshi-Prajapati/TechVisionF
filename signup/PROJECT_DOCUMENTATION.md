# Technical Project Documentation: Signup Feed Application

## Executive Summary

This is a **Social Media Feed Platform** built with **Spring Boot 3.2.0** and **Java 17**. It's a full-stack web application that allows users to create accounts, build profiles, share posts (with code snippets, images, links), follow other users, engage with content through likes/comments, and discover trending content.

---

## 1. PROJECT OVERVIEW

### Purpose
A skill-sharing social networking platform where developers can:
- Create accounts with professional profiles
- Share posts about programming topics
- Follow other developers
- Like and comment on posts
- Search for users and content
- Explore trending posts by categories
- Manage their feed based on followed users and interests

### Key Features
1. **User Authentication & Authorization**
   - Signup/Login with email validation
   - Password encryption using Spring Security
   - OAuth2 integration (GitHub login)
   - Admin role management

2. **User Profiles**
   - Profile completion with GitHub username and primary skill
   - Profile image upload
   - Follower/following counts
   - Social statistics

3. **Posts & Feed**
   - Create posts with: text content, code snippets, images, external links
   - Categorized by technology skill (Java, Python, JavaScript, etc.)
   - Like system with tracking
   - Comment system
   - Soft delete (posts marked as deleted, not permanently removed)
   - Feed algorithm showing:
     - Posts from followed users first
     - Posts from users with similar skills
     - Trending/new posts

4. **Search & Discovery**
   - Search users by username
   - Search posts by category
   - Explore trending posts
   - Landing page with featured content

5. **Admin Panel**
   - View all users
   - View all posts
   - Manage user roles
   - Delete/report posts

---

## 2. TECHNOLOGY STACK

### Backend
- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Build Tool**: Maven 3.x

### Key Dependencies
| Dependency | Purpose |
|------------|---------|
| Spring Boot Web Starter | REST API & Web MVC |
| Spring Data JPA | Database ORM |
| Thymeleaf | Server-side HTML templating |
| Spring Validation | Input validation |
| Spring Security Crypto | Password encryption |
| Spring OAuth2 Client | GitHub OAuth login |
| Spring Mail | Email notifications |
| MySQL Connector | MySQL database driver |

### Database
- **MySQL 8.0+**
- **JDBC Connection**: `localhost:3306`
- **Database Name**: `signup_db`
- **ORM**: JPA with Hibernate

### Frontend
- **Templating**: Thymeleaf HTML templates
- **Styling**: CSS (static/css/)
- **Scripting**: JavaScript (static/js/)
- **CSRF Protection**: Custom CSRF token management (csrf.js)

### Security
- **Password Encryption**: Spring Security PasswordEncoder (BCrypt)
- **CSRF Protection**: CsrfCookieFilter
- **Session Management**: SessionAuthenticationFilter
- **CORS**: Enabled for localhost:8080
- **OAuth2**: GitHub authentication

---

## 3. PROJECT STRUCTURE

```
src/main/java/com/example/signup/
├── SignupApplication.java           [ENTRY POINT - Main Spring Boot app]
├── config/                          [CONFIGURATION BEANS]
│   ├── SecurityConfig.java         [Security & CSRF config]
│   ├── AppConfig.java              [General app configuration]
│   ├── WebConfig.java              [Web MVC configuration]
│   ├── CsrfCookieFilter.java       [CSRF token filtering]
│   ├── SessionAuthenticationFilter [Session management]
│   └── CustomOAuth2SuccessHandler  [OAuth2 callback handler]
│
├── controller/                      [HTTP REQUEST HANDLERS]
│   ├── AuthController.java         [/api/auth - Login/Signup]
│   ├── PostController.java         [/api/posts - Post CRUD & Feed]
│   ├── ProfileController.java      [/api/profile - User profiles]
│   ├── SearchController.java       [/api/search - Search functionality]
│   ├── LandingController.java      [/ - Home page]
│   └── AdminController.java        [/admin - Admin panel]
│
├── entity/                          [DATABASE ENTITY CLASSES - JPA]
│   ├── User.java                   [User account data]
│   ├── Post.java                   [Post/feed content]
│   ├── Like.java                   [Like on posts]
│   ├── Follow.java                 [Following relationships]
│   ├── Comment.java                [Comments on posts]
│   ├── PostReport.java             [Post reporting/moderation]
│   └── AppSettings.java            [App-wide configuration]
│
├── repository/                      [DATA ACCESS LAYER - JPA Repositories]
│   ├── UserRepository.java         [User CRUD queries]
│   ├── PostRepository.java         [Post CRUD queries]
│   ├── LikeRepository.java         [Like queries]
│   ├── FollowRepository.java       [Following queries]
│   ├── CommentRepository.java      [Comment queries]
│   └── PostReportRepository.java   [Report queries]
│
├── service/                         [BUSINESS LOGIC LAYER]
│   └── ProfileImageService.java    [Image upload/storage logic]
│
└── dto/                             [DATA TRANSFER OBJECTS]
    ├── SignupRequest.java          [Signup request structure]
    ├── LoginRequest.java           [Login request structure]
    └── LoginResponse.java          [Login response structure]

src/main/resources/
├── application.properties           [Main configuration (DB, port, SMTP)]
├── application-secret.properties   [Sensitive data (API keys, passwords)]
├── templates/                       [Thymeleaf HTML templates]
│   ├── index.html                  [Landing page]
│   ├── signup.html                 [Signup form]
│   ├── Login.html                  [Login form]
│   ├── Home.html                   [Main feed page]
│   ├── Explore.html                [Trending posts]
│   ├── Search.html                 [Search results]
│   ├── Profile.html                [User profile]
│   ├── edit-profile.html           [Edit profile form]
│   ├── CompleteProfile.html        [Profile completion after signup]
│   ├── Admin.html                  [Admin dashboard]
│   ├── Settings.html               [User settings]
│   ├── Privacy.html                [Privacy policy]
│   ├── Terms.html                  [Terms of service]
│   └── Reset.html                  [Password reset]
│
├── static/
│   ├── js/
│   │   └── csrf.js                 [CSRF token handling]
│   ├── css/                        [Stylesheets]
│   └── images/                     [Static images]
│
└── db/migration/                   [Database migration scripts]

uploads/                            [FILE STORAGE - NOT IN VERSION CONTROL]
├── posts/                          [Post attachments (images, files)]
└── profile/                        [User profile pictures]
```

---

## 4. DATA MODEL (Entity Relationships)

### Core Entities

#### User Entity
```java
- id (PK)
- fullName (String)
- username (String, UNIQUE)
- email (String, UNIQUE)
- password (String, encrypted)
- githubUsername (String, optional)
- primarySkill (String, e.g., "Java", "Python")
- isAdmin (Boolean)
- followers (Integer count)
- following (Integer count)
- createdAt (LocalDateTime)
```

#### Post Entity
```java
- id (PK)
- authorId (FK → User.id)
- author (String, username)
- content (String, max 2000 chars)
- category (String, tech skill)
- createdAt (LocalDateTime)
- likeCount (Integer)
- commentCount (Integer)
- code (String, optional code snippet)
- imageUrl (String, optional)
- linkUrl (String, optional)
- deleted (Boolean, soft delete flag)
```

#### Like Entity
```java
- id (PK)
- userId (FK → User.id)
- postId (FK → Post.id)
- createdAt (LocalDateTime)
- UNIQUE: (userId, postId) - User can like post only once
```

#### Follow Entity
```java
- id (PK)
- followerId (FK → User.id) - who is following
- followingId (FK → User.id) - who is being followed
- createdAt (LocalDateTime)
- UNIQUE: (followerId, followingId)
```

#### Comment Entity
```java
- id (PK)
- postId (FK → Post.id)
- userId (FK → User.id)
- content (String)
- createdAt (LocalDateTime)
```

#### PostReport Entity
```java
- id (PK)
- postId (FK → Post.id)
- reportedBy (FK → User.id)
- reason (String)
- createdAt (LocalDateTime)
```

---

## 5. API ENDPOINTS

### Authentication (`/api/auth`)
| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/api/auth/signup` | Show signup form |
| POST | `/api/auth/signup` | Register new user |
| GET | `/api/auth/login` | Show login form |
| POST | `/api/auth/login` | Authenticate user |
| GET | `/api/auth/logout` | Logout user (destroy session) |
| GET | `/api/auth/oauth2/callback/github` | OAuth2 callback |

### Posts (`/api/posts`)
| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/api/posts` | Get user's home feed |
| GET | `/api/posts/explore` | Get trending posts |
| GET | `/api/posts/category/{category}` | Get posts by category |
| POST | `/api/posts/create` | Create new post |
| POST | `/api/posts/{id}/like` | Like a post |
| POST | `/api/posts/{id}/unlike` | Unlike a post |
| POST | `/api/posts/{id}/comment` | Add comment |
| DELETE | `/api/posts/{id}` | Delete post (soft delete) |

### Profile (`/api/profile`)
| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/api/profile/{username}` | View user profile |
| GET | `/api/profile/me` | View own profile |
| GET | `/api/profile/{username}/posts` | Get user's posts |
| POST | `/api/profile/edit` | Update profile |
| POST | `/api/profile/upload-image` | Upload profile picture |

### Search (`/api/search`)
| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/api/search/users?q={query}` | Search users |
| GET | `/api/search/posts?q={query}` | Search posts |

### Admin (`/admin`)
| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/admin` | Admin dashboard |
| GET | `/admin/users` | List all users |
| GET | `/admin/posts` | List all posts |
| POST | `/admin/posts/{id}/delete` | Permanently delete post |
| POST | `/admin/users/{id}/role` | Change user role |

### Landing
| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/` | Home page (not logged in) |
| GET | `/Home` | Main feed (logged in) |
| GET | `/Explore` | Trending page |

---

## 6. WORKFLOW: USER JOURNEY

### 1. Signup/Registration Flow
```
User opens index.html
    ↓
User fills signup form (fullName, email, username, password)
    ↓
AuthController.signup() receives POST request
    ↓
Validate input (email format, password strength, unique username/email)
    ↓
Hash password using BCrypt (Spring Security PasswordEncoder)
    ↓
Create User entity and save to database
    ↓
Send confirmation email (Java Mail Sender)
    ↓
Redirect to CompleteProfile.html
    ↓
User enters GitHub username and primary skill
    ↓
Update User entity in database
    ↓
Redirect to Login.html
```

### 2. Login/Session Flow
```
User enters username/email and password
    ↓
AuthController.login() validates credentials
    ↓
Compare provided password with stored hash
    ↓
On match: Create HTTP Session
    ↓
Store User data in session (HttpSession.setAttribute())
    ↓
SessionAuthenticationFilter intercepts and validates session
    ↓
Return JWT or Session token
    ↓
Redirect to Home.html
    ↓
Client browser stores session cookie
```

### 3. Post Creation Flow
```
User on Home.html clicks "Create Post"
    ↓
User fills post form (content, category, optional code/image/link)
    ↓
JavaScript adds CSRF token (csrf.js)
    ↓
POST /api/posts/create sent to server
    ↓
CsrfCookieFilter validates CSRF token
    ↓
PostController.createPost() receives request
    ↓
Validate post content (not empty, valid category)
    ↓
If image: ProfileImageService saves image to uploads/posts/
    ↓
Create Post entity with authorId, category, timestamp
    ↓
Save Post to database via PostRepository
    ↓
Return success response
    ↓
JavaScript updates Home.html feed with new post
```

### 4. Feed Algorithm Flow
```
User visits Home.html
    ↓
PostController.getHomeFeed() executes
    ↓
Priority 1: Get all posts from users current user follows
    ↓
    Query: SELECT * FROM posts WHERE authorId IN (SELECT followingId FROM follow WHERE followerId = currentUserId)
    ↓
Priority 2: If feed empty, add posts by users with same primary skill
    ↓
    Query: SELECT * FROM posts WHERE category = currentUser.primarySkill
    ↓
Priority 3: If still sparse, add trending posts (by like count)
    ↓
    Query: SELECT * FROM posts ORDER BY like_count DESC LIMIT 20
    ↓
Sort by timestamp (newest first)
    ↓
Return posts array to Home.html
    ↓
Thymeleaf template renders each post
```

### 5. Like Flow
```
User clicks "Like" button on a post
    ↓
JavaScript sends POST /api/posts/{postId}/like
    ↓
CSRF token included in request
    ↓
PostController.likePost() executes
    ↓
Check if like already exists: LikeRepository.findByUserIdAndPostId()
    ↓
If not exists:
    - Create Like entity
    - Increment Post.likeCount
    - Save Like record
    ↓
If exists (already liked):
    - Delete Like record
    - Decrement Post.likeCount
    ↓
Update Post entity in database
    ↓
Return updated likeCount to JavaScript
    ↓
Update UI: Change button color, update count display
```

### 6. Follow Flow
```
User visits another user's profile (Profile.html/{username})
    ↓
ProfileController.getProfile() loads user data
    ↓
Check if current user already follows: FollowRepository.findByFollowerIdAndFollowingId()
    ↓
Pass "isFollowing" flag to template
    ↓
User clicks "Follow" button
    ↓
JavaScript sends POST /api/profile/{userId}/follow
    ↓
ProfileController.toggleFollow() executes
    ↓
If not following:
    - Create Follow entity
    - Increment followingCount for current user
    - Increment followersCount for target user
    ↓
If already following:
    - Delete Follow entity
    - Decrement counts
    ↓
Update both User records
    ↓
Return new counts
    ↓
Update UI
```

---

## 7. SECURITY ARCHITECTURE

### Authentication Layers

#### 1. HTTP Session Layer
- **SessionAuthenticationFilter**: Intercepts every request
- Validates session exists in HttpSession
- Checks user is not null
- If invalid → redirect to login

#### 2. CSRF Protection
- **CsrfCookieFilter**: Generates unique token per session
- **csrf.js**: Automatically includes token in all POST/PUT/DELETE requests
- Server validates token matches session token
- Prevents cross-site request forgery attacks

#### 3. Password Security
- **BCrypt Hashing**: Passwords hashed with random salt
- **Spring Security PasswordEncoder**: Never store plaintext
- Comparison uses secure algorithm (doesn't leak timing info)

#### 4. OAuth2 Integration
- **Spring OAuth2 Client**: Authentication via GitHub account
- **CustomOAuth2SuccessHandler**: Receives GitHub user data after authentication
- Creates or updates User record
- Creates session automatically

### Authorization Levels

```
ROLE_USER (default)
├── Read own profile
├── Create/edit own posts
├── Like/comment on posts
└── Follow users

ROLE_ADMIN
├── All ROLE_USER permissions
├── View all users/posts
├── Delete any post
├── Change user roles
└── Access /admin dashboard
```

---

## 8. DATABASE SCHEMA

### Users Table
```sql
CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  full_name VARCHAR(255) NOT NULL,
  username VARCHAR(255) UNIQUE NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  github_username VARCHAR(255),
  primary_skill VARCHAR(255),
  is_admin BOOLEAN DEFAULT FALSE,
  followers INT DEFAULT 0,
  following INT DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Posts Table
```sql
CREATE TABLE posts (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  author_id BIGINT NOT NULL,
  author VARCHAR(255) NOT NULL,
  content VARCHAR(2000),
  category VARCHAR(50) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  like_count INT DEFAULT 0,
  comment_count INT DEFAULT 0,
  code VARCHAR(4000),
  image_url VARCHAR(1000),
  link_url VARCHAR(1000),
  deleted BOOLEAN DEFAULT FALSE,
  FOREIGN KEY (author_id) REFERENCES users(id)
);
```

### Likes Table
```sql
CREATE TABLE likes (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  post_id BIGINT NOT NULL,
  created_at TIMESTAMP,
  UNIQUE(user_id, post_id),
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (post_id) REFERENCES posts(id)
);
```

### Follow Table
```sql
CREATE TABLE follow (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  follower_id BIGINT NOT NULL,
  following_id BIGINT NOT NULL,
  created_at TIMESTAMP,
  UNIQUE(follower_id, following_id),
  FOREIGN KEY (follower_id) REFERENCES users(id),
  FOREIGN KEY (following_id) REFERENCES users(id)
);
```

### Comments Table
```sql
CREATE TABLE comments (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  content VARCHAR(1000),
  created_at TIMESTAMP,
  FOREIGN KEY (post_id) REFERENCES posts(id),
  FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### Post Reports Table
```sql
CREATE TABLE post_reports (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_id BIGINT NOT NULL,
  reported_by BIGINT NOT NULL,
  reason VARCHAR(255),
  created_at TIMESTAMP,
  FOREIGN KEY (post_id) REFERENCES posts(id),
  FOREIGN KEY (reported_by) REFERENCES users(id)
);
```

---

## 9. CONFIGURATION FILES

### application.properties
```properties
# Server Configuration
server.port=8080
server.servlet.context-path=/

# MySQL Database
spring.datasource.url=jdbc:mysql://localhost:3306/signup_db
spring.datasource.username=root
spring.datasource.password=sonu2607
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update (auto-creates/updates tables)
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
spring.jpa.show-sql=false

# Thymeleaf
spring.thymeleaf.cache=false (disable caching in development)

# Gmail SMTP (for email notifications)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# OAuth2 - GitHub
spring.security.oauth2.client.registration.github.client-id=your_github_client_id
spring.security.oauth2.client.registration.github.client-secret=your_github_client_secret
```

### application-secret.properties
```properties
# Store sensitive data here (not in version control)
# Contains:
# - Database credentials
# - Gmail app password
# - OAuth2 secrets
# - API keys
```

---

## 10. REQUEST/RESPONSE FLOW EXAMPLE

### Example: Create a Post

#### Request
```
POST /api/posts/create
Host: localhost:8080
Content-Type: application/x-www-form-urlencoded
Cookie: JSESSIONID=ABC123; XSRF-TOKEN=XYZ789

content=Hello%20World&category=Java&code=System.out.println();
```

#### Controller Processing
```java
@PostMapping("/create")
public String createPost(
    @RequestParam String content,
    @RequestParam String category,
    @RequestParam(required=false) MultipartFile image,
    HttpSession session) {
    
    User currentUser = (User) session.getAttribute("user");
    
    Post post = new Post();
    post.setAuthorId(currentUser.getId());
    post.setAuthor(currentUser.getUsername());
    post.setContent(content);
    post.setCategory(category);
    post.setCreatedAt(LocalDateTime.now());
    post.setLikeCount(0);
    
    if (image != null) {
        String imageUrl = profileImageService.uploadImage(image);
        post.setImageUrl(imageUrl);
    }
    
    postRepository.save(post);
    return "redirect:/Home";
}
```

#### Response (Redirect)
```
HTTP/1.1 302 Found
Location: /Home
Set-Cookie: JSESSIONID=ABC123; Path=/; HttpOnly
```

#### Home Page Loads
```
GET /Home
Cookie: JSESSIONID=ABC123

Returns: Home.html with Thymeleaf rendering
- Posts fetched from database
- Each post displayed using Thymeleaf loop: <div th:each="post : ${posts}">
- Like/comment buttons populated with postId
```

---

## 11. REQUEST SIZE LIMITS & CONSTRAINTS

| Component | Limit | Purpose |
|-----------|-------|---------|
| Post content | 2000 characters | Prevent DB bloat |
| Code snippet | 4000 characters | Allow substantial code |
| Image URL | 1000 characters | Reasonable file path |
| External link | 1000 characters | URL length |
| Username | 255 characters | Unique identifier |
| Category | 50 characters | Tech skill name |
| Session timeout | 30 minutes (default) | Security |
| CSRF token | Regenerated per request | Security |

---

## 12. RUNNING THE PROJECT

### Prerequisites
1. Java 17+ installed
2. MySQL 8.0+ running on localhost:3306
3. Maven installed (or use mvnw wrapper)

### Setup Steps
```bash
# 1. Create database
mysql -u root -p
CREATE DATABASE signup_db;

# 2. Configure credentials
Edit src/main/resources/application.properties
- Update spring.datasource.password with your MySQL password
- Add Gmail SMTP credentials (if email features needed)

# 3. Run project
./mvnw.cmd spring-boot:run

# 4. Access application
Browser: http://localhost:8080
```

### What Happens on Startup
1. **SignupApplication.main()** runs
2. **Spring Boot initializes**:
   - Loads application.properties
   - Creates database connection pool
   - Scans for @Component, @Controller, @Service beans
3. **JPA/Hibernate**:
   - Connects to MySQL
   - Reads entities
   - Creates tables (if ddl-auto=update)
4. **CommandLineRunner**:
   - Creates default admin user if not exists
   - Fixes any NULL values in database
5. **Server starts** on port 8080
6. **Tomcat embedded web server** ready to accept requests

---

## 13. TESTING

### Test Files
- `src/test/java/ControllerFlowTests.java` - Integration tests for controllers
- `src/test/java/SignupApplicationTests.java` - Basic app startup tests

### How Tests Work
```java
@SpringBootTest
public class ControllerFlowTests {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    public void testSignupFlow() {
        // Send POST to /api/auth/signup
        // Verify user created in database
        // Verify redirect to CompleteProfile
    }
}
```

---

## 14. KEY DESIGN PATTERNS

### 1. MVC Architecture
- **Model**: Entity classes (User, Post, etc.)
- **View**: Thymeleaf HTML templates
- **Controller**: REST endpoints handling requests

### 2. Repository Pattern
- Database queries abstracted in Repository classes
- JPA handles SQL generation
- Easy to swap database or mock for testing

### 3. Dependency Injection
- @Autowired injects beans into controllers/services
- Spring manages lifecycle
- Loose coupling between layers

### 4. Session Management
- HttpSession stores logged-in user data
- SessionAuthenticationFilter validates every request
- Automatic timeout after inactivity

### 5. Soft Delete Pattern
- Posts marked as `deleted=true` instead of being removed
- Allows recovery, audit trail
- Uses `WHERE deleted=FALSE` in queries

### 6. Feed Algorithm Strategy
- Prioritizes content from followed users
- Falls back to similar-interest content
- Finally shows trending content
- Prevents cold-start problem for new users

---

## 15. FILE UPLOAD HANDLING

### Profile Image Upload
```
User uploads image → ProfileImageService.uploadImage(MultipartFile)
    ↓
Generate unique filename (prevents collisions)
    ↓
Save to: uploads/profile/{userId}_{timestamp}_{filename}
    ↓
Return relative URL path
    ↓
Store URL in User.profileImageUrl
    ↓
Display in profile HTML: <img th:src="${user.profileImageUrl}">
```

### Post Image Upload
```
User includes image in post → Same process as profile
    ↓
Save to: uploads/posts/{postId}_{filename}
    ↓
Store URL in Post.imageUrl
    ↓
Display in feed
```

---

## 16. ADMIN PANEL FEATURES

### Dashboard
- Total users registered
- Total posts created
- Active users this month
- Reports pending moderation

### User Management
- View all users
- Search users
- Change user role (USER → ADMIN)
- View user statistics

### Post Management
- View all posts
- Filter by category/author
- Search posts by content
- View flagged/reported posts
- Permanently delete posts

### Moderation
- View post reports
- Reason for report
- Delete reported post
- Ban user (future feature)

---

## 17. EMAIL FUNCTIONALITY

### When Emails Are Sent
1. **After Signup**: Confirmation email with account details
2. **Password Reset**: Reset link (future feature)
3. **Notifications**: User followed, post liked (future feature)

### Email Service
```java
@Autowired
private JavaMailSender mailSender;

// Example: Send confirmation email
SimpleMailMessage message = new SimpleMailMessage();
message.setTo(user.getEmail());
message.setSubject("Welcome to Signup Feed");
message.setText("Your account has been created!");
mailSender.send(message);
```

---

## 18. FUTURE ENHANCEMENTS

1. **Real-time Notifications**: WebSocket for live feed updates
2. **Message System**: Private messaging between users
3. **Verified Badges**: GitHub verification badge
4. **Advanced Search**: Full-text search on post content
5. **Trending Algorithm**: ML-based recommendation system
6. **Analytics**: User engagement metrics
7. **Mobile App**: Native iOS/Android using API
8. **Caching**: Redis for session/feed caching
9. **API Documentation**: Swagger/OpenAPI specification
10. **Rate Limiting**: Prevent spam/DDoS

---

## 19. TROUBLESHOOTING COMMON ISSUES

| Issue | Cause | Solution |
|-------|-------|----------|
| Connection refused to MySQL | MySQL not running | Start MySQL service |
| Access denied on database | Wrong password | Check application.properties |
| Signup page shows 404 | Controller not mapped | Restart application |
| CSRF token invalid | Session expired | Refresh page, login again |
| Profile image not uploading | uploads/ folder missing | Create uploads/profile/ directory |
| OAuth2 callback fails | GitHub credentials wrong | Verify client ID/secret in application.properties |
| Posts not showing in feed | User has no follows | Follow users or create posts yourself |

---

## 20. SUMMARY

**This project is a full-stack Spring Boot social media platform** that demonstrates:
- ✅ User authentication & session management
- ✅ RESTful API design
- ✅ Database modeling with relationships
- ✅ Security best practices (CSRF, passwords)
- ✅ File upload handling
- ✅ Front-end templating with Thymeleaf
- ✅ Admin authorization levels
- ✅ Complex business logic (feed algorithm)

**Perfect for learning:**
- Java/Spring Boot fundamentals
- Web development patterns
- Database design
- Security concepts
- Full-stack development workflow

---

**Project Version**: 0.0.1-SNAPSHOT  
**Built With**: Spring Boot 3.2.0, Java 17  
**Database**: MySQL 8.0+  
**License**: MIT (Typical for demo projects)
