# Everyday commands of the project; `make` alone lists them.
# The backend runs on the host through the Maven wrapper, the frontend inside its container (it owns node_modules).

MVNW := cd backend && ./mvnw
FRONTEND := docker compose exec frontend npm run

.DEFAULT_GOAL := help
.PHONY: help up down wsl-up run run-fixtures clean test verify check test-front lint typecheck build

help: ## List the commands
	@grep -E '^[a-z-]+:.*## ' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*## "} {printf "  \033[36m%-14s\033[0m %s\n", $$1, $$2}'

# Infrastructure

up: ## Start postgres, typesense and the frontend dev server
	docker compose up -d

down: ## Stop the containers
	docker compose down

wsl-up: ## Point the frontend /api proxy at a backend inside WSL, then start the containers
	./scripts/wsl-api-target.sh
	docker compose up -d

# Backend

run: ## Run the backend (applies migrations)
	$(MVNW) spring-boot:run

run-fixtures: ## Run the backend with the fixture data set; cleans the build so no stale fixture is applied
	$(MVNW) clean spring-boot:run -Dspring-boot.run.profiles=fixtures

clean: ## Clean the backend build
	$(MVNW) clean

test: ## Backend unit tests
	$(MVNW) test

verify: ## Backend unit and integration tests (Testcontainers)
	$(MVNW) verify

# Frontend

check: ## Frontend: types, ESLint, Stylelint, Steiger and tests
	$(FRONTEND) check

test-front: ## Frontend tests
	$(FRONTEND) test

lint: ## Frontend ESLint, Stylelint and Steiger
	$(FRONTEND) lint
	$(FRONTEND) lint:css
	$(FRONTEND) lint:fsd

typecheck: ## Frontend types
	$(FRONTEND) typecheck

build: ## Frontend production build
	$(FRONTEND) build
