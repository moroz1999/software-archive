# Group Catalog

The catalog at `/groups` ([domain/authors.md](../domain/authors.md#group-catalog)). It is built and behaves like the author catalog ([author-catalog.md](author-catalog.md)) — letters, place filters, pages, the address, loading and errors — and shares its parts from `entities/labels` and `entities/locations`.

## Page

- An entry is the name leading to the group's page ([group-view.md](group-view.md)), with the group's name muted on the same line unless the entry is that name.
- "No groups found." when nothing matches.

## REST

| Call | Answer | Errors |
|---|---|---|
| `GET /api/groups?letter=&country=&city=&page=&size=` | page of `{groupId, name, groupName}`; `groupName` `null` when the entry is the group's name | `400 label.letter-invalid`; `400` for a malformed id |
