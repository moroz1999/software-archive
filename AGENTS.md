# AGENTS.md

This file contains the most CRITICAL rules that ALL agents must follow.
You MUST read the MD files linked in this document which are relevant to the current task.

## CRITICAL RULES

### General
- Keep it simple, don't invent unnecessary checks.
- Before each new task, read `AGENTS.md` and review the relevant rules if task scope differs from previous.
- All documentation and code comments MUST be in English, even if the user communicates in Russian or another language.
- After completing a task, re-read the task description and verify every point.
- After finishing the task code, read the rules on this topic from the .md docs once again and double-check your changes.
- Before starting a task, you MUST read the relevant documents from the DOCUMENTATION TREE below.
- ALWAYS add newly created files to GIT immediately after creation.
- Commit directly to `master`; do not create feature branches. Commit only the files of your own task (explicit pathspecs) — the working tree may hold unrelated uncommitted work.
- When the IDE is in 'Ask' (readonly) mode, it is STRICTLY FORBIDDEN to do anything except answering the user's question. No file modifications or tool calls that change state are allowed.
- ALWAYS use MCP tools (JetBrains IDE) when available for code search, file reading, navigation, and locating files, methods, and classes instead of Grep/Glob/Read/Bash.
- When checking file problems via IDE (`get_file_problems`), ALWAYS request ALL severity levels (`errorsOnly: false`). Weak warnings (unused imports, unused parameters, etc.) must be caught too.
- Every feature is done only when ALL its error paths work and are shown to the user properly, not just the happy path — see [docs/error-handling.md](docs/error-handling.md).
- Do not scan the whole project by file extension or run naive recursive searches over the entire repository. Pick the specific directories from the PROJECT STRUCTURE that match the task.

### Documentation
- Documentation does NOT duplicate the code. It records only the business motivation: why something exists, which business rules it follows, and what the code cannot tell by itself. Do not restate what can be read from the code — field lists, signatures, call flows, configuration values.
- Documentation updates must be placed in the appropriate .md file (e.g., backend rules in `docs/backend.md`).
- Documentation must describe only the CURRENT state of the code. Do NOT mention services, classes, practices, or techniques that were removed or replaced, and do NOT describe the prior state of refactored code (no "replaces X", "formerly Y", "no longer uses Z", "previously did W"). Git history is the record of the past.
- EVERY business fact learned during work (from the user, a task, a discussion) MUST be written down in the domain documentation right away. Each domain has its own document `docs/domain/{domain}.md`; `docs/domain.md` is only the index. A new domain gets a new document and an index entry.
- Documentation additions in `docs` must be concise, clear, and only about the core points.
- **Domain documentation (`docs/domain.md` and `docs/domain/*.md`) describes WHAT the archive does and WHY, never HOW it is built.** It is written for someone who needs to understand the subject area, not the codebase. Forbidden there: class, record, enum, method, component and file names; packages and paths; endpoint URLs, query parameters and response field lists; SQL, table and column names; framework and library names. Name the entities, their fields as the user meets them, the rules they obey and the behaviour visitors see. Implementation belongs in `docs/backend.md`, `docs/frontend.md` or a feature document in `docs/features/` — and the domain document does not link to the code, because code moves and the domain does not.
- General backend/frontend documents contain domain-neutral architecture rules only. Entity-specific behaviour and REST contracts belong in `docs/features/*.md`; the business rules behind them belong in `docs/domain/*.md`.

### Mandatory Task Workflow
Every task follows these stages in order. Do not merge stages or continue past an approval gate without explicit user approval.

1. The user provides a task.
2. Research the relevant code without changing project files. Briefly describe the essence of the proposed fix as clear implementation steps, then wait for approval.
3. **Red.** After approval, add or update tests for the requested behavior and run only those tests. Confirm that they fail for the expected reason, then wait for approval of the tests. If no meaningful automated test can be written, explain why and wait for explicit approval to proceed without one. See [docs/testing.md](docs/testing.md).
4. **Green.** After test approval, implement the minimum change that makes the new tests pass, run those tests, refactor with them green, and run only the relevant linters. Do not run the full test suites or other validation commands at this stage.
5. The user tests and reviews the implementation. Apply their feedback within the same implementation stage and run only the relevant linters after each code update.
6. After the user explicitly approves the implementation, run the relevant test suites, builds, and any other checks mandated by the task-specific documentation.
7. When all final checks pass, report the result and wait for a separate, explicit approval to commit. Only then commit the files belonging to the task with a short commit message describing the completed change. No other approval implies this one — not an approval of the implementation, and not a request to work through several tasks unattended: such work stays as uncommitted changes until the user says to commit.

