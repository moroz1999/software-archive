# Software Editing

Adding, editing and deleting software ([domain/software.md](../domain/software.md)). Minimal for now: title, description and release date.

## Pages

| Page | Path | Content |
|---|---|---|
| Software list | `/` | "Add software" next to the page title; a card's title leads to the software's page ([software-view.md](software-view.md)), whose "Edit" leads to the editing |
| Add software | `/software/new` | Empty form, "Add software" submits; back to the list after adding |
| Edit software | `/software/{id}/edit` | Form with the software's values, "Save" and "Delete"; back to the software's page after saving, to the list after deleting |

## Form

- Fields: Title (required, at most 255 characters), Description (multi-line), Release date (a full day).
- Format rules are checked before sending; errors appear under their fields on submit and clear when the field is edited. Empty optional fields are sent as unknown (`null`).
- While saving, the fields and the submit button are locked.

## Deleting

"Delete" opens a confirmation naming the software; "Delete for good" deletes it with everything that belongs to it, Cancel or Escape closes the dialog. Both buttons are locked while the deletion runs.

## Loading and errors

- The edit page preloads the software's details ([data-loading.md](../data-loading.md)): within 300 ms the form appears filled, otherwise a form skeleton first.
- Unknown software, an id in the URL that cannot be one, and software deleted in the meantime — found out on saving or deleting — show the not found page ([error-handling.md](../error-handling.md)); a malformed id is never sent to the backend.
- System errors go to the error dialog; its Retry repeats the same save or delete with the same values, and the form keeps what was typed.

## REST

| Call | Answer | Errors |
|---|---|---|
| `GET /api/software/{id}` | `200` `{id, title, description, releaseDate}` | `404 software.not-found`; `400` for a malformed id |
| `POST /api/software` `{title, description, releaseDate}` | `201`, `Location: /api/software/{id}`, `{id}` | `400 title.required`, `400 title.too-long`, `400` for malformed JSON |
| `PUT /api/software/{id}` `{title, description, releaseDate}` | `204` | `404 software.not-found`; the `400`s of `POST` |
| `DELETE /api/software/{id}` | `204` | `404 software.not-found` |

`description` and `releaseDate` are `null` when unknown; `releaseDate` is an ISO day.
