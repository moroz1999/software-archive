# Backend

Spring Boot 3, Java 21, PostgreSQL, Maven wrapper (`backend/mvnw`).

## Architecture: DDD

Each domain is a package under `com.archive.backend`. A request passes four roles, always in this order:

**Controller → Use case → Domain service / domain model → Repository**

### Controllers (`web`)
- Endpoints follow the frontend's logical calls: one endpoint per logical block of a view, not one per page (see [data-loading.md](data-loading.md)).
- HTTP only: read the request, call exactly one use case, map the result to a DTO.
- No business logic, no transactions, no repository access, no JPA entities.
- DTOs are records in `web/dto`; domain → DTO mapping is a MapStruct mapper in `web/mapper`.

### Use cases (`app`)
- One use case per user action: an input record and its handler with a `handle` method. The input is either
  - a **command** (`CreateSoftwareCommand` + `CreateSoftwareHandler`) — changes state: loads domain entities through their repository, lets them apply the rules, saves them; it never touches the read side. A command returns nothing, except the id of the entity it creates;
  - or a **query** (`GetSoftwareQuery` + `GetSoftwareHandler`) — reads: asks the read repository and returns a read model; `handle` is `@Transactional(readOnly = true)`. Of the domain it uses only value objects and business exceptions, never entities or repositories.
- Owns the transaction boundary.
- Holds no business rules itself — they live in the domain.

### Domain services and model (`domain`)
- Business rules belong to the domain model first. A domain service holds a rule only when it does not belong to a single entity.
- An invariant of an entity that needs data outside its aggregate (a reference book) is never checked by the use case. A factory domain service checks that data once and builds an **always-valid value object** nothing else can build; the entity accepts only that value object and checks the rest itself, so the type holds the invariant and no use case can forget it. Example: `HardwareSets` builds a `HardwareSet`, and `Software` keeps the hardware of every release on its own platform.
- Such data comes through a port declared in `domain` and named after what it is (`HardwareCatalog`), implemented in `infra`. A `Repository` is only ever the collection of an aggregate.
- The repository adapter restores always-valid value objects read back from storage with their `restore` factory: they were checked when stored. ArchUnit forbids `restore` outside `infra`.
- Domain services are plain classes; `shared/config/DomainServicesConfig` makes them Spring beans.
- Pure Java: no Spring web, JPA or other infrastructure dependencies.
- Grouped by concept, never by kind of class: an entity, its value objects, its repository interface and its exceptions sit together (`Software`, `SoftwareTitle`, `SoftwareRepository`, `SoftwareNotFoundException`). A growing domain is split by aggregate (`software/domain/release`). Packages such as `exceptions`, `vo`, `entities`, `models`, `services` are forbidden.

### Reads: a plain read model
- Reading never loads domain entities. Entities exist to guard rules on change; a read changes nothing, so it gets a simple **read model**: records shaped for the response, with scalars.
- The read side lives in the domain's `read` package: the read models and the read repository interface (`SoftwareReadRepository`). It is implemented in `infra` and does not depend on `domain`.
- A read model that no single table holds (the author and group catalogs mix owners and their labels) is read with plain SQL through `JdbcClient`: the adapter is `Jdbc{X}ReadRepository`. SQL that several domains share lives in the read side of the domain it is about: `label/read` holds the main name of people and groups (`MainNameSql`) and the catalog letter condition (`LetterFilterSql`).
- A query (`GetSoftwareQuery` + `GetSoftwareHandler`) only asks the read repository and throws `NotFoundException` for a missing entity; the controller maps the read model to its DTO.
- Domain entities and their repository serve only commands.

Domain entities, domain services and use case commands/results use value objects, DTOs and JPA entities use scalars — see [value-objects.md](value-objects.md).

### Repositories
- The repository interface is declared in `domain` and speaks domain objects.
- The implementation lives in `infra`: JPA entities, Spring Data repositories and the MapStruct entity ↔ domain mapper.
- JPA entities never leave `infra`.
- A domain repository serves only commands, so it loads an aggregate only to change it: the row of its root is locked (`PESSIMISTIC_WRITE`) until the transaction commits. A second change of the same aggregate waits and then checks its invariants against the committed state; reads go through the read side and never wait. Aggregates carry no versions.
- Spring Data `Page`/`Pageable` are allowed in domain repository interfaces: an own pagination type would add mapping without adding meaning.
- Naming: domain interface `SoftwareRepository`, Spring Data interface `SoftwareJpaRepository`, adapter `JpaSoftwareRepository`.

Cross-domain configuration lives in `shared/config`.

