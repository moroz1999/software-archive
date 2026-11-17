# User Management

The Users section of management at `/management/users`, with the editing of a user at `/management/users/{id}/edit` ([management.md](management.md)), for administrators only. The rules behind it: [domain/users.md](../domain/users.md#managing-users).

## Backend

The `user` module.

- `UserEditing` changes a user for an administrator: it refuses anyone else (`Actor.mayManageUsers`), keeps the last administrator one by asking the repository whether another account is (`UserRepository.hasAdminBesides`), and checks the name and the email through `AccountNames`, which registration uses too: the account's own name and email are no conflict. `User.changeGroups` keeps every confirmed user a member and refuses groups for an unconfirmed account.
- `GrantedGroups` holds the groups given on purpose: administrator and moderator only.
- The list and a single account are read with plain SQL (`JdbcUserReadRepository.findPage`, `findEntry`): every account, ordered by its user name whatever the letter case; a search matches a part of the user name or the email whatever the case, as text, not as a pattern.
- A user's access token carries the groups they had when it was issued: new groups reach their requests at the next refresh of their session.

### REST

| Call | Answer | Errors |
|---|---|---|
| `GET /api/users?query=&page=&size=` | page of `{id, name, email, confirmed, registeredAt, groups}` | `401 auth.required`, `403 access.denied` |
| `GET /api/users/{id}` | `{id, name, email, confirmed, registeredAt, groups}` | `401 auth.required`, `403 access.denied`, `404 user.not-found` |
| `PUT /api/users/{id}` | body `{name, email, groups}`: groups as codes of `admin`, `moderator`; `204` | `400` for the format of the name and the email, `user.group-invalid` / `user.groups-required`; `401 auth.required`; `403 access.denied`; `404 user.not-found`; `422 user.name-taken` / `user.email-taken` on the field, `user.last-admin` / `user.unconfirmed-groups` |

## Frontend

- `features/user/user-list` — `UserListProvider` reads the search and the page from the address (`?query=&page=`, pages from 1) like the author catalog does ([author-catalog.md](author-catalog.md)); `UserList` shows the search field, the table and its pages, and takes the actions of a row from its page.
- `pages/management/users` — the page, with the management tabs; its loader preloads the list of the address. Every row ends with "Edit", leading to the editing of the user.
- `features/user/user-edit` — `UserDetailsProvider` loads the user, `EditUserProvider` holds the form and saves it, `EditUserForm` shows it: the account section (user name, email, checked by the format rules of registration) and the groups section with a box for each group given on purpose; an unconfirmed account gets a note instead of the boxes and is saved with no groups. A taken name or email shows under its field until edited; the other refusals at the top of the form. Once saved, the lists of users go stale.
- `pages/management/user-edit` — the page, with the management tabs; its loader preloads the user, a slow one shows the form skeleton. Cancel and Save return to the list; an unknown user or an id that cannot be one is the not found page.
