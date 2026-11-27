# Value Objects

Domain values of the backend are value objects (VO), not bare scalars: an id of one entity cannot be passed where another is expected, and a value that exists is always valid.

## Where they live

| Place | Types |
|---|---|
| Domain entities and domain services | VO |
| Use case commands/queries and their results | VO |
| REST request and response DTOs | Scalars (`string`, `number`, `boolean`, ISO dates) |
| Persistence (JPA entities, SQL) | Scalars |

Conversion happens only at the edges: the web mapper (DTO ↔ command/domain) and the infra mapper (JPA entity ↔ domain). Nothing in between unwraps a VO into a scalar.

## In the backend (Java)

- A VO is a `record` annotated `@ValueObject`, with one or a few components: `SoftwareId(UUID value)`. A fixed list of values is an `enum` annotated `@ValueObject`, each value known by its code: `ReleaseType`.
- A VO that is a case of a general concept extends that concept's abstract VO in the shared kernel `shared/domain`, which holds the shared rules: every title of the archive is a `Title` (required, not blank, at most 255 characters), so `SoftwareTitle` is a final `@ValueObject` class extending `Title`. The base validates in its constructor, so no case can skip the rules; equality is by class and value.
- A VO whose validity depends on data outside it (the reference book) is a final class without a public constructor, built only by its factory domain service, which checks that data — see [backend.md](backend.md#domain-services-and-model-domain).
- Scalars that need a VO: `String`, `UUID`, numbers, booleans. `java.time` types are already value types and are used as is.
- Every id gets its own VO type; ids of different entities are never interchangeable.
- The compact constructor enforces the invariants: `null` is a programming error (`NullPointerException`); a value breaking a format rule throws `InvalidValueException` with a `code`. A VO built from a request that breaks a format rule yields `400` — see [error-handling.md](error-handling.md).
- VOs live in the `domain` package of their domain, next to the entity they describe; shared ones in `shared/domain`. The `@ValueObject` marker itself is in `shared/domain/ddd`.
- VOs are immutable and compared by value.

Value objects are a backend matter: the frontend takes the plain types of the REST contract ([frontend.md](frontend.md#data-loading)). ArchUnit forbids scalar fields in `domain`/`app` ([backend.md](backend.md#static-checks)).
