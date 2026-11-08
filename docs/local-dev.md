# Local Development

Infrastructure and the frontend dev server run in Docker; the backend runs on the host (IDE or `./mvnw spring-boot:run`) for fast restarts and debugging.

```bash
docker compose up -d
cd backend && ./mvnw spring-boot:run                                       # empty database
cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=fixtures   # with fixture data
```

A database once seeded with fixtures keeps them and still starts without the profile.

| Service | URL |
|---|---|
| Frontend (Vite, hot reload) | http://localhost:5173 |
| Backend REST API | http://localhost:8080/api/... |
| PostgreSQL | `localhost:5432`, database `mydb` |
| Typesense | http://localhost:8108/health |

The frontend dev server proxies `/api` to the backend at `API_PROXY_TARGET` (from `.env`, default `http://host.docker.internal:8080`). Docker Desktop's `host.docker.internal` is the Windows host and does not reach a backend running inside WSL; then point the proxy at the WSL address before starting the containers (it changes after a restart):

```bash
./scripts/wsl-api-target.sh && docker compose up -d frontend
```

The backend connects to PostgreSQL on `localhost:5432` and applies Flyway migrations on start. Spring DevTools restarts the application when compiled classes change.

Check the database from the container:

```bash
docker exec software-archive-postgres-1 psql -U myuser -d mydb -c '\dt'
```
