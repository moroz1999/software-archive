# Software Editing

Adding, editing and deleting software ([domain/software.md](../domain/software.md)). Title, platform, description, release date, base hardware and categories; releases are edited on their own pages ([release-editing.md](release-editing.md)).

## Pages

| Page | Path | Content |
|---|---|---|
| Software list | `/software` | "Add software" next to the page title; a card's title leads to the software's page ([software-view.md](software-view.md)), whose "Edit" leads to the editing |
| Add software | `/software/new` | Empty form, "Add software" submits; back to the list after adding |
| Edit software | `/software/{id}/edit` | Form with the software's values, "Save" and "Delete"; back to the software's page after saving, to the list after deleting; the software's screenshots below the form ([screenshots.md](screenshots.md#pages)) |

## Form

- Fields: Title (required, at most 255 characters), Platform (required, chosen from the archive's platforms; a new form starts with the platform of the switch, [platform-switch.md](platform-switch.md)), Description (multi-line), Release date (a year or a day, one text field with a hint: `1987` or `1987-05-12`).
- Hardware: once a platform is chosen, its hardware by group, picked with the same picker as a release's ([design-system/multi-select-filter.md](../design-system/multi-select-filter.md)); a skeleton while it loads.
- Categories: under the hardware, the whole tree of categories ([domain/categories.md](../domain/categories.md)) to check any number in, none included ([design-system/tree-checklist.md](../design-system/tree-checklist.md)); a skeleton while it loads.
- Publishers and developer companies are remote multi-selects: typing searches the current people, groups and aliases, and each choice stays as a removable chip. Publishers offer people and groups; developer companies offer groups only. Choosing an alias preserves that exact alias rather than changing it to its owner's display name.
- Credits find people and their aliases remotely by name; every selected person has removable role chips. Saving replaces the complete set of credits.
- While hardware is chosen the platform is locked, with "Remove the hardware to change the platform." under it: hardware belongs to its platform ([domain/hardware.md](../domain/hardware.md#platform-switch)). Hardware of a release still locks it on the backend, shown under Platform.
- Format rules are checked before sending; errors appear under their fields on submit and clear when the field is edited. Empty optional fields are sent as unknown (`null`).
- While saving, the fields and the submit button are locked.

## Deleting

"Delete" opens a confirmation naming the software; "Delete for good" deletes it with everything that belongs to it, Cancel or Escape closes the dialog. Both buttons are locked while the deletion runs.

## Loading and errors

- The edit page preloads the software's details, then the hardware of its platform ([data-loading.md](../data-loading.md)): within 300 ms the form appears filled, otherwise a form skeleton first.
- Hardware of another platform is refused under Hardware; a category, publisher or developer company deleted in the meantime is refused under its field.
- Unknown software, an id in the URL that cannot be one, and software deleted in the meantime — found out on saving or deleting — show the not found page ([error-handling.md](../error-handling.md)); a malformed id is never sent to the backend.
- System errors go to the error dialog; its Retry repeats the same save or delete with the same values, and the form keeps what was typed.

## REST

| Call | Answer | Errors |
|---|---|---|
| `GET /api/software/{id}` | `200` software details, including publisher/developer choices credits as `{id, type, title, roles}` and `sources: [{archive, url}]`, the pages of the archives it was imported from | `404 software.not-found`; `400` for a malformed id |
| `POST /api/software` with the form values; credits are `{id, type, roles}` | `201`, `Location: /api/software/{id}`, `{id}` | Format errors; `422` for an unknown hardware, category, publisher, developer company or credited person under its field |
| `PUT /api/software/{id}` (same body) | `204` | `404 software.not-found`; `422 software.platform-locked` (field `platform`) when the platform changes while a release has hardware; the errors of `POST` |
| `DELETE /api/software/{id}` | `204` | `404 software.not-found` |
| `GET /api/entity-lookup?types=…&query=…` | `200` `{items: [{id, type, title}]}` ordered by the closest names first, empty for a blank query | `400 software.reference-type-invalid` for an unknown type |

`platform` is a platform code; `description` and `releaseDate` are `null` when unknown; `releaseDate` is a year (`1987`) or an ISO day (`1987-05-12`), anything else is `400 release-date.invalid`; `hardware` is the software's whole base hardware, every code of its platform (missing means none); `categories`, `publishers`, `developers` and `credits` are whole sets (missing means none). Credit types are `author` and `author-label`. A category sent together with one under it is dropped, so the software keeps only the deepest category of each branch. Publisher types are `author`, `author-label`, `group` and `group-label`; developer types are `group` and `group-label`.

The categories are stored by their link to the software. Saving looks up the branch of every chosen category and locks it for sharing, so none of it can be deleted or moved before the software is stored; loading software for a change looks the branches up again, so a category the tree has meanwhile put above another of the software's leaves on the next save.