Use cases and the domain signal failures by throwing business exceptions; one `@RestControllerAdvice` in `shared/web` turns exceptions into REST statuses and `ProblemDetail` bodies. Controllers never catch exceptions — see [error-handling.md](error-handling.md).

## Shared building blocks

- `shared/domain` — the shared kernel: concepts of the archive used by several domains, such as `Title`, the abstract value object of every title.
- `shared/domain/error` — the taxonomy of business errors: `BusinessException` and its kinds (`NotFoundException`, `ConflictException`, `RuleViolationException`, `InvalidValueException`), mapped as a whole by `ApiExceptionHandler`.
- `shared/domain/ddd` — DDD building-block markers: `@ValueObject`.
- `shared/web` — `ApiExceptionHandler`, the only exception → REST status mapping.
- `shared/config` — `SecurityConfig`; `DomainServicesConfig` registers the domain services; `WebConfig` fixes the page JSON contract: `{content: [...], page: {size, number, totalElements, totalPages}}`.

## Mapping

- MapStruct mappers use `componentModel = "spring"`.
- MapStruct runs together with Lombok through `lombok-mapstruct-binding` in the compiler's `annotationProcessorPaths`; without it MapStruct cannot see Lombok accessors and generates empty mappers.

## Persistence

- The schema is owned by Flyway (`src/main/resources/db/migration`), applied on application start.
- Hibernate runs with `ddl-auto: validate`: an entity that does not match the schema stops the application from starting.
- Schema changes are versioned migrations only (`V{n}__…`). Repeatable migrations (`R__…`) are reserved for fixtures — see [testing.md](testing.md#fixtures).

## Static checks

All run on every build; nothing is suppressed to get a change through.

- **Formatter** — palantir-java-format through Spotless. Every build formats the Java sources before compiling them (`process-sources`), so code is never reviewed or committed unformatted; `make format` (`./mvnw spotless:apply`) does it alone. Unused imports are removed.
- **Compiler** — `-Xlint:all -Werror` (only `processing` is off: annotations without a processor are normal). Any warning fails the build.
- **Error Prone + NullAway** — run inside `javac` (`.mvn/jvm.config` opens the compiler internals they need). Everything in `com.archive.backend` is non-null unless marked `@Nullable` (`org.jspecify.annotations.Nullable`). Generated sources (MapStruct) are excluded.
- **ArchUnit** — `architecture/ArchitectureTest`, part of `./mvnw test`. It enforces:

| Rule | Check |
|---|---|
| Layers: `web → app → domain`, `infra → domain`; `read` is used by `web`, `app` and `infra`; nothing depends on `web` or `infra` | `layers` |
| `read` does not depend on any domain package | `readSideIsFreeOfDomain` |
| Records in `app` end with `Command` or `Query` | `useCaseInputsAreCommandsOrQueries` |
| A query handler uses of the domain only value objects and business exceptions | `queriesUseOnlyTheReadSide` |
| A query handler's `handle` is `@Transactional(readOnly = true)` | `queriesAreReadOnly` |
| A command handler does not use `read` | `commandsDoNotUseTheReadSide` |
| No domain subpackage is named after a kind of class (`exceptions`, `vo`, `entities`, `models`, `services`, …); `shared/domain/error` and `shared/domain/ddd` are the shared exceptions | `domainIsGroupedByConcept` |
| `domain` has no Spring (except `Page`/`Pageable`), Jakarta, Hibernate or Jackson | `domainIsFrameworkFree` |
| `@Entity` classes live in `infra` and end with `Entity` | `jpaEntitiesLiveInInfra` |
| Spring Data repositories live in `infra` and end with `JpaRepository` | `springDataRepositoriesLiveInInfra` |
| `@RestController`s live in `web` and end with `Controller` | `controllersLiveInWeb` |
| `@Service`s in `app` end with `Handler` | `useCasesAreHandlers` |
| Exceptions in `domain`/`app` extend `BusinessException` | `exceptionsAreBusinessExceptions` |
| `@ValueObject`s live in `domain` and are records, enums, abstract shared value objects of the shared kernel, final cases of one, or final classes without public constructors (always-valid, built by their factory) | `valueObjectsAreDomainRecords` |
| `HardwareSet.restore` is called only in `infra` | `hardwareSetsAreRestoredOnlyInInfra` |
| No scalar fields (`String`, `UUID`, numbers, booleans) in `domain`/`app` classes except value objects and exceptions | `noScalarsInDomainAndCommands` |

Not checkable by ArchUnit, left to review: controllers and use cases never catch exceptions to build responses.
