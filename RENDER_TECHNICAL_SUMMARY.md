# 📦 Render Deployment - Technical Summary

## What We've Prepared

✅ **application.properties** - Updated to use environment variables
✅ **Dockerfile** - Optimized for Render (Java 17, builds & runs JAR)
✅ **render.yaml** - Configuration for Render deployment
✅ **Documentation** - Complete guides with troubleshooting

---

## Files Created/Updated

| File | Purpose |
|------|---------|
| `src/main/resources/application.properties` | Uses env variables for production |
| `Dockerfile` | Builds & runs Java app on Render |
| `render.yaml` | Render deployment configuration |
| `START_RENDER_DEPLOYMENT.md` | Quick start (5 min) |
| `RENDER_DEPLOYMENT_GUIDE.md` | Complete step-by-step guide |
| `RENDER_DEPLOYMENT_CHECKLIST.md` | Pre-deployment checklist |

---

## Environment Variables Used

```yaml
DATABASE_URL              # mysql://user:pass@host/techvision
DATABASE_USERNAME         # your-db-username
DATABASE_PASSWORD         # your-db-password
PORT                      # 8080 (automatically set by Render)
JPA_DDL_AUTO             # validate (production safe)
GEMINI_API_KEY           # Optional
MAIL_HOST                # smtp.gmail.com
MAIL_PORT                # 587
MAIL_USERNAME            # your-email@gmail.com
MAIL_PASSWORD            # gmail-app-password
GOOGLE_CLIENT_ID         # Optional
GOOGLE_CLIENT_SECRET     # Optional
GITHUB_CLIENT_ID         # Optional
GITHUB_CLIENT_SECRET     # Optional
```

---

## Deployment Flow

```
1. Push code to GitHub
        ↓
2. Render detects push
        ↓
3. Render builds Docker image
   - Copies files
   - Runs mvnw clean package
   - Creates JAR
        ↓
4. Render starts container
   - java -jar signup/target/*.jar
        ↓
5. Application starts on port 8080
        ↓
6. Render's reverse proxy routes traffic
        ↓
7. Your app is live! 🎉
```

---

## Database Setup Comparison

### PlanetScale (Recommended - Free)
```
✅ Free tier
✅ MySQL compatible
✅ No credit card needed
✅ Instant setup
✅ Public API included
⚠️  Limited to 10GB free tier
```

**Steps:**
1. planetscale.com → Sign up
2. Create DB "techvision"
3. Get connection string
4. Use in Render

### Aiven (Free)
```
✅ Free tier available
✅ MySQL support
✅ Auto backups
⚠️  Setup takes ~10 min
```

### AWS RDS (Production)
```
✅ Most reliable
✅ Managed backups
✅ Monitoring included
💰 $0.017/hr = ~$12/month
```

---

## Gmail App Password Setup

**Required for sending emails:**

1. Enable 2FA: https://myaccount.google.com/security
2. Generate app password: https://myaccount.google.com/apppasswords
3. Select "Mail" and "Other (custom name)"
4. Copy the 16-character password
5. Use that as `MAIL_PASSWORD` in Render

---

## Deployment Timeline

| Step | Time | What Happens |
|------|------|-------------|
| Code push | 1 min | You push to GitHub |
| Render webhook | Instant | GitHub notifies Render |
| Build starts | 1 min | Renders polls for changes |
| Docker build | 3-5 min | Builds Java app & JAR |
| Container starts | 1-2 min | Starts JVM with JAR |
| **Total** | **5-8 min** | **App is live!** |

---

## Monitoring After Deployment

### View Logs
```
Render Dashboard → Your Service → Logs tab
```

**Look for these success messages:**
```
Step 1/10 : FROM openjdk:17-jdk-slim
...
Step 10/10 : CMD ["java", "-jar", "signup/target/*.jar"]
...
Started SignupApplication in X.XXX seconds
Tomcat started on port(s): 8080 (http)
```

### Common Build Output

```
[INFO] Building jar: /app/signup/target/signup-0.0.1-SNAPSHOT.jar
[INFO] BUILD SUCCESS
```

---

## Testing Your Deployed App

