# Admin API Guide - Complete Solution

## Problem Fixed ✅

**Why accessing `/api/admin` directly in browser shows "Oops! This content cannot be viewed directly":**

1. **String View Return**: The old endpoint returned `"Admin"` (String), telling Spring to render an HTML template
2. **API Route Handling**: Your GlobalExceptionHandler checks if the request is from a browser and tries to render HTML
3. **Request Type Mismatch**: Direct browser navigation sends `Accept: text/html` headers, but `/api/` endpoints expect JSON requests
4. **Result**: Spring tries to find and render the `Admin.html` template, which causes errors in the view resolution process

## Solution Implemented ✅

The `/api/admin` endpoint now:
- ✅ Returns JSON data instead of a view name
- ✅ Properly checks authentication and authorization
- ✅ Returns 403 Forbidden for non-admin users with JSON error
- ✅ Works with REST clients and fetch API

---

## Architecture Overview

```
Browser/Frontend
    ↓
    └─→ fetch() request with session credentials
         ↓
User Login (Session-based)
    ↓
SessionAuthenticationFilter
    ↓
    └─→ Extracts user from session
        └─→ Sets ROLE_ADMIN if user.isAdmin() = true
    ↓
SecurityConfig
    ↓
    └─→ Checks hasRole("ADMIN") for /api/admin/**
         ├─ PASS: Returns JSON response
         └─ FAIL: Returns 403 Forbidden
```

---

## Backend Configuration

### 1. SecurityConfig (Already Configured ✅)

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> {
            CookieCsrfTokenRepository csrfRepo = CookieCsrfTokenRepository.withHttpOnlyFalse();
            csrfRepo.setCookiePath("/");
            csrf.csrfTokenRepository(csrfRepo)
                    .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler());
        })
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
        .addFilterBefore(sessionAuthenticationFilter,
                org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
        .addFilterAfter(csrfCookieFilter, CsrfFilter.class)
        .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/", "/login", "/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers(HttpMethod.GET,
                    "/api/auth/login", "/api/auth/signup", "/api/posts/explore").permitAll()
                .requestMatchers(HttpMethod.POST,
                    "/api/auth/login", "/api/auth/signup").permitAll()
                
                // ADMIN ONLY - This is the key part
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // Other authenticated endpoints
                .requestMatchers("/api/auth/home", "/api/posts/feed").authenticated()
                .anyRequest().authenticated()
        )
        .exceptionHandling(ex -> ex
            .authenticationEntryPoint(customEntryPoint())
            .accessDeniedHandler((request, response, accessDeniedException) -> {
                response.setStatus(403);
                response.setContentType("application/json");
                response.getWriter().write("{\"status\": 403, \"message\":\"Forbidden\"}");
            })
        );
    
    return http.build();
}
```

### 2. SessionAuthenticationFilter (Already Configured ✅)

```java
@Component
public class SessionAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Authentication existingAuthentication = SecurityContextHolder.getContext().getAuthentication();
        if (existingAuthentication == null || !existingAuthentication.isAuthenticated()) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                Object sessionUser = session.getAttribute("user");
                if (sessionUser instanceof User user) {
                    // KEY: Converts user.isAdmin() to ROLE_ADMIN authority
                    List<SimpleGrantedAuthority> authorities = user.isAdmin()
                            ? List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                            : List.of(new SimpleGrantedAuthority("ROLE_USER"));

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            user.getUsername(), null, authorities);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
```

### 3. AdminController (Fixed ✅)

```java
@RestController  // ← Changed from @Controller
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:8080", allowCredentials = "true")
public class AdminController {

    // ... repositories ...

    /** Admin Dashboard: Returns JSON data for the admin panel */
    @GetMapping("")
    public ResponseEntity<?> adminPage(HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(403)
                .body(Map.of("error", "Access Denied", 
                             "message", "You do not have permission to access the admin panel"));
        }
        
        // Get dashboard stats
        List<User> allUsers = userRepository.findAll();
        List<Post> allPosts = postRepository.findAll();
        long pendingReports = postReportRepository.countByStatus(PostReport.Status.PENDING);
        
        AppSettings modeSetting = appSettingsRepository.findBySettingKey("app_mode").orElse(null);
        String appMode = (modeSetting != null) ? modeSetting.getSettingValue() : "PRODUCTION";
        
