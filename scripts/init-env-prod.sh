#!/usr/bin/env sh
# Writes .env.prod for compose.prod.yaml: asks the domain, generates the passwords and the secret (docs/deploy.md).
# Never overwrites an existing .env.prod: its passwords are those of the database already created with them.
set -eu

cd "$(dirname "$0")/.."

if [ -e .env.prod ]; then
    echo ".env.prod already exists; delete it first to write a new one." >&2
    exit 1
fi

printf 'Domain of the site (e.g. test.zxart.ee): '
read -r domain
if [ -z "$domain" ]; then
    echo "The domain is required." >&2
    exit 1
fi

umask 077
cat > .env.prod <<EOF
SITE_ADDRESS=$domain

POSTGRES_USER=archive
POSTGRES_DB=archive
POSTGRES_PASSWORD=$(openssl rand -hex 24)

TYPESENSE_API_KEY=$(openssl rand -hex 24)

AUTH_JWT_SECRET=$(openssl rand -hex 48)
EOF

echo ".env.prod written for $domain."
