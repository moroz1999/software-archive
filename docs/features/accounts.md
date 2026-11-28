# Accounts and sessions

How registration, confirmation and signing in are built. The rules behind them: [domain/users.md](../domain/users.md).

## Backend

The `user` module. `User` and `Session` are aggregates; `Registrations` keeps a name and an email to one account and frees them from abandoned accounts, `AccountConfirmation` makes the first confirmed account an administrator, `Sessions` opens and refreshes sessions.

- Only digests (SHA-256) of the secrets handed out — confirmation links and refresh secrets — are stored, so the database alone opens no account and no session.
- Passwords are kept as BCrypt hashes.
- A confirmation link is mailed through a domain event (`ConfirmationLinkIssued`), answered inside the change by `MailConfirmationLinkHandler`: a letter that cannot be sent rolls the registration back and reaches the user as a system error.
- Letters go in the interface language the request names; any other falls back to English.

### Sessions

The access token is a JWT (HS256): its subject is the user id and `groups` holds the group codes. Login, confirmation and refresh return it in the response; the frontend keeps it only in memory and sends it as `Authorization: Bearer`. It works for `auth.access-lifetime` (15 minutes).

The random refresh secret is the only session cookie. Its path is `/api/auth`; it lasts for 30 days, or until the browser closes without "remember me". It is `HttpOnly`, `Secure` and `SameSite=Strict`: scripts cannot read it and no other site sends it. Browsers accept `Secure` cookies on `http://localhost`.

- A refresh secret works once: refreshing deletes its session and opens a new one, whose access token carries the groups the user has by then.
- Spring Security decides nothing about access: every request without authentication passes, and `AccessTokenBearerFilter` signs it in by its access token. A missing token is a public visitor; a presented expired or forged token answers `401`, so the frontend can refresh instead of silently reading as a visitor.
- A controller takes the `Actor` of the request as an argument (`ActorArgumentResolver`): the user and their groups, or a public visitor.

### Permissions

