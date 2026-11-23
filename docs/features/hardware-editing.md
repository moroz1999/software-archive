# Hardware editing

Keeping the hardware reference book and its groups from the management ([domain/hardware.md](../domain/hardware.md#keeping-the-reference-book)).

## Pages

| Page | Path | Content |
|---|---|---|
| Hardware | `/management/hardware` | "Add hardware" and "Add group" next to the title; the management tabs; every group in its order as a panel, its name leading to its editing, its hardware by name under it, each leading to its editing; "No hardware in the group yet." for an empty one |
| Add group, Add hardware | `/management/hardware/groups/new`, `/management/hardware/new` | Management tabs and an empty form; Cancel and the submit button both return to the list |
| Edit group, Edit hardware | `/management/hardware/groups/{code}/edit`, `/management/hardware/{code}/edit` | Management tabs; "Delete" next to the title; the form with Cancel and "Save" back to the list; the list after deleting |

## Form

- Code (required, locked when editing: it never changes), Name in Russian (required), Name in English (optional); hardware also has Group, chosen from the groups in their order, and Platforms, a checkbox per platform.
- Format rules are checked before sending: a code of lower-case Latin letters and digits, words joined by a hyphen; the Russian name; every name at most 255 characters; hardware's group and at least one platform. An error shows under its field on submit and clears once the form is edited.
- Refusals show under their field and clear once it is edited: a code taken ("The code is taken already."), a group or a platform gone in the meantime ("… is not in the archive any more. Reload and try again."), a platform taken away while software of it has the hardware ("Software of a platform taken away has this hardware: keep that platform.").

## Deleting

"Delete" asks "Delete “{name}” from the archive?"; a group with hardware ("The group has hardware in it: it cannot be deleted.") and hardware of software or releases ("Software or releases have this hardware: it cannot be deleted.") are refused inside the dialog.

## Loading and errors

- The list page preloads the groups with their hardware; the page of adding hardware preloads the groups and the platforms; an edit page preloads its group or hardware, and hardware's page the groups and the platforms too ([data-loading.md](../data-loading.md)). A late answer shows a skeleton first.
- An unknown group or hardware, and a code in the URL that cannot be one, show the not found page; system errors go to the error dialog.
- A change makes every cached query stale, since the names of hardware and groups show on software pages and in the catalog.

## REST

Names are by language code, the Russian one always there; a name and a group name in a list are in the language of `Accept-Language`, else in Russian. Every change is for administrators only: `401`/`403` for anyone else.

| Call | Answer | Errors |
|---|---|---|
| `GET /api/hardware-groups` | `200`, `[{code, name, hardware: [{code, name}]}]`, groups in their order, hardware by name | |
| `GET /api/hardware-groups/{code}` | `200`, `{code, names}` | `404 hardware.group-not-found` |
| `POST /api/hardware-groups` `{code, names}` | `201`, `Location`, `{code}`; the group goes to the end of the order | `400 hardware.group-code-invalid`, `400 hardware.name-required` (a blank name is none), `400 hardware.name-too-long`, `400 language.unknown`; `409 hardware.group-code-taken` (field `code`) |
| `PUT /api/hardware-groups/{code}` `{names}` | `204` | The `400` errors of `POST`; `404` |
| `DELETE /api/hardware-groups/{code}` | `204` | `422 hardware.group-in-use`; `404` |
| `GET /api/hardware/{code}` | `200`, `{code, names, group, platforms}`, the platforms by code | `404 hardware.not-found` |
| `POST /api/hardware` `{code, names, group, platforms}` | `201`, `Location`, `{code}` | `400 hardware.code-invalid`, `400 hardware.name-required`, `400 hardware.name-too-long`, `400 hardware.platform-required`, `400 language.unknown`; `409 hardware.code-taken` (field `code`); `422 hardware.group-unknown` (field `group`), `422 hardware.platform-unknown` (field `platforms`) |
| `PUT /api/hardware/{code}` `{names, group, platforms}` | `204` | The errors of `POST` but the code; `422 hardware.platform-in-use` (field `platforms`) when a platform taken away has software, or a release of it, with the hardware; `404` |
| `DELETE /api/hardware/{code}` | `204` | `422 hardware.in-use` while software or a release has it; `404` |

## Search

Hardware moved to another group (`HardwareRegrouped`) sends the software that has it, by itself or by a release, to the search again ([search.md](search.md)), whose documents keep each item as `group:code`. Renaming changes no document: the names come from the archive.
