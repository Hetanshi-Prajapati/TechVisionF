# 🚀 RENDER DEPLOYMENT - START HERE

## 5-Minute Quick Start

### Step 1: Prepare Database (5 min)
```
1. Go to planetscale.com → Sign up free
2. Create database "techvision"
3. Get connection string: mysql://user:pass@host/techvision
4. Save it - you'll need it soon
```

### Step 2: Sign Up Render (2 min)
```
1. Go to render.com → Sign up with GitHub
2. Authorize GitHub access
3. Done!
```

### Step 3: Push Code to GitHub (3 min)
```bash
cd signup
git add .
git commit -m "Ready for deployment"
git push origin main
```

### Step 4: Deploy on Render (10 min deploy)
```
1. Click Create → Web Service
2. Select your GitHub repo
3. Settings:
   - Name: techvision-api
   - Root: signup
   - Instance: Free
4. Click Advanced → Add these environment variables:
   - DATABASE_URL: (paste from step 1)
   - DATABASE_USERNAME: (from step 1)
   - DATABASE_PASSWORD: (from step 1)
   - PORT: 8080
   - JPA_DDL_AUTO: validate
5. Click "Create Web Service"
6. Wait 10 minutes, check logs
```

### Step 5: Test (2 min)
```bash
# Copy your Render URL from dashboard
curl -X POST https://YOUR-SERVICE.onrender.com/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "fullName":"Test",
    "username":"test",
    "email":"test@test.com",
    "password":"Test1234!",
    "githubUsername":"test",
    "primarySkill":"Java"
  }'
```

**🎉 Done! Your app is live!**

---

## Full Details

See: [RENDER_DEPLOYMENT_GUIDE.md](RENDER_DEPLOYMENT_GUIDE.md)

See: [RENDER_DEPLOYMENT_CHECKLIST.md](RENDER_DEPLOYMENT_CHECKLIST.md)

---

## Database Options

| Service | Cost | Setup Time | Recommended? |
|---------|------|-----------|-------------|
| **PlanetScale** | Free | 2 min | ✅ YES |
| Aiven | Free | 5 min | OK |
| AWS RDS | $12+/mo | 15 min | For production |

---

## Need Help?

- 📖 Full guide: [RENDER_DEPLOYMENT_GUIDE.md](RENDER_DEPLOYMENT_GUIDE.md)
- ✅ Checklist: [RENDER_DEPLOYMENT_CHECKLIST.md](RENDER_DEPLOYMENT_CHECKLIST.md)
- 🎓 Render docs: https://render.com/docs
- 🐛 Spring Boot guide: https://spring.io/guides/gs/spring-boot-docker/

---

**Next: Follow the RENDER_DEPLOYMENT_GUIDE.md step-by-step!** ⬇️
