# Testing

All development is test-driven: **Red → Green → Refactor**.

1. **Red** — write a test for the next piece of behaviour and run it. It must fail, and for the expected reason (an assertion, not a compilation or setup error).
2. **Green** — write the minimum production code that makes it pass.
3. **Refactor** — clean up code and tests with all tests green.

No production code is written without a failing test that requires it. A bug fix starts with a test that reproduces the bug.

Every feature is tested on the happy path AND on every error path it can produce — see [error-handling.md](error-handling.md#tests).

Tests describe behaviour, not implementation: assert on results and visible effects, not on internal calls.

## Backend

JUnit 5 with `spring-boot-starter-test`.

- `*Test` — unit and slice tests, no database. Includes the ArchUnit architecture rules. Run: `cd backend && ./mvnw test` (single test: `-Dtest=ClassName`).
- `*IT` — integration tests against a throwaway PostgreSQL container (Testcontainers, shared `TestcontainersConfiguration`). Run with everything else: `cd backend && ./mvnw verify` (single test: `-Dit.test=ClassName`). They never touch the dev database. Tests that start the whole application (`@SpringBootTest`) also import `TypesenseTestConfiguration`: a throwaway Typesense, with the search worker off its schedule so the test drives it.

| Layer | Test |
|---|---|
| Domain model and domain services | Plain unit tests, no Spring context |
| Use cases | Unit tests with in-memory fakes of the domain repository interfaces |
| Controllers | `@WebMvcTest` slice with the use case mocked; asserts HTTP status and JSON |
| Repository implementations | Integration tests against PostgreSQL with Flyway migrations applied |

## Frontend

Vitest + React Testing Library. Tests sit next to the code: `{Component}.test.tsx`.
Tests run inside the `frontend` container, which owns `node_modules`: `docker compose exec frontend npm test`.
- Network is stubbed with the helpers from `shared/lib/test` (`stubFetch` answers in order, `stubFetchRoutes` by URL for blocks loading in parallel; `json`, `pageResponse`); test data is declared in the test itself — domain data never goes to `shared`.
- Components that load or change data are rendered with `renderWithQueries(ui)` (`shared/lib/test`): every test gets a fresh query client, so no cache leaks between tests. Page tests create the client themselves and pass it to the route loader and to `renderWithQueries`.
- System error paths are tested by subscribing with `subscribeSystemErrors` or by rendering `App`, which mounts the error dialog.
Query elements the way a user finds them (role, label, text), not by CSS class or test id when an accessible query exists.
- Exception: design system atoms (`shared/ui`) promise markup — the element, its classes, its attributes — so their tests may read the rendered DOM node and its classes (CSS module class names are not scoped in tests).
- Pages are tested through a memory router with their real loader, so the preload window is part of the test: data in time means no skeleton, a slow call means a skeleton first.
- Timers, animation frames and intersection are faked where behaviour depends on them (`vi.useFakeTimers`, a stubbed `IntersectionObserver`).
- The minimum skeleton time is 0 in tests (`vitest.setup.ts` mocks `shared/lib/loading/skeleton-timing`), so a block test does not wait for it; a test of that hold unmocks it in a file of its own, `{Component}.hold.test.tsx`.
- Network helpers also include `stubFetchAround`: it answers the calls every page makes (the platform switch, a lazy block) by URL, and the others in order.
- The local Stylelint plugin is tested by linting code snippets as files (`frontend/stylelint/*.test.js`).

## Fixtures

Fixtures are the shared data set for e2e tests and for working with a filled local database.

- **Every feature adds fixtures for the data it introduces**, in the same task, so its e2e scenarios have something to work with.
- One file per domain: `backend/src/main/resources/db/fixtures/R__{n}_{domain}.sql` (Flyway repeatable migrations). Flyway applies them in the order of their names, so `{n}` puts a domain after the ones its rows refer to (authors' locations refer to geo).
- Fixtures are applied only with the `fixtures` profile, on top of the schema migrations.
- Rows have fixed UUIDs so tests can reference them; large sets (geo, authors) derive them by a rule written at the top of the file, and are written as `INSERT … ON CONFLICT (id) DO UPDATE`: editing a fixture file re-applies it without duplicates.
- `FixturesIT` checks that the data set loads and is served by the API; extend it when a domain gets fixtures.
