# 📋 Supabase + Render Deployment Checklist

## Pre-Deployment

### Code Ready
- [ ] Application.properties updated for PostgreSQL (DONE ✓)
- [ ] pom.xml has PostgreSQL driver (DONE ✓)
- [ ] No hardcoded credentials (DONE ✓)
- [ ] Code pushed to GitHub

### Account Setup
- [ ] Supabase account created (free)
- [ ] Render account created (free)
- [ ] GitHub connected to Render

---

## Supabase Setup

### Database Creation
- [ ] New project created in Supabase
- [ ] Project name: techvision
- [ ] Password generated (save it!)
- [ ] Region selected
- [ ] Project creation complete (wait 2 min)

### Connection String
- [ ] Logged into Supabase dashboard
- [ ] Went to Settings → Database
- [ ] Copied connection string (postgresql://...)
- [ ] Verified format: postgresql://user:pass@host:port/database
- [ ] Saved DATABASE_URL
- [ ] Saved DATABASE_USERNAME (postgres)
- [ ] Saved DATABASE_PASSWORD

---

## Render Deployment

### Service Creation
- [ ] GitHub repo connected to Render
- [ ] Web Service created
- [ ] Root directory set to: signup
- [ ] Instance type: Free

### Environment Variables
- [ ] DATABASE_URL: (from Supabase)
- [ ] DATABASE_USERNAME: postgres
- [ ] DATABASE_PASSWORD: (from Supabase)
- [ ] PORT: 8080
- [ ] JPA_DDL_AUTO: validate
- [ ] GEMINI_API_KEY: (optional)
- [ ] MAIL_HOST: (optional)
- [ ] MAIL_PORT: (optional)
- [ ] MAIL_USERNAME: (optional)
- [ ] MAIL_PASSWORD: (optional)
- [ ] GOOGLE_CLIENT_ID: (optional)
- [ ] GOOGLE_CLIENT_SECRET: (optional)
- [ ] GITHUB_CLIENT_ID: (optional)
- [ ] GITHUB_CLIENT_SECRET: (optional)

### Deployment
- [ ] Service created
- [ ] Build started
- [ ] Waiting for deployment (5-10 min)

---

## Testing

### Basic Tests
- [ ] Render service shows "live" status
- [ ] Check Logs tab for errors
- [ ] Get service URL from Render

### API Tests
- [ ] Test POST /api/auth/signup
- [ ] Test POST /api/auth/login
- [ ] Check response status (should be 200 or 201)

### Database Tests
- [ ] Data appears in Supabase dashboard
- [ ] Can query users table
- [ ] New signups are recorded

---

## Domain Setup (Optional)

### Purchase
- [ ] Domain purchased from Namecheap ($0.88)
- [ ] Payment completed
- [ ] Domain activated

### Connection
- [ ] Added domain to Render service
- [ ] Copied Render CNAME records
- [ ] Updated Namecheap DNS settings
- [ ] Nameservers updated
- [ ] Waiting for DNS propagation (24-48 hours)

### Verification
- [ ] Can access via custom domain
- [ ] SSL/HTTPS working
- [ ] Endpoints responding

---

## Post-Deployment

### Monitoring
- [ ] Check Render logs weekly
- [ ] Monitor database size in Supabase
- [ ] Test endpoints periodically
- [ ] Watch for errors in logs

### Maintenance
- [ ] Auto-deploy enabled (GitHub push → auto redeploy)
- [ ] Backups enabled in Supabase (automatic)
- [ ] No manual database backups needed (automatic)

### Documentation
- [ ] Added deployment notes
- [ ] Saved connection strings safely (not in code!)
- [ ] Documented environment variables

---

## Troubleshooting Checklist

### Build Issues
- [ ] Java version is 17+ (check pom.xml)
- [ ] PostgreSQL driver in pom.xml
- [ ] No syntax errors in code

### Database Issues
- [ ] CONNECTION_STRING has port 6543 (not 5432)
- [ ] Password is correct (check Supabase)
- [ ] Database name is "postgres"
- [ ] Host matches Supabase URL

### Render Issues
- [ ] Service in "live" state
- [ ] No error logs
- [ ] Env variables all set correctly
- [ ] All required vars present

### DNS Issues
- [ ] Waited 24-48 hours after DNS change
- [ ] CNAME record correctly added
- [ ] No typos in domain name

---

## Summary

✅ **Total Time:** 20-30 minutes

✅ **Result:** Your app is LIVE with:
- Free Supabase database
- Free Render hosting
- Optional custom domain ($0.88)
- Automatic deploys
- Monitoring & logs

✅ **Cost:** $0/month (or $0.88/year if domain)

✅ **Next Steps:**
1. Follow SUPABASE_RENDER_DEPLOYMENT.md
2. Test all endpoints
3. Monitor logs
4. Add domain later (optional)

---

**Ready to go! Start with the deployment guide! 🚀**
