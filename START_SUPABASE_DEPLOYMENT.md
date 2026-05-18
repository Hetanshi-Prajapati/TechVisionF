# 🚀 SUPABASE + RENDER - Quick Start (5 Minutes)

## Step 1: Create Supabase Database (3 minutes)

```
1. Go to supabase.com → Sign up with GitHub
2. Create new project:
   - Name: techvision
   - Password: (generate strong password)
   - Region: (choose nearest)
   - Plan: Free
3. Wait 2 minutes for creation
4. Go to Settings → Database
5. Copy the connection string:
   postgresql://postgres.abc123:password@host:6543/postgres
```

**Save these 3 things:**
```
DATABASE_URL = (the connection string above)
DATABASE_USERNAME = postgres
DATABASE_PASSWORD = (your password)
```

---

## Step 2: Deploy on Render (10 minutes)

```
1. Go to render.com → Sign in with GitHub
2. Click "Create" → "Web Service"
3. Select your GitHub repo → "Connect"
4. Settings:
   - Name: techvision-api
   - Root: signup
   - Instance: Free
5. Click "Advanced"
6. Add environment variables:
   
   DATABASE_URL: postgresql://...
   DATABASE_USERNAME: postgres
   DATABASE_PASSWORD: (from step 1)
   PORT: 8080
   JPA_DDL_AUTO: validate

7. Click "Create Web Service"
8. Wait 5-10 minutes
9. Check Logs → Look for "Successfully deployed"
```

---

## Step 3: Test (2 minutes)

```bash
# Copy your service URL from Render dashboard
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

**Should return:**
```json
{
  "id": 1,
  "fullName": "Test",
  "username": "test",
  ...
}
```

---

## 🎉 Done! Your App is LIVE!

**URL:** `https://YOUR-SERVICE.onrender.com`

---

## Optional: Add Domain (10 minutes)

```
1. Go to namecheap.com
2. Buy domain for $0.88
3. In Render: Settings → Custom Domains → Add Domain
4. Add Namecheap DNS records
5. Wait 24-48 hours
6. Access at: https://yourdomain.com
```

---

## For Detailed Steps

See: [SUPABASE_RENDER_DEPLOYMENT.md](SUPABASE_RENDER_DEPLOYMENT.md)

---

**Total time to live: 20 minutes! 🚀**