An approval advances only the next pending gate. Requirements elsewhere in this file and the linked documentation still apply; their validation commands are deferred to the final validation stage when this workflow requires it.

## DOCUMENTATION TREE

Read ONLY the documents relevant to your task.

### Core Documentation
- **[docs/domain.md](docs/domain.md)** - Archive domain and entities: WHAT the archive does, never HOW
- **[docs/features/](docs/features/)** - How individual features are built: the implementation behind a domain document
- **[docs/local-dev.md](docs/local-dev.md)** - Running the stack locally and reaching it over HTTP
- **[docs/value-objects.md](docs/value-objects.md)** - Branded value objects in domain, commands and entities; scalars only in request/response and persistence
- **[docs/error-handling.md](docs/error-handling.md)** - Business vs system errors: exceptions → REST statuses, inline errors vs error modal; every error path is tested
- **[docs/testing.md](docs/testing.md)** - Red → Green → Refactor: all development is test-driven; test types per layer; e2e fixtures

### Backend (Java / Spring Boot)
- **[docs/backend.md](docs/backend.md)** - DDD: controllers, use cases, domain services, repositories; mapping, persistence

### Frontend (React)
- **[docs/frontend.md](docs/frontend.md)** - Strict Feature-Sliced Design, state in providers and context, static checks, value objects, errors
- **[docs/styles.md](docs/styles.md)** - Styling rules: component variables → semantic theme → base tokens; typography, layout, CSS Modules, the Stylelint plugin
- **[docs/data-loading.md](docs/data-loading.md)** - Route preload with a 300 ms window, lazy loading by viewport, skeletons on first load, spinners for pagination/filters
- **[docs/design-system.md](docs/design-system.md)** - Own design system: principles and component set in `shared/ui`

## COMMANDS

`make` lists the everyday commands (`make up`, `make run-fixtures`, `make verify`, `make check`, …); they wrap the ones below.

```bash
# Infrastructure (postgres, typesense, frontend dev server)
docker compose up -d

# Backend: run (applies Flyway migrations on start); add fixture data with the profile
cd backend && ./mvnw spring-boot:run
cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=fixtures

# Backend tests: unit (*Test) / unit + integration (*IT, Testcontainers)
cd backend && ./mvnw test
cd backend && ./mvnw verify

# Backend: format the Java sources (every build does it too)
cd backend && ./mvnw spotless:apply

# Frontend: run inside the container (it owns node_modules)
docker compose exec frontend npm run check      # types + ESLint + Stylelint + Steiger + tests
docker compose exec frontend npm test
docker compose exec frontend npm run typecheck
docker compose exec frontend npm run lint
docker compose exec frontend npm run lint:css
docker compose exec frontend npm run lint:fsd
docker compose exec frontend npm run build

# Backend inside WSL: point the frontend /api proxy at it (before docker compose up)
./scripts/wsl-api-target.sh
```

## PROJECT STRUCTURE

- `/backend/` - Spring Boot application
  - `/backend/src/main/java/com/archive/backend/{domain}/` - One package per domain (DDD): `web` controllers, `app` use cases, `domain` model + domain services + repository interfaces, `infra` repository implementations
  - `/backend/src/main/java/com/archive/backend/shared/` - Cross-module configuration
  - `/backend/src/main/resources/db/migration/` - Flyway migrations
- `/frontend/` - React + Vite application, strict FSD: `app` → `pages` → `widgets` → `features` → `entities` → `shared`
  - `/frontend/src/shared/ui/` - Design system components
  - `/frontend/src/app/styles/` - Palette, tokens, semantic theme, component theme files (`components/`), reset
  - `/frontend/stylelint/` - Local Stylelint plugin enforcing the variable hierarchy, with its tests
- `/data/` - Local data files
- `/docs/` - All documentation