        return ResponseEntity.ok(Map.of(
            "dashboard", Map.of(
                "totalUsers", allUsers.size(),
                "activeUsers", allUsers.stream().filter(u -> !u.isAdmin()).count(),
                "totalPosts", allPosts.size(),
                "pendingReports", pendingReports,
                "appMode", appMode
            ),
            "message", "Admin dashboard data retrieved successfully"
        ));
    }

    // Other admin endpoints...
    // GET    /api/admin/users       - List all users (paginated)
    // GET    /api/admin/posts       - List all posts (paginated)
    // GET    /api/admin/reports     - List all reports
    // GET    /api/admin/stats       - Get analytics stats
}
```

---

## Frontend Implementation

### ⚠️ IMPORTANT: Do NOT access `/api/admin` directly in browser!

**Wrong:**
```
❌ Visit: http://localhost:8080/api/admin
   Result: Error - Browser expects HTML, API returns JSON
```

**Correct:**
```
✅ Call API via fetch() with proper headers
   Result: Get JSON data, render admin panel with JavaScript
```

### Example 1: Fetch Admin Dashboard Data

```javascript
// admin.js - Load admin dashboard
async function loadAdminDashboard() {
    try {
        const response = await fetch('/api/admin', {
            method: 'GET',
            credentials: 'include', // IMPORTANT: Include session cookies
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            }
        });

        if (response.status === 403) {
            const error = await response.json();
            document.body.innerHTML = `
                <div style="padding: 20px; color: red;">
                    <h1>Access Denied</h1>
                    <p>${error.message}</p>
                    <a href="/api/auth/home">Go to Home</a>
                </div>`;
            return;
        }

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }

        const data = await response.json();
        console.log('Admin Dashboard Data:', data);
        
        // Render dashboard with the data
        renderAdminDashboard(data.dashboard);
        
    } catch (error) {
        console.error('Failed to load admin dashboard:', error);
        document.body.innerHTML = `
            <div style="padding: 20px; color: red;">
                <h1>Error Loading Dashboard</h1>
                <p>${error.message}</p>
                <button onclick="location.reload()">Retry</button>
            </div>`;
    }
}

function renderAdminDashboard(dashboard) {
    const container = document.getElementById('admin-container');
    container.innerHTML = `
        <div class="admin-header">
            <h1>Admin Dashboard</h1>
            <p>Welcome Admin!</p>
        </div>
        
        <div class="stats">
            <div class="stat-card">
                <h3>Total Users</h3>
                <p class="stat-value">${dashboard.totalUsers}</p>
            </div>
            <div class="stat-card">
                <h3>Active Users</h3>
                <p class="stat-value">${dashboard.activeUsers}</p>
            </div>
            <div class="stat-card">
                <h3>Total Posts</h3>
                <p class="stat-value">${dashboard.totalPosts}</p>
            </div>
            <div class="stat-card">
                <h3>Pending Reports</h3>
                <p class="stat-value">${dashboard.pendingReports}</p>
            </div>
        </div>
        
        <div class="admin-mode">
            <h3>App Mode: <strong>${dashboard.appMode}</strong></h3>
        </div>
        
        <div class="admin-actions">
            <button onclick="loadUsers()">Manage Users</button>
            <button onclick="loadPosts()">Manage Posts</button>
            <button onclick="loadReports()">View Reports</button>
            <button onclick="loadStats()">View Analytics</button>
        </div>
    `;
}

// Call this when page loads
document.addEventListener('DOMContentLoaded', loadAdminDashboard);
```

### Example 2: Fetch Users

```javascript
async function loadUsers(page = 0, size = 10) {
    try {
        const response = await fetch(`/api/admin/users?page=${page}&size=${size}`, {
            credentials: 'include',
            headers: {
                'Accept': 'application/json'
            }
        });

        if (response.status === 401) {
            alert('Please login first');
            window.location.href = '/api/auth/login';
            return;
        }

        if (response.status === 403) {
            alert('You do not have permission to access this page');
            window.location.href = '/api/auth/home';
            return;
        }

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }

        const data = await response.json();
        console.log('Users:', data);
        
        // Render users table
        renderUsersTable(data.users);
        
    } catch (error) {
        console.error('Error loading users:', error);
        alert('Failed to load users: ' + error.message);
    }
}

