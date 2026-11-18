#!/usr/bin/env sh
# Writes .env.prod for compose.prod.yaml: asks the domain and the SMTP relay, generates the passwords and the secret (docs/deploy.md).
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

printf 'SMTP host: '
read -r smtp_host
printf 'SMTP port [587]: '
read -r smtp_port
printf 'SMTP user name: '
read -r smtp_username
printf 'SMTP password: '
stty -echo
read -r smtp_password
stty echo
echo
printf 'Sender of letters [no-reply@%s]: ' "$domain"
read -r mail_from
if [ -z "$smtp_host" ] || [ -z "$smtp_username" ] || [ -z "$smtp_password" ]; then
    echo "The SMTP host, user name and password are required." >&2
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

SMTP_HOST=$smtp_host
SMTP_PORT=${smtp_port:-587}
SMTP_USERNAME=$smtp_username
SMTP_PASSWORD=$smtp_password
MAIL_FROM=${mail_from:-no-reply@$domain}
EOF

echo ".env.prod written for $domain."
