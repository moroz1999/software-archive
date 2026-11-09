# Error Handling

Every feature handles all its error paths, not only the happy one, and the user always sees a proper message. An error the user can act on appears next to what caused it; a missing entity replaces the page; an error the user cannot fix appears in a modal.

## Three kinds of errors

| | Business error | Not found | System error |
|---|---|---|---|
| Meaning | The request is correct, but the domain refuses its substance: conflict, business rule violated | What the page is about does not exist: deleted, never existed, a malformed id, an unknown address | Something is broken: server failure, network down, an incorrect request, unexpected response |
| HTTP | `409`, `422` | `404` with a problem `code` | `400`, `5xx`, network failure, any response that is not a valid error body |
| Shown | Inline, next to the component or form that triggered the action | The not found page, in place of the page | Modal dialog |
| User can | Fix the input or choose another action | Go elsewhere | Retry or come back later |

Not found is not a business error: there is nothing to fix or choose, and every page meets it the same way, so it is handled in one place instead of in every block.

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

- The API client in `shared/api` (`getJson`) fails only with `NotFoundError` (`code`) — for a `404` whose body has a `code` —, `BusinessError` (`status`, `code`, `fieldErrors`) — for `409`/`422` whose body has a `code` — or `SystemError` with a `kind`: `network`, `server` (any other status, including a `404` without `code`), `response` (a body the client cannot read or map).
- **Not found** is never handled by slices. The query client throws a `NotFoundError` of any query or mutation into rendering (`throwOnError`), and the route error boundary (`widgets/route-error`, around every route) shows the not found page. The same page answers an address the app does not know and an id in the URL that cannot be one: the route loader and the page parse it with `require…Id`, which throws `NotFoundError`, so a malformed id is never sent to the backend. Anything else that breaks rendering shows the boundary's "Something went wrong" page.
- Slices load data with TanStack Query queries and run user actions (save, delete) as mutations, in their providers. The query client (`createQueryClient()`) reports every system error of a query or a mutation through `reportSystemError` with a retry — the retry of a query loads it again, the retry of a mutation repeats it with the same input — so slices never handle them. Business errors are shown only by writes: a mutation keeps its business error for the form or dialog that ran it (`businessErrorOf(error)`), while a business error of a read goes to the dialog like a breakage — a read whose business error must be shown inline (a lock, say) gets its own handling when it appears. Nothing is retried automatically.
- Form format rules (required, length, pattern) are validated on the frontend before sending and shown under the fields; they are the same rules the backend enforces.
- **Business errors** of a write are shown where the action happened:
  - in a form: an error naming a field of the form under that field, until the field is edited; anything else at the top of the form. The form's provider takes the business error as it is and the slice's texts, and splits it;
  - in a dialog (a delete confirmation): inside the dialog.
- **System errors** are not handled by slices. They go to the `system-error-dialog` widget, mounted once by `App`, which explains the failure by its kind and offers retry where the action can be repeated.
- User-facing texts are chosen on the frontend by `code`: `businessErrorMessage(error, messages)` takes the slice's texts (its `config/messages.ts`) and falls back to a generic text for the status.

## Tests

Each feature covers every error path, not only success:

- **Backend** — a controller test per error status the endpoint can return, asserting `status`, `code` and `errors`; domain/use case tests for every rule that throws.
- **Frontend** — a test per error the feature can receive: each business error renders in the right place with the right text; a system error opens the modal.
- **Fixtures** — data needed to reproduce business errors in e2e (e.g. an entity in a conflicting state) is part of the feature's fixtures.
