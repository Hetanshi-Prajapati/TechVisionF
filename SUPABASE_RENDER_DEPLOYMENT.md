# 🚀 Supabase + Render Deployment Guide

## 🎯 Why Supabase?

✅ **100% Free** (unlike PlanetScale paid tier)
✅ **PostgreSQL** (industry standard, better than MySQL)
✅ **Generous Free Tier** (unlimited tables, 500MB storage free)
✅ **Real-time features** (optional, not needed for your app)
✅ **Auth built-in** (we're not using, but available)
✅ **No credit card needed** (truly free)

---

## 📊 PlanetScale vs Supabase Comparison

| Feature | PlanetScale | Supabase |
|---------|------------|----------|
| **Free Tier** | Yes | ✅ YES |
| **Database Type** | MySQL | PostgreSQL |
| **Storage** | Limited | ✅ 500MB free |
| **Credit Card** | Required | ❌ Not needed |
| **Cost/Month** | $0-50+ | ✅ $0 |
| **Setup Time** | 5 min | ✅ 3 min |

**Winner: Supabase** 🏆

---

## 🎯 Complete Deployment Plan (30 minutes total)

### **Part 1: Set Up Supabase (5 minutes)**
1. Create free account
2. Create new project
3. Get connection string
4. Create database schema

### **Part 2: Deploy to Render (10 minutes)**
1. Add environment variables
2. Deploy web service
3. Wait for build

### **Part 3: Test & Verify (5 minutes)**
1. Test signup endpoint
2. Test login endpoint
3. Check database

### **Part 4: Buy Domain (Optional, 10 minutes)**
1. Buy domain
2. Connect to Render
3. Wait 24-48 hours

---

## 🔧 Step 1: Create Supabase Project

### 1.1 Sign Up

```
1. Go to supabase.com
2. Click "Sign Up"
3. Use GitHub to sign up (easiest)
4. Authorize Supabase access
```

### 1.2 Create New Project

```
1. In dashboard, click "New project"
2. Fill details:
   - Project name: techvision
   - Database password: (generate strong one)
   - Region: Choose closest to you
   - Plan: Free tier
3. Click "Create new project"
4. Wait 2-3 minutes for creation
```

### 1.3 Get Connection String

**Option A: PostgreSQL Connection String (What we need)**

```
1. In Supabase dashboard, click your project
2. Go to Settings → Database
3. Copy the "Connection string" (NOT "URI")
4. Find the line that starts with: postgresql://
5. Format:
   postgresql://[user]:[password]@[host]:[port]/[database]
```

**Example:**
```
postgresql://postgres.abcdefgh:password123@aws-0-us-east-1.pooler.supabase.com:6543/postgres
```

### 1.4 Save Your Credentials

Create a temporary note with:
```
DATABASE_URL: postgresql://...
DATABASE_USERNAME: postgres
DATABASE_PASSWORD: (your password)
```

---

## 🚀 Step 2: Deploy to Render

### 2.1 Log in to Render

```
1. Go to render.com
2. Sign in with GitHub
```

### 2.2 Create Web Service

```
1. Click "Create" → "Web Service"
2. Select your GitHub repository
3. Click "Connect"
```

### 2.3 Configure Settings

```
Service Name:           techvision-api
Branch:                 main
Build Command:          (auto-detected)
Start Command:          (auto-detected)
Root Directory:         signup
Instance Type:          Free
Plan:                   Free
```

### 2.4 Add Environment Variables

**Click "Advanced" and add these:**

```
DATABASE_URL
- Value: postgresql://postgres.abcdefgh:password123@aws-0-us-east-1.pooler.supabase.com:6543/postgres

DATABASE_USERNAME
- Value: postgres

DATABASE_PASSWORD
- Value: (your Supabase password)

PORT
- Value: 8080

JPA_DDL_AUTO
- Value: validate

GEMINI_API_KEY
- Value: (leave empty or add your key)

MAIL_HOST
- Value: smtp.gmail.com

MAIL_PORT
- Value: 587

MAIL_USERNAME
- Value: (your gmail)

MAIL_PASSWORD
- Value: (your app password)

GOOGLE_CLIENT_ID
- Value: (optional)

GOOGLE_CLIENT_SECRET
- Value: (optional)

GITHUB_CLIENT_ID
- Value: (optional)

GITHUB_CLIENT_SECRET
- Value: (optional)
```

### 2.5 Deploy

```
1. Click "Create Web Service"
2. Wait 5-10 minutes for build & deploy
3. Check Logs tab for success messages
```

---

## ✅ Step 3: Test Your Deployment

### 3.1 Get Your Service URL

```
Render Dashboard → Your Service → Copy URL
Example: https://techvision-api.onrender.com
```

### 3.2 Test Signup

```bash
curl -X POST https://techvision-api.onrender.com/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Test User",
    "username": "testuser",
    "email": "test@example.com",
    "password": "Test1234!",
    "githubUsername": "testuser",
    "primarySkill": "Java"
  }'
```

**Expected Response:**
```json
{
  "id": 1,
  "fullName": "Test User",
  "username": "testuser",
  "email": "test@example.com",
  ...
}
```

### 3.3 Verify Database

**Option A: Via Supabase Dashboard**
```
1. Go to Supabase dashboard
2. Click your project
3. Go to "SQL Editor"
4. Click "New query"
5. Run: SELECT * FROM users;
6. Should show your test user
```

**Option B: Via SQL Client**
```bash
# Using psql
psql postgresql://postgres.abcd:password@host:6543/postgres

# Then:
SELECT * FROM users;
```

---

## 🌐 Step 4: Add Custom Domain (Optional)

### 4.1 Buy Domain

**On Namecheap ($0.88 first year):**
```
1. Go to namecheap.com
2. Search for your domain
3. Add to cart
4. Buy for $0.88 (first year)
5. Complete checkout
```

### 4.2 Connect to Render

```
1. In Render, go to your service
2. Click "Settings" → "Custom Domains"
3. Click "Add Domain"
4. Enter your domain (yourdomain.com)
5. Click "Add"
6. Copy the CNAME records shown
```

### 4.3 Update DNS at Namecheap

```
1. Go to Namecheap account
2. Find your domain
3. Click "Manage"
4. Go to "Advanced DNS"
5. Click "Add Record"
6. Type: CNAME
7. Host: @ (or www)
8. Value: (copy from Render)
9. Save
10. Wait 24-48 hours for DNS to propagate
```

### 4.4 Verify Domain

```bash
# Test after 48 hours:
curl https://yourdomain.com/api/auth/login
# Should work!
```

---

## 📋 Environment Variables Checklist

When setting up Render, use **EXACTLY** these keys:

```
☐ DATABASE_URL
☐ DATABASE_USERNAME
☐ DATABASE_PASSWORD
☐ PORT (set to 8080)
☐ JPA_DDL_AUTO (set to validate)
☐ GEMINI_API_KEY (optional)
☐ MAIL_HOST (optional)
☐ MAIL_PORT (optional)
☐ MAIL_USERNAME (optional)
☐ MAIL_PASSWORD (optional)
☐ GOOGLE_CLIENT_ID (optional)
☐ GOOGLE_CLIENT_SECRET (optional)
☐ GITHUB_CLIENT_ID (optional)
☐ GITHUB_CLIENT_SECRET (optional)
```

---

## 🚨 Database Connection Issues? Fix These

### Issue: `org.postgresql.util.PSQLException`

**Solution:**
```
1. Copy DATABASE_URL exactly from Supabase
2. Make sure no spaces or typos
3. Check port is 6543 (Supabase uses 6543)
4. Verify database name (usually "postgres")
```

### Issue: Connection timeout

**Solution:**
```
1. Check internet connection
2. Verify Supabase project is active
3. Check if Render is in same region (helps)
4. Try different browser to get connection string
```

### Issue: Password wrong

**Solution:**
```
1. Go to Supabase dashboard
2. Settings → Database
3. Reveal password or reset it
4. Update Render env variables
```

---

## 🔄 Auto-Deploy on Every Push

**Already enabled!**

```bash
# Push code, Render auto-deploys (5-8 min)
git add .
git commit -m "Update message"
git push origin main
```

---

## 📊 Supabase Dashboard Features

**Once deployed, you can:**

### View Data
```
Supabase Dashboard → Your Project → "Table Editor"
→ Click "users" table → See all signup data
```

### Run SQL Queries
```
Supabase Dashboard → "SQL Editor"
→ Write custom queries
→ Example: SELECT * FROM users WHERE is_admin = true;
```

### Backup Data
```
Supabase Dashboard → Settings → Backups
→ Automatic daily backups (free tier)
```

### Monitor Performance
```
Supabase Dashboard → Stats
→ See database size, connection count, etc.
```

---

## 💰 Cost Breakdown

| Service | Free Tier | Cost |
|---------|-----------|------|
| **Supabase** | PostgreSQL 500MB | $0/month |
| **Render** | Web service | $0/month |
| **Domain** | (optional) | $0.88/year |
| **Email** | Gmail | $0 |
| **TOTAL** | - | **$0/month** |

---

## 🎯 What's Different from MySQL?

Your Spring Boot app **automatically** uses PostgreSQL now because:

```
✅ application.properties uses PostgreSQL connection string
✅ pom.xml has PostgreSQL driver (postgresql jar)
✅ Hibernate dialect set to PostgreSQL
✅ No other code changes needed!
```

**Everything is compatible!** Your JPA code works the same.

---

## 🔐 Security Best Practices

✅ **Supabase secures:**
- Automatic SSL/TLS encryption
- Row-level security (optional)
- Auth built-in (optional)
- Backups automatic

✅ **You should do:**
- Use strong password (Supabase generates one)
- Don't share connection strings
- Use environment variables (already done ✓)
- Monitor for unusual activity

---

## 📈 Scaling Up (When You Get Traffic)

### Current Free Setup
```
- 1 PostgreSQL database
- 500MB storage
- Shared resources
- ~100 concurrent users
```

### Upgrade Steps
```
1. Supabase: Click "Upgrade" → $25/month
2. Render: Click "Upgrade" → $12/month
3. Upgrade to paid tier, no code changes needed!
```

---

## 🎉 Deployment Timeline

```
TODAY:
├─ 5 min: Create Supabase project
├─ 10 min: Deploy to Render
├─ 5 min: Test endpoints
└─ ✅ LIVE!

LATER (Optional):
├─ 10 min: Buy domain
├─ 5 min: Connect domain
└─ 48 hours: DNS propagates

TOTAL: 20-25 minutes (live!)
```

---

## 🆘 Troubleshooting

| Problem | Solution |
|---------|----------|
| **Can't find connection string** | Supabase → Project → Settings → Database |
| **Build fails** | Check Java 17 in pom.xml |
| **Database connection error** | Copy connection string exactly, check port 6543 |
| **502 Bad Gateway** | Check Render logs for errors |
| **Very slow first request** | Java startup is slow, wait 15 sec |

---

## ✨ Next Steps

1. ✅ Code updated for PostgreSQL
2. ✅ pom.xml has PostgreSQL driver
3. 📝 Follow this guide step-by-step
4. 🚀 Deploy to Render
5. ✅ Test endpoints
6. 🌐 (Optional) Add domain

---

## 📞 Help & Resources

- **Supabase Docs**: https://supabase.com/docs
- **Render Docs**: https://render.com/docs
- **PostgreSQL Docs**: https://www.postgresql.org/docs/
- **Spring Boot + PostgreSQL**: https://spring.io/guides/

---

**Ready to deploy? Follow the steps above! You'll be live in 20 minutes!** 🚀