Who may change what ([domain/users.md](../domain/users.md#user-groups)) is decided in the domain, asked by the command handlers: every write command carries the `Actor`.

- Software, releases, people and groups remember who added them (`AddedBy`, the `added_by` column): adding one always takes the signed-in user (`AddedBy.of(Actor)`). The column is empty only once the adder's account is deleted (`ON DELETE SET NULL`), which only loading and reading meet (`AddedBy.ACCOUNT_DELETED`).
- The aggregate refuses a change (`requireChangeableBy(actor)`; releases inside `editRelease`/`removeRelease`; categories through `Category.requireKeptBy`): a public visitor with `401 auth.required`, a signed-in user with `403 access.denied` (`ChangeForbiddenException`).
- The frontend shows both inside the form or the delete dialog, by the `401` and `403` texts of the slice.
- Reads tell the reader what they may do, by the same domain rule (`AddedBy.mayBeChangedBy`): the details of software, people and groups and every release carry `canChange`, computed by the query handler for the `Actor` of the request; who added an entry never leaves the backend. `GET /api/auth/me` carries `abilities`: `add-entries` for every signed-in user, `keep-categories` for administrators, `use-assistant` for the members of the AI assistant group.
- The frontend shows only the buttons these allow: editing by `canChange`, adding by `useAbility('add-entries')`, the categories' adding and editing by `useAbility('keep-categories')`.

### REST

| Endpoint | Body | Answer |
|---|---|---|
| `POST /api/auth/register` | `name`, `email`, `password`, `language` | `204`; `422 user.name-taken` / `user.email-taken` on the field |
| `POST /api/auth/confirm` | `token` | `200 {accessToken}` with refresh cookie; `422 user.confirmation-invalid` / `user.confirmation-expired` |
| `POST /api/auth/resend-confirmation` | `login`, `password`, `language` | `204`; `422 user.credentials-invalid` / `user.already-confirmed` |
| `POST /api/auth/login` | `login` (name or email), `password`, `remember` | `200 {accessToken}` with refresh cookie; `422 user.credentials-invalid` / `user.unconfirmed` |
| `POST /api/auth/refresh` | refresh cookie | `200 {accessToken}` with rotated refresh cookie; `401 auth.session-expired` |
| `POST /api/auth/logout` | refresh cookie | `204`, cookie cleared, also without a session |
| `GET /api/auth/me` | Bearer access token | `{id, name, email, groups, abilities}`; `204` for a visitor; `401 auth.required` when the browser holds a refresh cookie, so the session is to be restored first |

Wrong credentials are `422`, not `401`: they are a mistake in the form, shown in it, while `401` tells the frontend the session needs refreshing.

### Configuration

`auth.*` in `application.yml`: the JWT secret (production sets its own, at least 32 bytes), the access token lifetime, the public URL of the frontend that links lead to, the sender of letters, secure cookies. Mail goes through `spring.mail`; locally Mailpit catches it ([local-dev.md](../local-dev.md)), in production an SMTP relay ([deploy.md](../deploy.md#mail)).

### First administrator

`create-admin` as the argument of the backend (`docker compose ... run --rm backend create-admin`, [README](../README.md#first-start)) starts the application as a command: `ArchiveCatalogApplication` adds the options of a command — no web server, no search worker, warnings only in the log — and `CreateAdminRunner` asks the account in the terminal (`CreateAdminCommandLine`), passwords without echo, shows the user name and email as they will be kept and creates the account only once that is confirmed, and exits with its code before the tasks of a started archive (the search rebuild, stopping unfinished imports) run. Flyway migrates the database first, so it works on an empty one. A terminal of a container treats every byte as a character, so Backspace over a non-Latin letter left broken bytes in the name; the image's `docker-entrypoint.sh` switches the terminal to UTF-8 (`stty iutf8`). `Registrations.registerAdministrator` refuses with `user.admin-exists` once there is an administrator; a refusal or a wrong value is printed and the command exits with 1.

### Fixtures

`R__0_users.sql`, applied before the other fixtures, which are all added by `Admin`: `Admin`, `Moderator`, `Member` (each `name@archive.test`), `Pending` waiting for its link (`/verify-email?token=pending-fixture-token`) and `Abandoned`, whose link expired. Every password is `password1`.

## Frontend

- `entities/users`: the account format rules (the same the backend checks), `CurrentUserProvider` (mounted by `App`), `useCurrentUser()` — `{user, known}`, read from the session of the browser — and `useAbility(ability)`.
- `features/user/sign-in` — `AccountMenu`, the person button of the header (`MainMenu`) with its `IconPopover` window: for a visitor the sign-in form (user name or email, password, "Remember me", checked by default) and a link to registration; for a signed-in user their name and Sign out. An unconfirmed account gets "Send the link again", sent with what was typed. The window closes once the visitor goes anywhere.
- `features/user/register` — `RegisterForm` (user name, email, password, repeated password) at `/register` (`pages/account/register`); once sent it tells where the link went. The letter goes in the interface language.
- `features/user/confirm-email` — `ConfirmEmail` at `/verify-email?token=…` (`pages/account/verify-email`): sends the token once on opening and tells the outcome: confirmed (and signed in), expired (register again or sign in to get a new link) or not working.

### The session of the browser

The frontend sees only the short-lived access token and keeps it in module memory, never persistent browser storage. The refresh secret remains in its `HttpOnly` cookie. One module, `shared/api/session`, owns both the access token and what the browser is — not known yet, a visitor or a signed-in user — and only it changes them.

- Everything that changes the session runs one after another: asking who is signed in, signing in, confirming, refreshing, signing out. Each sets the session once, when it is done, so an answer of an earlier session never lands after a later one; signing in and confirming ask who signed in as their last step.
- API requests send the in-memory access token as Bearer. The refresh request uses the refresh cookie alone so that an expired access token cannot block its own replacement. A request answered `401` (other than the calls that make or end sessions) waits its turn for a refresh, shared by all requests refused meanwhile in the tab, and is repeated once with the replacement token. When the server says the session has expired, the browser becomes a visitor and the request fails with `AuthenticationRequiredError`.
- The session is refreshed only when a request needs it. A refreshed access token is sent to the other open tabs through the channel below, so an inactive tab does not need its own refresh when it next makes a request.
- Who is signed in is asked once per page load, before the first route preloads (`main.tsx`). Every request waits for that answer, so it goes out with the session the answer refreshed and reads what the user may see; it goes out also when the question failed.
- What the page shows depends on who reads it: once the session changes after it was known (signing in or out, here or in another tab, a session that ends), `CurrentUserProvider` has every query read again. The same user with a new access token — a newly opened tab restoring its session through the refresh cookie — is no change: the tab keeps its session and only takes the token, so the open pages do not reload.
- Every change is told to the other tabs of the browser (`BroadcastChannel`), including the in-memory access token: signing in, refreshing or signing out in one tab takes effect in all.
- A failed refresh does not clear the refresh cookie. Only signing out clears it.
