#!/bin/sh
# Points the frontend dev server's /api proxy at a backend running inside WSL.
# Docker Desktop's host.docker.internal is the Windows host, which does not reach WSL ports;
# the WSL address does, but changes after a restart — run this before `docker compose up`.
set -eu
cd "$(dirname "$0")/.."
ip=$(hostname -I | awk '{print $1}')
grep -v '^API_PROXY_TARGET=' .env 2>/dev/null > .env.tmp || true
echo "API_PROXY_TARGET=http://$ip:8080" >> .env.tmp
mv .env.tmp .env
echo "API_PROXY_TARGET=http://$ip:8080"
