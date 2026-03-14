# English Tense Coach

Full-stack web app for guided tense learning with auth, scoring, streaks, and AI chat.

## Features
- Spring Boot backend + React frontend
- Register/login with BCrypt password hashing
- JWT token auth for protected APIs
- Chapter-wise MID/PRO lessons
- Practice answer scoring and lesson progress tracking
- Daily streak and points system
- AI chat (type + voice input)
- Optional OpenAI provider with local fallback
- PostgreSQL-ready cloud profile

## Project Structure
- `/Users/kartik/Documents/Englishapp/backend`
- `/Users/kartik/Documents/Englishapp/frontend`
- `/Users/kartik/Documents/Englishapp/docker-compose.yml`

## Local Development

### Backend
```bash
cd /Users/kartik/Documents/Englishapp/backend
export JWT_SECRET='replace-with-your-long-32-plus-char-secret-key'
mvn spring-boot:run
```

### Frontend
```bash
cd /Users/kartik/Documents/Englishapp/frontend
npm install
VITE_API_BASE_URL=http://localhost:8080 npm run dev
```

## One-Command Docker Run (Production-like)
1. Create env file:
```bash
cd /Users/kartik/Documents/Englishapp
cp .env.example .env
```
2. Set a real `JWT_SECRET` in `.env`.
3. Start stack:
```bash
docker compose up --build
```

App URLs:
- Frontend: `http://localhost:3000`
- Backend: `http://localhost:8080`

## Environment Variables
- `JWT_SECRET` (required in production)
- `CORS_ALLOWED_ORIGIN_PATTERNS` (comma-separated list, example: `https://app.example.com`)
- `AI_PROVIDER` (`LOCAL` or `OPENAI`)
- `OPENAI_API_KEY` (required when `AI_PROVIDER=OPENAI`)
- `OPENAI_MODEL` (default `gpt-4o-mini`)

## CI
GitHub Actions workflow at:
- `/Users/kartik/Documents/Englishapp/.github/workflows/ci.yml`

Checks:
- Backend: `mvn -q test`
- Frontend: `npm ci && npm run build`

## Main APIs
- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/streak`
- `GET /api/learn/chapters?level=MID|PRO`
- `GET /api/learn/lessons?level=MID|PRO&chapterNo=...`
- `POST /api/learn/answer`
- `POST /api/ai/chat`
- `GET /api/ai/history`

## Deploy on Render
This repo includes a Render Blueprint:
- `/Users/kartik/Documents/Englishapp/render.yaml`

Steps:
1. Push this project to GitHub.
2. In Render: `New +` -> `Blueprint`.
3. Select your repo and deploy.
4. After first deploy, open backend service env vars and set:
   - `DB_URL` (JDBC format), example:
     `jdbc:postgresql://<db-host>:5432/<db-name>?sslmode=require`
   - `DB_USERNAME`
   - `DB_PASSWORD`
   - `CORS_ALLOWED_ORIGIN_PATTERNS` to your frontend URL
5. Open frontend service env vars and set:
   - `VITE_API_BASE_URL` to backend public URL (`https://...onrender.com`)
6. Redeploy backend and frontend.
7. Optional AI:
   - backend `AI_PROVIDER=OPENAI`
   - backend `OPENAI_API_KEY=<your-key>`

## Deploy on Railway
Railway setup (manual):
1. Create a PostgreSQL service.
2. Create backend service from `backend/Dockerfile`.
3. Set backend env vars:
   - `SPRING_PROFILES_ACTIVE=cloud`
   - `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` from Railway Postgres
   - `JWT_SECRET` (long random string)
   - `CORS_ALLOWED_ORIGIN_PATTERNS` with frontend URL
4. Create frontend service from `frontend` (Node static build):
   - Build: `npm ci && npm run build`
   - Start: serve static `dist` (or deploy as static site)
   - Set `VITE_API_BASE_URL` to backend public URL and redeploy.
