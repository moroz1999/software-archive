# Release Editing

Adding, editing and deleting the releases of software ([domain/software.md](../domain/software.md#releases)). Releases live inside the software aggregate: every change loads and saves the software, which guards the rule that its platform stays while it or a release has hardware ([domain/hardware.md](../domain/hardware.md#platform-switch)).

## Pages

| Page | Path | Content |
|---|---|---|
| Software page | `/software/{id}` | "Add release" next to the Releases heading, "Edit" with every release ([software-view.md](software-view.md)) |
| Release page | `/software/{id}/releases/{releaseId}` | "Edit" in the menu right after the release title ([release-view.md](release-view.md)) |
| Add release | `/software/{id}/releases/new` | "Add release to {title}": an empty form of unknown type; Cancel returns to the software, "Add release" submits |
| Edit release | `/software/{id}/releases/{releaseId}/edit` | "Edit release of {title}": "Delete" next to the page title; the release as it states itself, Cancel back to the release's page and "Save" |

Every page returns to the software's page once done. The editing page has the release's screenshots below the form ([screenshots.md](screenshots.md#pages)).

The edit page shows the breadcrumbs of the release ([release-view.md](release-view.md#page)), the release leading to its page — by the title of its software when it has none — then "Editing". Adding pages have none.

## Form

- Layout: sections Main (Title; Version, Type and Release date in one row; Description), Hardware and archive, and Credits ([design-system/form.md](../design-system/form.md)); Cancel and the submit button at its bottom right.
- Title (empty: the release is called by its software's title, at most 255 characters), Version (at most 50), Type (required, the fixed list), Release date (a year or a day, one text field with a hint: `1987` or `12.05.1987`; the API keeps the ISO day), Description.
- Hardware: the hardware of the software's platform, by group, in the picker the software form uses too ([design-system/multi-select-filter.md](../design-system/multi-select-filter.md)). The form holds only the release's own hardware, not what it shows merged with the software's; a group checked here replaces that group of the software.
- Archive: one optional file of any type, up to 100 MB. The editing form shows its current original name and size and offers replacement or removal. The chosen archive change is saved atomically with the other fields ([release-files.md](release-files.md)).
- Credits: "Person or group" with roles, the editor the software form uses ([software-editing.md](software-editing.md#form)); a participant gone from the archive meanwhile is refused under it: "One of those credited is no longer in the archive: choose again." Saving joins an owner credited by themselves and under a label of theirs into the label ([domain/software.md](../domain/software.md#credits)).
- Format errors, refusals under their fields, locking while saving: as in the software form ([software-editing.md](software-editing.md#form)).

## Deleting

"Delete" asks "Delete the release “{title}” {version} from the archive?" (the software's title when the release has none); "Delete for good" deletes it.

## Loading and errors

- The pages preload the software, then the hardware of its platform, and on editing the release ([data-loading.md](../data-loading.md)); the form skeleton until all are there.
- Unknown software or release, a malformed id in the URL, and a release deleted in the meantime show the not found page ([error-handling.md](../error-handling.md)).
- Hardware of another platform is refused under Hardware; system errors go to the error dialog.

## REST

| Call | Answer | Errors |
|---|---|---|
| `GET /api/hardware?platform={code}` | `[{code, name, group, groupName}]` by the order of the groups, then name; `groupName` in the language of `Accept-Language`, else in Russian; empty for an unknown platform | `400 platform.required`, `400 platform.code-invalid` |
| `GET /api/software/{id}/releases/{releaseId}` | `{id, title, version, type, releaseDate, description, hardware, archive, credits}`: its own fields and hardware; `credits` as in the software details ([software-editing.md](software-editing.md#rest)); `archive` is `null` or `{fileName, size, url}` | `404 software.not-found`, `404 release.not-found` |
| `POST /api/software/{id}/releases`, multipart `release` (the JSON fields, `credits: [{type, id, roles}]` among them, as for software) and optional `archive` | `201`, `Location`, `{id}` | the credit errors of software ([software-editing.md](software-editing.md#rest)), `400 release.type-required`, `400 release.type-invalid`, `400 title.too-long`, `400 release.version-too-long`, `400 release-date.invalid` (not a year `1987` or a day `1987-05-12`), `400 hardware.code-invalid`, `422 hardware.unknown`, `422 release.archive-too-large`, `404 software.not-found` |
| `PUT /api/software/{id}/releases/{releaseId}`, the same multipart fields and optional `removeArchive=true` | `204` | the errors of `POST`, `422 release.archive-change-invalid`, `404 release.not-found` |
| `DELETE /api/software/{id}/releases/{releaseId}` | `204` | `404 software.not-found`, `404 release.not-found` |
