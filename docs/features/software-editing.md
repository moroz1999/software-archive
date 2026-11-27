# Software Editing

Adding, editing and deleting software ([domain/software.md](../domain/software.md)). Title, platform, description, instructions, release date, preservation status, base hardware and categories; releases are edited on their own pages ([release-editing.md](release-editing.md)).

## Pages

| Page | Path | Content |
|---|---|---|
| Software list | `/software` | "Add software" next to the page title; a card leads to the software's page ([software-view.md](software-view.md)), whose menu right after the title has "Edit", leading to the editing |
| Add software | `/software/new` | Empty form; Cancel returns to the list, "Add software" submits and returns to it |
| Edit software | `/software/{id}/edit` | "Delete" next to the page title; the form with the software's values, Cancel and "Save" back to the software's page; back to the software's page after saving, to the list after deleting; the software's screenshots, maps, covers and adverts below the form ([screenshots.md](screenshots.md#pages)) |

The edit page shows the breadcrumbs of the software ([software-view.md](software-view.md#page)), the software leading to its page, then "Editing". Adding pages have none.

## Form

- Layout: a two-column form ([design-system/form.md](../design-system/form.md)) in sections — Main (Title, Platform, Release date, Description, Instructions), Classification (Hardware, Categories), Credits, and on editing Sources; Cancel and the submit button at its bottom right.
- Sources, on editing: for every import the entry came from, the source archive and the id there as text — the name it is known by there, when the source gives it no id ([domain/imports.md](../domain/imports.md#origin)) — the id leading to the page there when it has one; "None" when no import brought it. Not editable (`OriginList`).
- Fields: Title (required, at most 255 characters), Platform (required, chosen from the archive's platforms; a new form starts with the platform of the switch, [platform-switch.md](platform-switch.md)), Description (multi-line), Instructions (multi-line, how it is loaded, run and played; imports never fill it), Release date (a year or a day, one text field with a hint: `1987` or `12.05.1987`; the API keeps the ISO day).
- Hardware: once a platform is chosen, its hardware by group, picked with the same picker as a release's ([design-system/multi-select-filter.md](../design-system/multi-select-filter.md)); a skeleton while it loads.
- Categories: under the hardware, the whole tree of categories ([domain/categories.md](../domain/categories.md)) to check any number in, none included ([design-system/tree-checklist.md](../design-system/tree-checklist.md)); a skeleton while it loads.
- Credits: "Person or group" searches the current people, groups, their labels and the labels of nobody remotely by name; every one chosen has removable role chips, Developer and Publisher first among the roles. Choosing a label keeps that exact label rather than changing it to its owner's display name. Saving replaces the complete set of credits.
- "Compilation programs" looks up programs by title and shows removable selections. A submitted selection replaces the contents. An edit without a selection change sends no contents, so it can be saved alongside another editor's changes to the compilation.
- While hardware is chosen the platform is locked, with "Remove the hardware to change the platform." under it: hardware belongs to its platform ([domain/hardware.md](../domain/hardware.md#platform-switch)). Hardware of a release still locks it on the backend, shown under Platform.
- Format rules are checked before sending; errors appear under their fields on submit and clear when the field is edited. Empty optional fields are sent as unknown (`null`).
- While saving, the fields and the submit button are locked.

## Deleting

"Delete" opens a confirmation naming the software; "Delete for good" deletes it with everything that belongs to it, Cancel or Escape closes the dialog. Both buttons are locked while the deletion runs.

## Loading and errors

- The edit page preloads the software's details, then the hardware of its platform ([data-loading.md](../data-loading.md)): within 300 ms the form appears filled, otherwise a form skeleton first.
- Hardware of another platform is refused under Hardware; a category deleted in the meantime is refused under its field, anyone credited deleted in the meantime under Credits.
- A deleted program or a selection that would create a cycle is refused under "Compilation programs".
- Unknown software, an id in the URL that cannot be one, and software deleted in the meantime — found out on saving or deleting — show the not found page ([error-handling.md](../error-handling.md)); a malformed id is never sent to the backend.
- System errors go to the error dialog; its Retry repeats the same save or delete with the same values, and the form keeps what was typed.

## REST

| Call | Answer | Errors |
|---|---|---|
| `GET /api/software/{id}` | `200` software details, including credits with the nullable owner id used by the view, `instructions`, the `preservation` status code, `links: [{url, type}]` for saved external links, `sources` for pages from import origins and `origins: [{origin, externalId, url}]` for the imports themselves. An origin's `url` is `null` if it has no page. `added: {by, at}` tells who added the entry and when. `by` is `null` once the user's account is gone. | `404 software.not-found`, `400` for a malformed id |
| `POST /api/software` with the form values; credits are `{id, type, roles}`, the type `author`, `author-label`, `group` or `group-label` | `201`, `Location: /api/software/{id}`, `{id}` | Format errors (`400 software.reference-type-invalid`, `software.reference-id-required`, `software.credit-role-invalid`, `software.credit-duplicate`); `422` for an unknown hardware or category, `422 software.credit-unknown` (field `credits`) for anyone credited not in the archive |
| `PUT /api/software/{id}` (same body) | `204` | `404 software.not-found`; `422 software.platform-locked` (field `platform`) when the platform changes while a release has hardware; the errors of `POST` |
| `DELETE /api/software/{id}` | `204` | `404 software.not-found` |
| `GET /api/entity-lookup?types=…&query=…` | `200` `{items: [{id, ownerId, type, title}]}` ordered by the closest names first, empty for a blank query; a label of nobody is an `author-label` with `ownerId` `null` | `400 software.reference-type-invalid` for an unknown type |
| `GET /api/software/lookup?query=…` | `200` with program ids and titles in `items`, empty for a blank query | |

Software details carry `compilationItems` and `compilations`. A create or edit may send `compilationItemIds`, including an empty list to clear the contents. A missing program or a cycle returns `422` for that field. Edits that add links serialize the cycle check with the write so concurrent edits cannot close a cycle.

`platform` is a platform code; `description`, `instructions` and `releaseDate` are `null` when unknown; `releaseDate` is a year (`1987`) or an ISO day (`1987-05-12`), anything else is `400 release-date.invalid`; `preservation` is `unknown`, `missing-in-action`, `donationware` or `in-sales` (missing means `unknown`), anything else is `400 software.preservation-invalid`; `hardware` is the software's whole base hardware, every code of its platform (missing means none); `categories`, `publishers`, `developers` and `credits` are whole sets (missing means none). Credit types are `author` and `author-label`. A category sent together with one under it is dropped, so the software keeps only the deepest category of each branch. Publisher types are `author`, `author-label`, `group` and `group-label`; developer types are `group` and `group-label`.

The categories are stored by their link to the software. Saving looks up the branch of every chosen category and locks it for sharing, so none of it can be deleted or moved before the software is stored; loading software for a change looks the branches up again, so a category the tree has meanwhile put above another of the software's leaves on the next save.
