# Production Deployment

The whole archive runs on one host from `compose.prod.yaml`. Caddy is the only service reachable from outside (ports 80 and 443): it serves the static frontend, proxies `/api` to the backend and obtains the HTTPS certificate itself. PostgreSQL, Typesense and the backend stay in the internal network of the stack.

## First start

Needs Docker with Compose and a DNS `A` record of the domain pointing at the host, so Caddy can get its certificate. From the root of the repository:

```bash
cp .env.prod.example .env.prod   # fill in the domain, the passwords and the JWT secret
docker compose --env-file .env.prod -f compose.prod.yaml up -d --build
docker compose --env-file .env.prod -f compose.prod.yaml logs -f backend   # Flyway migrations, then "Started"
```

The same command deploys a new version after `git pull`. `.env.prod` holds the secrets and is never committed.

On the first start the backend applies the migrations to the empty database — never the fixtures, which need their profile — and builds the search collections in Typesense.

## Data

Named volumes keep what must survive a rebuild of the containers: the database, the search index, the uploads (screenshots and release archives, business data like the database: back them up together) and Caddy's certificates. `down` keeps them; only `down -v` deletes them. The search index can always be built anew from the database ([features/search.md](features/search.md#initial-load-and-rebuild)).

## Not set up yet

- No mail server: registration cannot send its confirmation link and fails with a system error, so accounts are made by hand in the database.
- Permissions of accounts are configured in the database.
