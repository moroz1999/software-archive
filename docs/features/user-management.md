# User Management

The Users section of management at `/management/users` ([management.md](management.md)), for administrators only. The rules behind it: [domain/users.md](../domain/users.md#managing-users).

## Backend

The `user` module.

- `GroupAssignment` gives a user their groups for an administrator: it refuses anyone else (`Actor.mayManageUsers`), and keeps the last administrator one by asking the repository whether another account is (`UserRepository.hasAdminBesides`). `User.changeGroups` keeps every confirmed user a member and refuses an unconfirmed account.
- `GrantedGroups` holds the groups given on purpose: administrator and moderator only.
- The list is read with plain SQL (`JdbcUserReadRepository.findPage`): every account, ordered by its user name whatever the letter case; a search matches a part of the user name or the email whatever the case, as text, not as a pattern.
- A user's access token carries the groups they had when it was issued: new groups reach their requests at the next refresh of their session.

### REST

| Call | Answer | Errors |
|---|---|---|
| `GET /api/users?query=&page=&size=` | page of `{id, name, email, confirmed, registeredAt, groups}` | `401 auth.required`, `403 access.denied` |
| `PUT /api/users/{id}/groups` | body `{groups}`: codes of `admin`, `moderator`; `204` | `400 user.group-invalid` / `user.groups-required`; `401 auth.required`; `403 access.denied`; `404 user.not-found`; `422 user.last-admin` / `user.unconfirmed-groups` |

## Frontend

- `features/user/user-list` — `UserListProvider` reads the search and the page from the address (`?query=&page=`, pages from 1) like the author catalog does ([author-catalog.md](author-catalog.md)); `UserList` shows the search field, the table and its pages, and takes the actions of a row from its page.
- `features/user/change-groups` — `ChangeUserGroupsButton`, in the row of every confirmed user: a dialog with a box for each group given on purpose. Its business errors show inside the dialog; once saved, the lists of users are read again.
- `pages/management/users` — the page, with the management tabs; its loader preloads the list of the address.
