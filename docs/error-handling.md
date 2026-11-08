# Error Handling

Every feature handles all its error paths, not only the happy one, and the user always sees a proper message. An error the user can act on appears next to what caused it; an error the user cannot fix appears in a modal.

## Two kinds of errors

| | Business error | System error |
|---|---|---|
| Meaning | The request is correct, but the domain refuses its substance: not found, conflict, business rule violated | Something is broken: server failure, network down, an incorrect request, unexpected response |
| HTTP | `404`, `409`, `422` | `400`, `5xx`, network failure, any response that is not a valid error body |
| Shown | Inline, next to the component or form that triggered the action | Modal dialog |
| User can | Fix the input or choose another action | Retry or come back later |

## Backend

Exceptions are turned into HTTP statuses in one place: `ApiExceptionHandler` in `shared/web`, a `@RestControllerAdvice` extending `ResponseEntityExceptionHandler` (which also maps Spring MVC's own exceptions, e.g. an unreadable body → `400`). Controllers and use cases never build error responses and never catch exceptions to return a status.

### Exceptions
- Business errors extend `BusinessException` (`shared/domain/error`) and carry a stable `code`. Kinds: `NotFoundException`, `ConflictException`, `RuleViolationException` (optionally naming a `field`), `InvalidValueException`.
- Use cases and the domain throw them; the advice maps each type to its status:

| Exception | Status |
|---|---|
| Entity not found | `404 Not Found` |
| State conflict (duplicate, concurrent change) | `409 Conflict` |
| Business rule violated: the form is correct, its substance is not | `422 Unprocessable Entity`, with field errors when the rule concerns a field |
| Incorrect request: malformed JSON, wrong types, a value failing value object or Bean Validation format rules | `400 Bad Request`, with field errors |
| Anything else | `500 Internal Server Error` |

- **`422` vs `400`.** `422` means the request is a correct form, but the domain refuses what it says ("this title is already taken"). Format rules of a form (required, length, pattern) are checked by the frontend before sending, so a request that breaks them is a client bug: `400`, a system error. The backend still enforces them.
- A `500` is logged with its stack trace; its body never reveals internals (no messages, class names or SQL).

### Response body
Every error is a `ProblemDetail` (RFC 9457, `application/problem+json`) with extra properties:

```json
{
  "type": "about:blank",
  "title": "Unprocessable Entity",
  "status": 422,
  "detail": "Release date cannot be in the future.",
  "code": "software.release-date-in-future",
  "errors": [
    { "field": "releaseDate", "code": "software.release-date-in-future" }
  ]
}
```

- `code` — stable machine-readable identifier `{domain}.{error}`. The frontend chooses the message by it.
- `errors` — present for field-level problems (`422` business rules and `400` format rules); `field` matches the request field name.
- `detail` — English, for logs and developers; not shown to users as is.

## Frontend

- The API client in `shared/api` (`getJson`) fails only with `BusinessError` (`status`, `code`, `fieldErrors`) — for `404`/`409`/`422` whose body has a `code` — or `SystemError` with a `kind`: `network`, `server` (any other status, including a `404` without `code`), `response` (a body the client cannot read or map).
- Slices load data with `useRequest` and run user actions (save, delete) with `useAction(action, onSuccess)`: both return business errors in their state and report system errors with a retry through `reportSystemError`; the slice never sees them. The retry of an action repeats it with the same input.
- Form format rules (required, length, pattern) are validated on the frontend before sending and shown under the fields; they are the same rules the backend enforces.
- **Business errors** are handled by the slice that made the request and shown where the action happened:
  - field errors under their fields, form-level errors at the top of the form;
  - errors of a component's own action (a button, a list load) inside that component;
  - "not found" for a page's main entity as the page's not-found state.
- **System errors** are not handled by slices. They go to the `system-error-dialog` widget, mounted once by `App`, which explains the failure by its kind and offers retry where the action can be repeated.
- User-facing texts are chosen on the frontend by `code`: `businessErrorMessage(error, messages)` takes the slice's texts (its `config/messages.ts`) and falls back to a generic text for the status.

## Tests

Each feature covers every error path, not only success:

- **Backend** — a controller test per error status the endpoint can return, asserting `status`, `code` and `errors`; domain/use case tests for every rule that throws.
- **Frontend** — a test per error the feature can receive: each business error renders in the right place with the right text; a system error opens the modal.
- **Fixtures** — data needed to reproduce business errors in e2e (e.g. an entity in a conflicting state) is part of the feature's fixtures.
