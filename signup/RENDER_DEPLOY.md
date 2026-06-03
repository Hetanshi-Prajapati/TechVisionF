Render deployment guide

1) Push repository to GitHub

```bash
git add .
git commit -m "dockerized springboot project + render blueprint"
git push origin main
```

2) Create a Render account and connect your GitHub repository.

3) Create a new service -> "Web Service" -> Connect repo -> Select `signup` folder (root) -> Choose "Docker" (render.yaml present).

4) Create a managed PostgreSQL database in Render (Dashboard -> Databases -> New Database).
   - Note the connection URL. Render provides a `DATABASE_URL` similar to:
     `postgres://<user>:<password>@<host>:<port>/<db>`

5) Set environment variables on the Render service (Dashboard -> Environment):
   - `DATABASE_URL` = connection string from the managed DB
   - `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_HOST`, `MAIL_PORT` (if using email)
   - `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, `GITHUB_CLIENT_ID`, `GITHUB_CLIENT_SECRET` (if using OAuth)
   - Any other secrets from `application-secret.properties` (you can add them directly on Render)

6) Deploy the service. Render will build using the Dockerfile and run the `startCommand`.

7) Verify the public URL provided by Render. The app should be live publicly.

Notes:
- The repository already contains `Dockerfile`, `.dockerignore`, and `render.yaml` to make Render deployments straightforward.
- If you want me to push and trigger the initial Render deploy, grant GitHub access or provide deploy permissions; I can attempt the push now.