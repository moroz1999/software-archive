# Label View

The page of a label of nobody at `/labels/{id}` ([domain/authors.md](../domain/authors.md#label-page)); its entry in the author catalog, its credits on software and its memberships on group pages lead here.

## Page

- The label's name is the page heading, "Owner unknown" under it, then Member of: the groups it was a member of, each leading to its page ([group-view.md](group-view.md)).
- Software below: the software it is credited on, as on an author's page ([author-view.md](author-view.md)).
- A label with an owner goes over to the page of the owner, replacing the address.

## Attaching to the owner

- "Attach to owner" next to the heading, for administrators only, opens a dialog: "Whose label is “{name}”?", "Person or group" searching people and groups by name, and "Display name", unchecked; "Attach" is locked until an owner is chosen.
- After attaching, the page of the owner opens; the owner's names, the catalogs and the lists of software read again.
- Refusals show inside the dialog: the label got an owner meanwhile, the owner is gone, the owner has no labels and the label is not marked as the display name, a missing sign-in, no right to change the owner. A label deleted meanwhile shows the not found page; system errors go to the error dialog ([error-handling.md](../error-handling.md)).

## Loading and errors

- The page preloads the label's details ([data-loading.md](../data-loading.md)): within the window it appears filled, otherwise with a skeleton. The software is asked for once its block comes into view.
- An unknown label, and an id that cannot be one, show the not found page; a malformed id is never sent.

## REST

| Call | Answer | Errors |
|---|---|---|
| `GET /api/labels/{id}` | `{id, name, owner: {type, id} \| null, groups: [{id, name}], canChange}`; `type` `author` or `group`; group names are main names; `canChange` for an administrator on a label of nobody | `404 label.not-found`; `400` for a malformed id |
| `GET /api/labels/{id}/software` | as `GET /api/authors/{id}/software`, every credit by the label itself | `400` for a malformed id |
| `POST /api/labels/{id}/owner` `{type, id, display}` | `204` | `404 label.not-found`; `409 label.owned`; `422 label.owner-unknown` (field `owner`), `422 label.display-one`; `401`, `403`; `400` for another `type` or a missing `id` |
