# Group Editing

Adding, editing and deleting groups: teams with a name of their own, their types if known, their labels, locations and members ([domain/authors.md](../domain/authors.md#group)). Member groups are not part of this form.

## Pages

| Page | Path | Content |
|---|---|---|
| Groups | `/groups` | "Add group" next to the page title ([group-catalog.md](group-catalog.md)); a group's page ([group-view.md](group-view.md)) has "Edit" next to its name |
| Add group | `/groups/new` | Empty form; Cancel returns to the catalog, "Add group" submits and opens the group's page |
| Edit group | `/groups/{id}/edit` | "Delete" next to the page title; the form with the group's values, Cancel and "Save" back to the group's page; the catalog after deleting |

## Form

- In one narrow column, in sections: Main (Name, required, at most 255 characters; Types, required; Notes, multi-line), Names (Labels), Geography (Locations), Members; Cancel and the submit button at its bottom right — the same layout as the author form ([author-editing.md](author-editing.md)).
- Types: a checkbox for every type of the reference book, under its code; none chosen is a group of no known type.
- Labels and Locations: the same editors as for authors, but the labels have no "Display name" — a group is always shown under its name; exactly one primary location; a label taken away stays in the archive as a label of nobody.
- Members: people and their aliases are found remotely by name; every selected member has removable role chips and optional start and end days. The form replaces the complete roster.
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
| `POST /api/groups` with the form values and `members: [{id, type, roles, startDate, endDate}]` | `201`, `Location: /api/groups/{id}`, `{id}` | Format errors; `422 group.member-unknown` under Members; the existing type, label and location refusals |
| `PUT /api/groups/{id}` (same body) | `204` | `404 group.not-found`; the errors of `POST` |
| `DELETE /api/groups/{id}` | `204` | `404 group.not-found` |

`types`, `labels` (`{id, name}`, without `display`) and `locations` are the group's whole lists, with the same meaning as for authors; missing lists mean none, `notes` is `null` when unknown. Records from before the rule are read as for authors.
