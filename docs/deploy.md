# Production Deployment

The whole archive runs on one host from `compose.prod.yaml`. Caddy is the only service reachable from outside (ports 80 and 443): it serves the static frontend, proxies `/api` to the backend and obtains the HTTPS certificate itself. PostgreSQL, Typesense and the backend stay in the internal network of the stack.

The steps of the first start and of deploying a new version are in the [README](../README.md#deploying-to-production).

On the first start the backend applies the migrations to the empty database — never the fixtures, which need their profile — and builds the search collections in Typesense.

## Data

Named volumes keep what must survive a rebuild of the containers: the database, the search index, the uploads (screenshots and release archives, business data like the database: back them up together) and Caddy's certificates. `down` keeps them; only `down -v` deletes them. The search index can always be built anew from the database ([features/search.md](features/search.md#initial-load-and-rebuild)).

## Not set up yet

- No mail server: registration cannot send its confirmation link and fails with a system error. The first administrator is created by the `create-admin` command of the first start, which needs no mail.
- Permissions of accounts are configured in the database.
