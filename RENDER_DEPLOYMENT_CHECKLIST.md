# 📋 Render Deployment Checklist

## Pre-Deployment Checklist

### GitHub Setup
- [ ] Code pushed to GitHub repository
- [ ] `.gitignore` includes: `target/`, `.env`, `*.class`, `uploads/`
- [ ] README.md exists in signup folder
- [ ] Java version is 17+ in pom.xml

### Code Cleanup
- [ ] Remove all hardcoded credentials (DONE ✓ - using env variables now)
- [ ] No sensitive API keys in application.properties
- [ ] Database config uses environment variables
- [ ] CORS settings allow frontend domain (if needed)

### Database Setup
- [ ] PlanetScale account created OR Aiven OR AWS RDS
- [ ] MySQL database named `techvision` created
- [ ] Connection string saved (format: `mysql://user:pass@host/techvision`)
- [ ] Can connect from your machine (test locally)

### Render.com Setup
- [ ] Free Render account created
- [ ] GitHub connected to Render
- [ ] Service created with correct settings
- [ ] All environment variables added:
  - [ ] DATABASE_URL
  - [ ] DATABASE_USERNAME
  - [ ] DATABASE_PASSWORD
  - [ ] PORT=8080
  - [ ] JPA_DDL_AUTO=validate
  - [ ] GEMINI_API_KEY (optional)
  - [ ] MAIL_USERNAME & MAIL_PASSWORD (optional)

### Testing
- [ ] Test signup endpoint: `POST /api/auth/signup`
- [ ] Test login endpoint: `POST /api/auth/login`
- [ ] Check logs for errors
- [ ] Verify database connection in logs

### Domain (Optional)
- [ ] Domain purchased from Namecheap/GoDaddy
- [ ] Custom domain added to Render service
- [ ] Nameservers updated
- [ ] Wait 24-48 hours for DNS propagation

---

## Quick Start Commands

### 1️⃣ Test Locally First
```bash
cd signup
./mvnw clean package -DskipTests
java -jar target/*.jar
```

### 2️⃣ Push to GitHub
```bash
git add .
git commit -m "Ready for Render deployment"
git push origin main
```

### 3️⃣ Deploy on Render (Manual Steps)
1. Go to render.com → Create → Web Service
2. Connect GitHub repo
3. Configure as per RENDER_DEPLOYMENT_GUIDE.md
4. Add environment variables
5. Click "Create Web Service"

### 4️⃣ Monitor
```
Check Render Dashboard → Logs tab every 5 minutes during deploy
```

---

## Environment Variables (Copy-Paste)

When adding to Render, use these keys:

```
DATABASE_URL
DATABASE_USERNAME
DATABASE_PASSWORD
PORT
JPA_DDL_AUTO
GEMINI_API_KEY
MAIL_HOST
MAIL_PORT
MAIL_USERNAME
MAIL_PASSWORD
GOOGLE_CLIENT_ID
GOOGLE_CLIENT_SECRET
GITHUB_CLIENT_ID
GITHUB_CLIENT_SECRET
```

---

## Troubleshooting Quick Reference

| Problem | Solution |
|---------|----------|
| Build fails | Check `signup/pom.xml` for Java 17 |
| Deploy hangs | Check Render logs, may timeout on first build |
| 502 Bad Gateway | Service crashed, check logs |
| Database error | Verify DATABASE_URL format & connectivity |
| Slow startup | Free tier is slow, may need upgrade |

---

## Cost Estimate

| Item | Cost |
|------|------|
| Render (free tier) | $0/month |
| PlanetScale MySQL (free) | $0/month |
| Domain (Namecheap, first year) | $0.88 |
| **TOTAL (First Year)** | **$0.88** |
| **TOTAL (Monthly)** | **$0** |

---

## After Deployment

1. Monitor logs daily: Render Dashboard → Logs
2. Test API weekly: `POST /api/auth/signup`
3. Check database size (free tier has limits)
4. Set up GitHub branch protection (optional)
5. Enable "Auto-Deploy" (should be default)

---

✅ **Ready? Follow the RENDER_DEPLOYMENT_GUIDE.md for detailed steps!**
