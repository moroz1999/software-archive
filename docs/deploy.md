# Production Deployment

The archive is served at retroteka.org (`SITE_ADDRESS` in `.env.prod`). The whole archive runs on one host from `compose.prod.yaml`. Caddy is the only service reachable from outside (ports 80 and 443): it serves the static frontend, proxies `/api` to the backend and obtains the HTTPS certificate itself. PostgreSQL, Typesense and the backend stay in the internal network of the stack.

The steps of the first start and of deploying a new version are in the [README](../README.md#deploying-to-production).

On the first start the backend applies the migrations to the empty database — never the fixtures, which need their profile — and builds the search collections in Typesense.

## Data

Named volumes keep what must survive a rebuild of the containers: the database, the search index, the uploads (screenshots and release archives, business data like the database: back them up together) and Caddy's certificates. `down` keeps them; only `down -v` deletes them. The search index can always be built anew from the database ([features/search.md](features/search.md#initial-load-and-rebuild)).

## Mail

Letters (the confirmation links of registration) go through an external SMTP relay given in `.env.prod`, over STARTTLS. A mail server of our own is not run: its letters would land in spam, and many hosts block outgoing port 25. The sender domain must have SPF and DKIM set up at the relay, otherwise letters are rejected or marked as spam. When the relay cannot be reached, registration fails with a system error and nothing is kept; the first administrator is created by the `create-admin` command, which needs no mail.

## Not set up yet

- Permissions of accounts are configured in the database.
