# How to Run the Signup Project

## Prerequisites

1. **Java 17** - Make sure Java 17 is installed
   ```bash
   java -version
   ```
   Should show version 17 or higher

2. **MySQL Database** - MySQL must be running on your system
   - Default: `localhost:3306`
   - Username: `root`
   - Password: `sonu2607` (as configured in application.properties)

3. **Maven** - Should be included with the project (mvnw wrapper)

## Step 1: Setup MySQL Database

1. Start MySQL service (if not running)
2. Open MySQL command line or MySQL Workbench
3. Create the database:
   ```sql
   CREATE DATABASE signup_db;
   ```
4. Verify the database exists:
   ```sql
   SHOW DATABASES;
   ```

## Step 2: Run the Project

### Option A: Using Maven Wrapper (Recommended)

**On Windows (PowerShell/CMD):**
```bash
.\mvnw.cmd spring-boot:run
```

**On Linux/Mac:**
```bash
./mvnw spring-boot:run
```

### Option B: Using Maven (if installed globally)

```bash
mvn spring-boot:run
```

### Option C: Build and Run JAR

1. Build the project:
   ```bash
   mvn clean package
   ```

2. Run the JAR:
   ```bash
   java -jar target/signup-0.0.1-SNAPSHOT.jar
   ```

## Step 3: Check if Application Started Successfully

### Look for these messages in the console:

✅ **Success indicators:**
```
Started SignupApplication in X.XXX seconds
Tomcat started on port(s): 8080 (http)
```

❌ **Error indicators:**
- `Connection refused` - MySQL is not running
- `Access denied` - Wrong MySQL username/password
- `Unknown database 'signup_db'` - Database doesn't exist

## Step 4: Verify the Application is Running

### 1. Open in Browser

**Signup Page:**
```
http://localhost:8080/api/auth/signup
```

**Login Page:**
```
http://localhost:8080/api/auth/login
```

### 2. Test API Endpoints

**Test Signup API (using curl or Postman):**
```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d "{\"fullName\":\"Test User\",\"username\":\"testuser\",\"email\":\"test@example.com\",\"password\":\"1234\",\"githubUsername\":\"test-git\",\"primarySkill\":\"Java\"}"
```

**Test Login API:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"emailOrUsername\":\"test@example.com\",\"password\":\"1234\"}"
```

### 3. Check Database

After signing up, verify data in MySQL:
```sql
USE signup_db;
SELECT * FROM users;
```

## Step 5: Monitor Console Output

The application will show:
- SQL queries (because `spring.jpa.show-sql=true`)
- Server startup logs
- Any errors or exceptions

**Example console output:**
```
Hibernate: create table users (id bigint not null auto_increment, email varchar(255), full_name varchar(255), github_username varchar(255), password varchar(255), primary_skill varchar(255), username varchar(255), primary key (id)) engine=InnoDB
...
Started SignupApplication in 3.456 seconds
```

## Troubleshooting

### Issue: Port 8080 already in use
**Solution:** Free port 8080 by stopping the process using it:
```
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

### Issue: Cannot connect to MySQL
**Solution:** 
1. Check MySQL service is running
2. Verify credentials in `application.properties`
3. Check MySQL is listening on port 3306

### Issue: Database doesn't exist
**Solution:** Create it manually:
```sql
CREATE DATABASE signup_db;
```

### Issue: Table creation errors
**Solution:** The `spring.jpa.hibernate.ddl-auto=update` should auto-create tables, but if issues persist, try:
```sql
DROP DATABASE signup_db;
CREATE DATABASE signup_db;
```
Then restart the application.

## Expected Behavior

1. **Application starts** on `http://localhost:8080`
2. **Database tables are created** automatically (users table)
3. **Signup page** is accessible at `/api/auth/signup`
4. **Login page** is accessible at `/api/auth/login`
5. **API endpoints** respond with JSON
6. **Data is saved** to MySQL database

## Quick Test Checklist

- [ ] MySQL is running
- [ ] Database `signup_db` exists
- [ ] Application starts without errors
- [ ] Can access signup page in browser
- [ ] Can access login page in browser
- [ ] Can create a new user via signup
- [ ] Can login with created user
- [ ] Data appears in MySQL `users` table

