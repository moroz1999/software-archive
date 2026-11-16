# Group Catalog

The catalog at `/groups` ([domain/authors.md](../domain/authors.md#group-catalog)). It is built and behaves like the author catalog ([author-catalog.md](author-catalog.md)) — letters, place filters, pages, the address, loading and errors — follows the platform switch through credits the same way, and shares its parts from `entities/labels` and `entities/locations`.

## Page

- "Add group" next to the page title leads to adding a group ([group-editing.md](group-editing.md)).
- A group's entry is its name leading to the group's page ([group-view.md](group-view.md)); a label's entry leads to its own page ([label-view.md](label-view.md)), with the group's name muted on the same line.
- "No groups found." when nothing matches.

## REST

| Call | Answer | Errors |
|---|---|---|
| `GET /api/groups?letter=&country=&city=&platform=&page=&size=` | page of `{groupId, labelId, name, note}`, exactly one of `groupId` (a group) and `labelId` (a label); `note` is the group's name next to a label, `null` for a group | `400 label.letter-invalid`, `400 platform.code-invalid`; `400` for a malformed id |
