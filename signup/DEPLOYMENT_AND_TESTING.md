# Deployment & Testing Guide

## 🚀 Deployment Steps

### Step 1: Rebuild the Project

```bash
cd d:\update(27)\update(27)\signup

# Clean and compile
mvn clean compile

# Build the JAR (optional, for production)
mvn clean package

# Run the application
mvn spring-boot:run
```

### Step 2: Verify the Application Started
```
Look for:
  ✓ Started SignupApplication in X seconds
  ✓ Tomcat started on port(s): 8080
  ✓ No compilation errors
```

---

## 🧪 Testing Guide

### Test 1: Verify Admin User Exists

```sql
-- Connect to MySQL
USE your_database_name;

-- Check admin user
SELECT id, username, email, is_admin FROM users WHERE is_admin = true;

-- If no admin user, update one:
UPDATE users SET is_admin = true WHERE username = 'your_username' LIMIT 1;

-- Or create test admin:
INSERT INTO users (full_name, username, email, password, is_admin) 
VALUES ('Admin User', 'admin', 'admin@test.com', 'hashed_password', true);
```

### Test 2: Login Test

```bash
# Using curl (Windows PowerShell)
$response = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/login" `
  -Method POST `
  -Headers @{"Content-Type" = "application/json"} `
  -Body '{"username":"admin","password":"yourpassword"}' `
  -SessionVariable session

# Response should show user object with is_admin = true
$response.Content
```

**Expected Response:**
```json
{
  "id": 1,
  "username": "admin",
  "email": "admin@test.com",
  "fullName": "Admin User",
  "isAdmin": true,
  ...
}
```

### Test 3: Direct API Test (After Login)

```bash
# Save session and test /api/admin
$response = Invoke-WebRequest -Uri "http://localhost:8080/api/admin" `
  -Method GET `
  -Headers @{
    "Accept" = "application/json"
    "Content-Type" = "application/json"
  } `
  -WebSession $session

# Check response
$response.StatusCode  # Should be 200
$response.Content | ConvertFrom-Json  # Should be JSON with dashboard data
```

**Expected Response:**
```json
{
  "dashboard": {
    "totalUsers": 10,
    "activeUsers": 8,
    "totalPosts": 42,
    "pendingReports": 3,
    "appMode": "PRODUCTION"
  },
  "message": "Admin dashboard data retrieved successfully"
}
```

### Test 4: Unauthorized Access Test

```bash
# Test without authentication
$response = Invoke-WebRequest -Uri "http://localhost:8080/api/admin" `
  -Method GET `
  -Headers @{"Accept" = "application/json"} `
  -ErrorAction SilentlyContinue

# Check status code
$response.StatusCode  # Should be 401

# Check response
$response.Content  # Should show "Unauthorized"
```

### Test 5: Forbidden Access Test (Non-Admin User)

```bash
# Login as non-admin user
$response = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/login" `
  -Method POST `
  -Headers @{"Content-Type" = "application/json"} `
  -Body '{"username":"regularuser","password":"pass"}' `
  -SessionVariable session

# Try to access /api/admin
$response = Invoke-WebRequest -Uri "http://localhost:8080/api/admin" `
  -Method GET `
  -Headers @{"Accept" = "application/json"} `
  -WebSession $session `
  -ErrorAction SilentlyContinue

# Check status code
$response.StatusCode  # Should be 403

# Check response
$response.Content  # Should show "Access Denied"
```

**Expected Response:**
```json
{
  "error": "Access Denied",
  "message": "You do not have permission to access the admin panel"
}
```

### Test 6: Admin Dashboard HTML Test

```
1. Open browser: http://localhost:8080
2. Login with admin credentials
3. Navigate to: http://localhost:8080/admin-dashboard.html
4. Should see:
   - Dashboard stats (Users, Posts, Reports)
   - Navigation buttons (Users, Posts, Reports, Analytics)
   - Admin controls for managing users/posts
```

---

## 📋 Manual Testing Checklist

### Authentication Tests
- [ ] Non-admin user cannot access `/api/admin` (403 Forbidden)
- [ ] Admin user can access `/api/admin` (200 OK with JSON)
- [ ] Unauthenticated user gets 401 Unauthorized
- [ ] Session persists across requests
- [ ] CSRF token is being used

### API Response Tests
- [ ] `/api/admin` returns JSON (not HTML)
- [ ] Response includes `dashboard` object
- [ ] `dashboard` includes: totalUsers, activeUsers, totalPosts, pendingReports, appMode
- [ ] Error responses are JSON (not HTML)
- [ ] HTTP status codes are correct (200, 401, 403)

### Admin Dashboard Tests
- [ ] Can load `/admin-dashboard.html`
- [ ] Dashboard stats display correctly
- [ ] Can navigate between sections (Users, Posts, Reports, Analytics)
- [ ] User list loads with pagination
- [ ] Post list loads with pagination
- [ ] Reports list shows pending/ignored/removed status
- [ ] Can delete users/posts
- [ ] Can ignore/remove reports
- [ ] Analytics data displays correctly
- [ ] Logout button works

### Other Endpoints Tests
- [ ] `/api/admin/users` returns paginated users
- [ ] `/api/admin/posts` returns paginated posts
- [ ] `/api/admin/reports` returns reports
- [ ] `/api/admin/stats` returns analytics
- [ ] `/api/admin/mode` returns app mode
- [ ] All return JSON (not HTML)

---

## 🔍 Debugging

### Enable Debug Logging

Add to `src/main/resources/application.properties`:

