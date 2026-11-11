# Group Editing

Adding, editing and deleting groups: teams with a name of their own, at least one type, their labels and locations ([domain/authors.md](../domain/authors.md#group)). Members and member groups are not part of this form.

## Pages

| Page | Path | Content |
|---|---|---|
| Groups | `/groups` | "Add group" next to the page title ([group-catalog.md](group-catalog.md)); a group's page ([group-view.md](group-view.md)) has "Edit" next to the main name |
| Add group | `/groups/new` | Empty form; Cancel returns to the catalog, "Add group" submits and opens the group's page |
| Edit group | `/groups/{id}/edit` | "Delete" next to the page title; the form with the group's values, Cancel and "Save" back to the group's page; the catalog after deleting |

## Form

- Name (required, at most 255 characters), Types, Labels, Locations, Notes (multi-line), in one narrow column; Cancel and the submit button at its bottom right — the same layout as the author form ([author-editing.md](author-editing.md)).
- Types: a checkbox for every type of the reference book, under its code; at least one is needed, "Choose at least one type." under Types on submit.
- Labels and Locations: the same editors and rules as for authors — exactly one display name among the labels and exactly one primary location, a label taken away stays in the archive as a label of nobody. A group may have no labels: it is then shown under its name.
- Format rules are checked before sending; errors appear under their fields on submit and clear when the field is edited. Empty notes are sent as unknown (`null`).
- While saving, the fields and the buttons are locked.

## Deleting

"Delete" opens a confirmation naming the group and saying its labels stay in the archive as labels of nobody, with their credits; "Delete for good" deletes the group with its types, locations and memberships, Cancel or Escape closes the dialog. A refusal shows inside the dialog.

## Loading and errors

- The edit page preloads the group's details, the types and the countries ([data-loading.md](../data-loading.md)); the add page preloads the types and the countries. Within 300 ms the form appears filled, otherwise a form skeleton first.
- A type that is not in the reference book is refused under Types ("One of the types is not in the archive any more. Reload and try again."); labels and locations are refused under their editors as for authors; a refusal of the whole form shows at its top.
- An unknown group, an id in the URL that cannot be one, and a group deleted in the meantime show the not found page ([error-handling.md](../error-handling.md)).
- System errors go to the error dialog; the form keeps what was typed.

## REST

| Call | Answer | Errors |
|---|---|---|
| `GET /api/group-types` | `200`, the codes of the reference book by code | — |
| `POST /api/groups` `{name, notes, types: [code], labels: [{id, name, display}], locations: [{country, city, primary}]}` | `201`, `Location: /api/groups/{id}`, `{id}` | `400 title.required` / `400 title.too-long` for the name or a label name, `400 group.type-code-blank`, `400 location.empty`, `400` for malformed JSON or ids; `422 group.types-required`, `422 group.type-unknown` (field `types`), `422 label.not-own`, `422 label.display-one` (field `labels`), `422 location.unknown`, `422 location.primary-one` (field `locations`) |
| `PUT /api/groups/{id}` (same body) | `204` | `404 group.not-found`; the errors of `POST` |
| `DELETE /api/groups/{id}` | `204` | `404 group.not-found` |

`types`, `labels` and `locations` are the group's whole lists, with the same meaning as for authors; missing lists mean none, `notes` is `null` when unknown. Records from before the rule are read as for authors.
