# Label View

The page of a label at `/labels/{id}` ([domain/authors.md](../domain/authors.md#label-page)); a label of nobody's entry in the author catalog, its credits on software and its memberships on group pages lead here, and so do the labels on the pages of people and groups.

## Page

- The label's name is the page heading; under it "Whose label": a person by the real name with the main name muted next to it (by the main name without a real name), a group by its name, leading to their page, or "Not known yet whose label this is" for a label of nobody; then Member of: the groups it was a member of, each leading to its page ([group-view.md](group-view.md)).
- Software below: the software it is credited on, as on an author's page ([author-view.md](author-view.md)).

## Attaching to the owner

- "Say whose label" next to the heading, for administrators only, opens a dialog: "Whose label is “{name}”?", "Person or group" searching people and groups by name, and "Display name", unchecked, which is not offered once a group is chosen; "Save" is locked until an owner is chosen. The wording never calls anyone an owner: it asks whose the label is.
- After attaching, the page of the owner opens; the owner's names, the catalogs and the lists of software read again.
- Refusals show inside the dialog: the label got an owner meanwhile, the owner is gone, a person without labels and the label not marked as the display name, a missing sign-in, no right to change the owner. A label deleted meanwhile shows the not found page; system errors go to the error dialog ([error-handling.md](../error-handling.md)).

## Making a person or a group

- "Make author" and "Make group" next to the heading, for whoever may take the label: any signed-in user on a label of nobody, whoever may change its owner otherwise.
- "Make author" asks "Make a person known as “{name}”?"; "Make group" asks "Make a group named “{name}”?" with the group types to choose, its button locked until one is chosen. The name is the label's, not editable. A label with an owner adds "It will no longer be a label of {owner}."
- Making a person replaces the label: the person gets its name, external origin, software credits and group memberships directly, and the label no longer exists. Making a group keeps the label as one of the new group's names.
- After making, the page of the new person or group opens; the old owner, the catalogs and the lists of software read again.
- Refusals show inside the dialog: the label being the display name of a person with other labels, or the only name of a person without a real name; a type gone from the archive; no right to change the owner; a missing sign-in. System errors go to the error dialog.

## Loading and errors

- The page preloads the label's details ([data-loading.md](../data-loading.md)): within the window it appears filled, otherwise with a skeleton. The software is asked for once its block comes into view.
- An unknown label, and an id that cannot be one, show the not found page; a malformed id is never sent.

## REST

| Call | Answer | Errors |
|---|---|---|
| `GET /api/labels/{id}` | `{id, name, owner: {type, id, name, note} \| null, groups: [{id, name}], canChange, canMakeOwner}`; `type` `author` or `group`; `owner.name` the person's real name, else their main name, or the group's name; `owner.note` the person's main name next to a real name, else `null`; group names are main names; `canChange` for an administrator on a label of nobody; `canMakeOwner` for a signed-in user on a label of nobody, or one who may change its owner | `404 label.not-found`; `400` for a malformed id |
| `GET /api/labels/{id}/software` | as `GET /api/authors/{id}/software`, every credit by the label itself | `400` for a malformed id |
| `POST /api/labels/{id}/owner` `{type, id, display}` | `204` | `404 label.not-found`; `409 label.owned`; `422 label.owner-unknown` (field `owner`), `422 label.display-one` (a person only; `display` is ignored for a group); `401`, `403`; `400` for another `type` or a missing `id` |
| `POST /api/labels/{id}/author` | `201 {id}` of the new person | `404 label.not-found`; `422 label.display-one`, `422 author.nameless` (taking it from a person); `401`, `403`; `400` for a malformed id |
| `POST /api/labels/{id}/group` `{types}` | `201 {id}` of the new group | as `/author`; `422 group.types-required`, `422 group.type-unknown` |