### Get Your Service URL
```
Render Dashboard → Your Service → URL (at top)
Example: https://techvision-api.onrender.com
```

### Test Signup
```bash
curl -X POST https://YOUR-SERVICE.onrender.com/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Test User",
    "username": "testuser123",
    "email": "test@example.com",
    "password": "Test@1234",
    "githubUsername": "testuser",
    "primarySkill": "Java"
  }'
```

**Expected Response:**
```json
{
  "id": 1,
  "fullName": "Test User",
  "username": "testuser123",
  "email": "test@example.com",
  "isAdmin": false,
  "createdAt": "2026-05-17T..."
}
```

### Test Login
```bash
curl -X POST https://YOUR-SERVICE.onrender.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser123",
    "password": "Test@1234"
  }'
```

---

## Auto-Deployment Setup

**Already enabled!** Every time you push to `main`:

```bash
git add .
git commit -m "Your message"
git push origin main
```

✅ Render automatically:
1. Detects the push
2. Rebuilds Docker image
3. Restarts container
4. Updates live app

**No manual clicks needed!**

---

## Scaling (When You Get Traffic)

### Current Setup
```
Free tier
- 1 shared CPU
- 512 MB RAM
- ~100 concurrent users
```

### Upgrade Path
```
Starter: $12/month
- 1 dedicated CPU
- 1 GB RAM
- ~500 concurrent users

Standard: $29/month
- More resources
- Priority support
```

---

## Cost Breakdown (First Year)

```
Render (Free tier)        $0/month     $0/year
PlanetScale (Free)        $0/month     $0/year
Domain (Namecheap)        -            $0.88/year
Gmail (Free)              $0           $0
────────────────────────────────────────────
TOTAL                     $0/month     $0.88/year
```

---

## Security Best Practices

✅ **Done:**
- Environment variables for all secrets
- No credentials in code
- JPA set to `validate` (no schema changes in production)
- HTTPS by default on Render
- Session cookies: HttpOnly, SameSite=Lax

⚠️ **To Do:**
- Monitor logs for suspicious activity
- Set up database backups
- Add rate limiting for APIs
- Implement CORS properly for your frontend
- Use strong passwords for database

---

## Troubleshooting: If Something Goes Wrong

### App won't start
```
Check Logs → Look for "ERROR"
Common: DATABASE_URL wrong, missing env vars
Fix: Verify all 3 database vars in Render settings
```

### 502 Bad Gateway
```
App crashed
Check Logs → Look for "java.lang.Exception"
Restart: Dashboard → Manual Deploy
```

### Very slow startup
```
First deploy is slow (building Java app)
Free tier is slower
Wait 15-20 minutes
If still slow, check: Settings → Instance Type
```

### Can't connect to database
```
Check:
1. DATABASE_URL format correct (mysql://...)
2. Database exists
3. Username & password correct
4. Host is publicly accessible
Test locally: mysql -h host -u user -p
```

---

## Next Steps

1. **TODAY:**
   - [ ] Set up database (PlanetScale)
   - [ ] Create Render account
   - [ ] Verify Dockerfile locally
   - [ ] Deploy to Render

2. **TOMORROW:**
   - [ ] Test all endpoints
   - [ ] Check logs for errors
   - [ ] Monitor performance

3. **NEXT WEEK:**
   - [ ] Buy domain (optional)
   - [ ] Connect custom domain
   - [ ] Set up monitoring/alerts
   - [ ] Create production checklist

---

## Help & Support

| Issue | Resource |
|-------|----------|
| Render deployment | render.com/docs |
| Spring Boot Docker | spring.io/guides/gs/spring-boot-docker/ |
| PlanetScale setup | planetscale.com/docs |
| Java on Docker | openjdk.java.net/ |

---

## Quick Commands Reference

```bash
# Test locally
cd signup && mvn clean package -DskipTests

# Push to GitHub
git push origin main

# View Render logs (via dashboard)
# Render → Service → Logs tab

# Redeploy manually
# Render → Service → Manual Deploy button

# Test deployed app
curl https://YOUR-SERVICE.onrender.com/api/auth/login
```

---

**Ready to deploy? Start with:** START_RENDER_DEPLOYMENT.md ✨