```properties
# Debug Spring Security
logging.level.org.springframework.security=DEBUG

# Debug Spring Web
logging.level.org.springframework.web=DEBUG

# Debug AdminController
logging.level.com.example.signup.controller.AdminController=DEBUG

# Debug SessionAuthenticationFilter
logging.level.com.example.signup.config.SessionAuthenticationFilter=DEBUG
```

### Check Logs for Issues

```bash
# Look for these lines in console output:
# ✓ SessionAuthenticationFilter - User authenticated with ROLE_ADMIN
# ✓ AdminController - Dashboard requested by admin user
# ✓ Response - 200 OK with JSON body

# If you see these, there's a problem:
# ✗ Access is denied (user is not authenticated)
# ✗ Trying to render view name 'Admin'
# ✗ Could not find template location
```

### Browser Developer Tools

```
1. Open http://localhost:8080/admin-dashboard.html
2. Press F12 (Developer Tools)
3. Go to "Network" tab
4. Reload page
5. Look for GET /api/admin request
   - Status: 200 (should be)
   - Response: JSON (should be)
   - Type: application/json (should be)
```

### Common Error Messages & Solutions

#### Error: "Oops! This content cannot be viewed directly"
```
Cause: Still accessing /api/admin directly in browser
Solution: Use /admin-dashboard.html instead
```

#### Error: "Access Denied" / 403 Forbidden
```
Cause 1: User is not admin
Solution: UPDATE users SET is_admin = true WHERE id = ?;

Cause 2: Not logged in
Solution: Login first at /api/auth/login
```

#### Error: "Unauthorized" / 401
```
Cause: Not authenticated
Solution: Login first at /api/auth/login
```

#### Error: "Template 'Admin' not found"
```
Cause: Old code is still running (didn't recompile)
Solution: mvn clean compile && mvn spring-boot:run
```

#### Error: CORS error in console
```
Cause: Frontend and backend origins don't match
Solution: Check @CrossOrigin annotation in AdminController
```

---

## 📊 Performance Testing

### Load Test (Optional)

```bash
# Test with Apache Bench (ab)
ab -n 100 -c 10 -H "Cookie: JSESSIONID=xxx" \
  -H "Accept: application/json" \
  http://localhost:8080/api/admin

# Results should show:
# ✓ 100% success rate (200 OK)
# ✓ Fast response times
# ✓ No 500 errors
```

---

## 🚀 Production Deployment

### Before Deploying to Production:

1. **Security Checklist**
   - [ ] Change CORS origins from localhost to your domain
   - [ ] Enable HTTPS
   - [ ] Set secure session cookies
   - [ ] Use environment variables for sensitive data
   - [ ] Review spring-security configurations

2. **Database Checklist**
   - [ ] Verify admin users exist
   - [ ] Backup existing data
   - [ ] Test on staging environment first

3. **Performance Checklist**
   - [ ] Database queries are optimized
   - [ ] Implement pagination for large datasets
   - [ ] Add caching if needed
   - [ ] Monitor server resources

4. **Documentation Checklist**
   - [ ] Update README with new admin panel URL
   - [ ] Document API endpoints for team
   - [ ] Create admin user management procedures

### Production Configuration

```properties
# application.properties (production)

# Security
server.servlet.session.cookie.secure=true
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.same-site=strict

# Logging
logging.level.root=WARN
logging.level.com.example.signup=INFO

# Performance
server.tomcat.threads.max=200
server.tomcat.threads.min-spare=10
```

### CORS Configuration for Production

```java
@CrossOrigin(origins = "https://yourdomain.com", allowCredentials = "true")
public class AdminController { ... }
```

---

## 📝 Troubleshooting Checklist

| Issue | Status | Solution |
|-------|--------|----------|
| `/api/admin` returns HTML | ❌ | Recompile: `mvn clean compile` |
| Cannot login | ❌ | Check credentials, verify user exists |
| 403 Forbidden for admin | ❌ | Check `is_admin` flag in database |
| JSON not rendering in dashboard | ❌ | Check browser console for JavaScript errors (F12) |
| CORS errors | ❌ | Update @CrossOrigin origins |
| Session not persisting | ❌ | Ensure `credentials: 'include'` in fetch |
| Database connection fails | ❌ | Check application.properties DB config |
| Dashboard loads but no data | ❌ | Check server logs, verify admin endpoints working |

---

## ✅ Success Criteria

Your fix is successful when:

- ✅ Admin can login successfully
- ✅ `/api/admin` returns JSON data (not HTML)
- ✅ `/admin-dashboard.html` loads and displays stats
- ✅ Can manage users, posts, and reports from dashboard
- ✅ Non-admin users get 403 Forbidden
- ✅ Unauthenticated users get 401 Unauthorized
- ✅ All API endpoints return JSON
- ✅ No HTML error pages for API routes

---

## 📞 Need Help?

1. Check the documentation files:
   - `QUICKSTART.md` - Quick reference
   - `ADMIN_API_GUIDE.md` - Detailed technical guide
   - `SOLUTION_SUMMARY.md` - Complete explanation
   - `CODE_CHANGES.md` - Before/after code comparison

2. Review server logs:
   - Look for error messages
   - Check authentication flow
   - Verify database queries

3. Test individual endpoints:
   - Use Postman or curl
   - Check response format (JSON vs HTML)
   - Verify HTTP status codes

4. Check browser console (F12):
   - JavaScript errors
   - Network requests and responses
   - CORS issues

---

## 🎉 Completion

Once all tests pass, you can:
- Deploy to production
- Document the admin panel for your team
- Create user management procedures
- Monitor admin activities in logs