function renderUsersTable(users) {
    const container = document.getElementById('users-container');
    let html = '<table><tr><th>ID</th><th>Username</th><th>Email</th><th>Admin</th><th>Action</th></tr>';
    
    users.forEach(user => {
        html += `<tr>
            <td>${user.id}</td>
            <td>${user.username}</td>
            <td>${user.email}</td>
            <td>${user.admin ? '✅ Yes' : '❌ No'}</td>
            <td><button onclick="deleteUser(${user.id})">Delete</button></td>
        </tr>`;
    });
    
    html += '</table>';
    container.innerHTML = html;
}

async function deleteUser(userId) {
    if (!confirm('Delete this user?')) return;
    
    try {
        const response = await fetch(`/api/admin/users/${userId}`, {
            method: 'DELETE',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            alert('User deleted successfully');
            loadUsers();
        } else {
            const error = await response.json();
            alert('Error: ' + error.message);
        }
    } catch (error) {
        console.error('Delete error:', error);
    }
}
```

### Example 3: Fetch Reports

```javascript
async function loadReports(status = 'all') {
    try {
        const response = await fetch(`/api/admin/reports?status=${status}`, {
            credentials: 'include',
            headers: {
                'Accept': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }

        const data = await response.json();
        console.log('Reports:', data);
        
        // Render reports
        renderReports(data.reports, data.pendingCount);
        
    } catch (error) {
        console.error('Error loading reports:', error);
    }
}

function renderReports(reports, pendingCount) {
    const container = document.getElementById('reports-container');
    let html = `<h2>Reports (${pendingCount} Pending)</h2>`;
    html += '<div class="reports-list">';
    
    reports.forEach(report => {
        html += `<div class="report-card">
            <p><strong>Post:</strong> ${report.postContent}</p>
            <p><strong>Reason:</strong> ${report.reason}</p>
            <p><strong>Status:</strong> ${report.status}</p>
            <button onclick="ignoreReport(${report.id})">Ignore</button>
            <button onclick="removePost(${report.id})">Remove Post</button>
        </div>`;
    });
    
    html += '</div>';
    container.innerHTML = html;
}
```

### Example 4: Complete HTML Page

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Panel</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: Arial, sans-serif;
            background: #f5f5f5;
            padding: 20px;
        }
        
        .container {
            max-width: 1200px;
            margin: 0 auto;
            background: white;
            border-radius: 8px;
            padding: 20px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        
        .admin-header {
            margin-bottom: 30px;
            padding-bottom: 20px;
            border-bottom: 2px solid #007bff;
        }
        
        .admin-header h1 {
            color: #333;
            margin-bottom: 10px;
        }
        
        .stats {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 20px;
            margin: 30px 0;
        }
        
        .stat-card {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 20px;
            border-radius: 8px;
            text-align: center;
        }
        
        .stat-card h3 {
            font-size: 14px;
            margin-bottom: 10px;
            opacity: 0.9;
        }
        
        .stat-value {
            font-size: 32px;
            font-weight: bold;
        }
        
        .admin-actions {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
            gap: 10px;
            margin: 30px 0;
        }
        
        button {
            padding: 10px 20px;
            background: #007bff;
            color: white;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-size: 14px;
        }
        
        button:hover {
            background: #0056b3;
        }
        
        table {
            width: 100%;
            border-collapse: collapse;
            margin: 20px 0;
        }
        
        table thead {
            background: #f8f9fa;
        }
        
        table th,
        table td {
            padding: 12px;
            text-align: left;
            border-bottom: 1px solid #dee2e6;
        }
        
        table tbody tr:hover {
            background: #f8f9fa;
        }
        
        .error {
            background: #f8d7da;
            color: #721c24;
            padding: 15px;
            border-radius: 4px;
            margin: 20px 0;
        }
        
        .success {
            background: #d4edda;
            color: #155724;
            padding: 15px;
            border-radius: 4px;
            margin: 20px 0;
        }
    </style>
</head>
<body>
    <div class="container">
        <div id="admin-container">
            <p>Loading admin dashboard...</p>
        </div>
        
        <div id="users-container"></div>
        <div id="posts-container"></div>
        <div id="reports-container"></div>
        <div id="stats-container"></div>
    </div>

    <script>
        // All the JavaScript code from examples above
        // ...
    </script>
</body>
</html>
```

---

## API Endpoints Reference

| Method | Endpoint | Description | Requires |
|--------|----------|-------------|----------|
| GET | `/api/admin` | Get dashboard stats | ROLE_ADMIN |
| GET | `/api/admin/users` | List all users (paginated) | ROLE_ADMIN |
| GET | `/api/admin/posts` | List all posts (paginated) | ROLE_ADMIN |
| GET | `/api/admin/reports` | List all reports | ROLE_ADMIN |
| GET | `/api/admin/stats` | Get analytics data | ROLE_ADMIN |
| GET | `/api/admin/mode` | Get app mode | ROLE_ADMIN |
| POST | `/api/admin/mode` | Set app mode | ROLE_ADMIN |
| DELETE | `/api/admin/users/{id}` | Delete user | ROLE_ADMIN |
| PUT | `/api/admin/users/{id}/role` | Update user role | ROLE_ADMIN |
| DELETE | `/api/admin/posts/{id}` | Delete post | ROLE_ADMIN |
| PUT | `/api/admin/posts/{id}/restore` | Restore post | ROLE_ADMIN |
| GET | `/api/admin/posts/{id}` | Get post details | ROLE_ADMIN |
| PUT | `/api/admin/reports/{id}/ignore` | Ignore report | ROLE_ADMIN |
| PUT | `/api/admin/reports/{id}/remove` | Remove post via report | ROLE_ADMIN |

---

## Error Handling

### 401 Unauthorized
```json
{
  "status": 401,
  "message": "Unauthorized"
}
```
**Solution**: Login first via `/api/auth/login`

### 403 Forbidden
```json
{
  "error": "Access Denied",
  "message": "You do not have permission to access the admin panel"
}
```
**Solution**: User must be an admin. Contact server administrator to change user role.

### 404 Not Found
```json
{
  "status": 404,
  "message": "Post not found"
}
```

### 500 Server Error
```json
{
  "status": 500,
  "message": "Something went wrong. Please try again later."
}
```

---

## Testing Checklist

- [ ] Admin user can login successfully
- [ ] Session is created and persisted
- [ ] `/api/admin` returns JSON with dashboard stats (403 code fixed)
- [ ] `/api/admin/users` returns paginated user list
- [ ] `/api/admin/posts` returns paginated post list
- [ ] `/api/admin/reports` returns report list
- [ ] Non-admin users get 403 Forbidden error
- [ ] Unauthenticated users get 401 Unauthorized error
- [ ] Frontend fetch requests include `credentials: 'include'`
- [ ] Frontend fetch requests include proper Accept headers

---

## Common Issues & Solutions

### Issue: Still getting "Oops! This content cannot be viewed directly"
- **Cause**: Browser is accessing `/api/admin` directly
- **Solution**: Use JavaScript fetch() instead of direct navigation

### Issue: 401 Unauthorized even after login
- **Cause**: Session cookie not sent with request
- **Solution**: Add `credentials: 'include'` to fetch options

### Issue: 403 Forbidden for admin user
- **Cause**: User's `isAdmin` field is false in database
- **Solution**: Update user record: `UPDATE users SET is_admin = true WHERE id = ?`

### Issue: CORS errors
- **Cause**: Frontend and backend on different origins
- **Solution**: @CrossOrigin is configured; ensure origins match in annotation

---

## Session vs JWT (Reference)

Your app uses **Session-based authentication** (current):
- ✅ User logs in via `/api/auth/login`
- ✅ Server creates session and stores user in `session.setAttribute("user", user)`
- ✅ SessionAuthenticationFilter extracts user from session on each request
- ✅ Roles are derived from `user.isAdmin()` field

If you want to switch to **JWT** in the future:
- Add JWT library: `jjwt`
- Create JWT token on login
- Send token in `Authorization: Bearer <token>` header
- Create JwtAuthenticationFilter to extract and validate token
- Update SecurityConfig to use JWT filter instead of session filter
