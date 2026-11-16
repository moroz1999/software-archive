# Production image of the site: the static build of the frontend served by Caddy (compose.prod.yaml, docs/deploy.md).
FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM caddy:2-alpine
COPY --from=build /app/dist /srv
