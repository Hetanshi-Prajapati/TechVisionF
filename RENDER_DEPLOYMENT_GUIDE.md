# 🚀 Render.com Deployment Guide - Complete Step-by-Step

## Prerequisites

- ✅ GitHub account (with your project pushed)
- ✅ Render.com account (free to create)
- ✅ MySQL database (use Aiven, PlanetScale, or AWS RDS)
- ✅ Domain name (optional for now, can add later)

---

## **Step 1: Prepare Your GitHub Repository**

### 1.1 Push Code to GitHub

```bash
# Initialize git (if not already done)
git init

# Add all files
git add .

# Commit
git commit -m "Ready for Render deployment"

# Push to GitHub (replace YOUR_REPO_URL)
git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPO.git
git branch -M main
git push -u origin main
```

### 1.2 Create `.gitignore` (if missing)

```bash
# In the project root, create .gitignore with:
*.class
target/
.classpath
.project
.settings/
uploads/
.env
application-secret.properties
```

---

## **Step 2: Set Up MySQL Database**

**Choose ONE option below:**

### **Option A: Free - PlanetScale (MySQL Compatible)**

1. Go to [planetscale.com](https://planetscale.com)
2. Sign up for free account
3. Create a new database named `techvision`
4. Get connection string:
   ```
   mysql://[username]:[password]@[host]/techvision?sslaccept=strict
   ```
5. Save this URL - you'll need it in Step 4

### **Option B: Free - Aiven (MySQL)**

1. Go to [aiven.io](https://aiven.io)
2. Create free account
3. Create MySQL service (free tier)
4. Get connection string from dashboard
5. Save the URL

### **Option C: Paid - AWS RDS (Most Reliable)**

1. Go to AWS Console
2. Create RDS MySQL instance (db.t3.micro = $0.017/hour ≈ $12/month)
3. Set publicly accessible
4. Get connection string
5. Save the URL

**Recommended for you: PlanetScale** (100% free, reliable)

---

## **Step 3: Set Up Render.com Account**

1. Go to [render.com](https://render.com)
2. Click **Sign Up**
3. Use GitHub to sign up (easier for auto-deployment)
4. Authorize GitHub access
5. Skip the dashboard tour

---

## **Step 4: Create Web Service on Render**

### 4.1 Create New Service

1. Click **Create** → **Web Service**
2. Select your GitHub repository
3. Configure settings:

```
Name:                  techvision-api
Environment:           Docker
Build & Deploy Trigger: Auto
```

### 4.2 Finalize Settings

```
Docker Command: (leave empty - uses Dockerfile)
Root Directory: signup
Instance Type: Free
```

### 4.3 Add Environment Variables

Click **Advanced** and add these variables:

```
DATABASE_URL = mysql://username:password@host/techvision?sslaccept=strict
DATABASE_USERNAME = (your-db-username)
DATABASE_PASSWORD = (your-db-password)
PORT = 8080
JPA_DDL_AUTO = validate
GEMINI_API_KEY = (leave empty or add your key)
MAIL_HOST = smtp.gmail.com
MAIL_PORT = 587
MAIL_USERNAME = (your-gmail@gmail.com)
MAIL_PASSWORD = (your-app-password)
GOOGLE_CLIENT_ID = (optional)
GOOGLE_CLIENT_SECRET = (optional)
GITHUB_CLIENT_ID = (optional)
GITHUB_CLIENT_SECRET = (optional)
```

⚠️ **Important**: For Gmail:
- Use **App Passwords** (not your regular password)
- Enable 2FA on your Gmail account
- Generate app password: https://myaccount.google.com/apppasswords

### 4.4 Create Service

Click **Create Web Service**

**Wait 5-10 minutes for deployment** (first deploy is slow)

---

## **Step 5: Monitor Deployment**

### 5.1 Check Build Logs

1. In Render dashboard, click your service
2. Go to **Logs** tab
3. Look for:
   ```
   ✅ Successfully deployed
   ✅ Server started on port 8080
   ```

### 5.2 Common Issues & Fixes

| Issue | Solution |
|-------|----------|
| **Build fails** | Check Java version in pom.xml (should be 17) |
| **Database error** | Verify DATABASE_URL and network access |
| **Port binding error** | Ensure PORT env var is set to 8080 |
| **Memory issues** | Upgrade to paid tier or optimize code |

---

## **Step 6: Test Your Deployment**

### 6.1 Get Your Render URL

In Render dashboard:
```
https://techvision-api.onrender.com (example)
```

### 6.2 Test Endpoints

**Test Signup API:**
```bash
curl -X POST https://techvision-api.onrender.com/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Test User",
    "username": "testuser",
    "email": "test@example.com",
    "password": "Test1234!",
    "githubUsername": "test-git",
    "primarySkill": "Java"
  }'
```

**Test Login:**
```bash
curl -X POST https://techvision-api.onrender.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Test1234!"
  }'
```

---

## **Step 7: Add Custom Domain (Optional)**

### 7.1 Buy Domain

1. Go to [Namecheap.com](https://namecheap.com)
2. Search for your domain
3. Buy for $0.88 (first year)
4. Complete checkout

### 7.2 Connect to Render

1. In Render service dashboard
2. Go to **Settings** → **Custom Domains**
3. Click **Add Domain**
4. Enter your domain (e.g., `mytechvision.com`)
5. Render shows DNS records to add

### 7.3 Update Nameservers at Namecheap

1. Go to Namecheap dashboard
2. Click **Manage** next to your domain
3. Go to **Nameservers**
4. Change to Render's nameservers:
   ```
   ns1.render.com
   ns2.render.com
   ns3.render.com
   ns4.render.com
   ```
5. Save changes
6. **Wait 24-48 hours for DNS propagation**

---

## **Step 8: Enable Auto-Deploys (GitHub Integration)**

Already enabled! Every time you push to `main` branch:

```bash
git add .
git commit -m "Update feature"
git push origin main
```

✅ **Automatic redeploy starts** (check Render logs)

---

## **Step 9: Set Up Monitoring & Alerts**

### 9.1 Enable Alerts (Paid Feature)

Go to **Settings** → **Alerts**
- Alert on deployment failures
- Alert on high memory usage

### 9.2 Check Logs Regularly

```
Render Dashboard → Logs → Check for errors
```

---

## **Troubleshooting**

### Build Fails

```bash
# Ensure Java 17 in pom.xml:
<java.version>17</java.version>

# Check if target/classes is git ignored
# (it should be)
```

### Database Connection Error

```bash
# Test connection URL locally first:
mysql -h your-host -u username -p techvision

# If fails, check:
1. Username/password correct
2. Database created
3. Host is publicly accessible
4. Firewall allows port 3306
```

### Out of Memory

```bash
# Use lighter database or:
# Upgrade to $12/month Starter tier
```

---

## **Cost Estimate**

| Service | Free Tier | Paid Tier |
|---------|-----------|-----------|
| **Render** | $0/month | $12+/month |
| **Database** | $0-15/month | $12-50+/month |
| **Domain** | - | $12/year |
| **Email** | $0 (Gmail) | - |
| **Total** | **$0-15/month** | **$24-62+/month** |

---

## **Next Steps**

1. ✅ Push code to GitHub
2. ✅ Create database (PlanetScale recommended)
3. ✅ Deploy to Render
4. ✅ Test endpoints
5. ✅ (Optional) Add custom domain
6. ✅ Monitor logs
7. ✅ Set up CI/CD alerts

---

## **Quick Reference Commands**

```bash
# View logs
git log --oneline

# See deployment history
# Go to Render Dashboard → Deployments tab

# Redeploy manually
# Render Dashboard → Service → Manual Deploy

# View environment variables
# Render Dashboard → Environment
```

---

## **Support Links**

- 📖 [Render Docs](https://render.com/docs)
- 🆘 [Render Support](https://support.render.com)
- 🐛 [Spring Boot on Docker](https://spring.io/guides/gs/spring-boot-docker/)
- 🗄️ [PlanetScale Docs](https://planetscale.com/docs)

---

**Good luck! Your app will be live soon! 🎉**
