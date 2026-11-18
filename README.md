# Software Archive

An archive of retro software: Spring Boot backend, React frontend, PostgreSQL and Typesense. How the local stack works: [docs/local-dev.md](docs/local-dev.md). How the production stack is built: [docs/deploy.md](docs/deploy.md).

## Local development

PostgreSQL, Typesense, Mailpit and the frontend dev server run in Docker; the backend runs on the host. `make` lists the everyday commands; each one wraps a command shown beside it.

### Infrastructure

| Make | Command | What it does |
|---|---|---|
| `make up` | `docker compose up -d` | Start postgres, typesense, mailpit and the frontend dev server |
| `make down` | `docker compose down` | Stop the containers |
| `make wsl-up` | `./scripts/wsl-api-target.sh && docker compose up -d` | Point the frontend `/api` proxy at a backend inside WSL, then start the containers |

### Backend

| Make | Command | What it does |
|---|---|---|
| `make run` | `cd backend && ./mvnw spring-boot:run` | Run the backend; applies the migrations on start |
| `make run-fixtures` | `cd backend && ./mvnw clean spring-boot:run -Dspring-boot.run.profiles=fixtures` | Run it with the fixture data set |
| `make reindex` | `cd backend && ./mvnw spring-boot:run -Dspring-boot.run.arguments=--search.rebuild=true` | Run it, building the search collections anew from the database |
| `make clean` | `cd backend && ./mvnw clean` | Clean the build |
| `make format` | `cd backend && ./mvnw spotless:apply` | Format the Java sources (every build does it too) |
| `make test` | `cd backend && ./mvnw test` | Unit tests |
| `make verify` | `cd backend && ./mvnw verify` | Unit and integration tests (Testcontainers) |

### Frontend

Run inside the container, which owns `node_modules`.

| Make | Command | What it does |
|---|---|---|
| `make check` | `docker compose exec frontend npm run check` | Types, ESLint, Stylelint, Steiger and tests |
| `make test-front` | `docker compose exec frontend npm test` | Tests |
| `make typecheck` | `docker compose exec frontend npm run typecheck` | Types |
| `make lint` | `docker compose exec frontend npm run lint`, `lint:css`, `lint:fsd` | ESLint, Stylelint and Steiger |
| `make build` | `docker compose exec frontend npm run build` | Production build |

### Addresses

| Service | URL |
|---|---|
| Frontend (Vite, hot reload) | http://localhost:5173 |
| Backend REST API | http://localhost:8080/api/... |
| PostgreSQL | `localhost:5432`, database `mydb` |
| Typesense | http://localhost:8108/health |
| Mailpit: every letter the backend sends | http://localhost:8025 |

## Deploying to production

The host needs Docker with Compose, ports 80 and 443 open, and a DNS `A` record of the domain pointing at it, so Caddy can obtain the HTTPS certificate.

### First start

From the root of the repository:

```bash
# once: asks the domain and the SMTP relay, generates the passwords and the secret into .env.prod
./scripts/init-env-prod.sh
docker compose --env-file .env.prod -f compose.prod.yaml build
# once, on the empty database: asks the user name, email and password of the first administrator, then to confirm them
docker compose --env-file .env.prod -f compose.prod.yaml run --rm backend create-admin
docker compose --env-file .env.prod -f compose.prod.yaml up -d --build
docker compose --env-file .env.prod -f compose.prod.yaml logs -f backend   # Flyway migrations, then "Started"
```

Check that the site answers:

```bash
curl https://<domain>/api/authors
```

On the first start the backend applies the migrations to the empty database (never the fixtures) and builds the search collections in Typesense.

### New version

```bash
git pull
docker compose --env-file .env.prod -f compose.prod.yaml up -d --build
```

A `.env.prod` written before mail was set up lacks the `SMTP_*` and `MAIL_FROM` lines: add them by hand from `.env.prod.example`; `up` refuses to start without them.

### Stopping

```bash
docker compose --env-file .env.prod -f compose.prod.yaml down
```

`down` keeps the data in the volumes; never run `down -v`, it deletes the database, the uploads and the certificates.

`.env.prod` holds the secrets and is never committed.
