# Release Editing

Adding, editing and deleting the releases of software ([domain/software.md](../domain/software.md#releases)). Releases live inside the software aggregate: every change loads and saves the software, which guards the rule that its platform stays while it or a release has hardware ([domain/hardware.md](../domain/hardware.md#platform-switch)).

## Pages

| Page | Path | Content |
|---|---|---|
| Software page | `/software/{id}` | "Add release" next to the Releases heading, "Edit" with every release ([software-view.md](software-view.md)) |
| Add release | `/software/{id}/releases/new` | "Add release to {title}": an empty form of unknown type, "Add release" submits |
| Edit release | `/software/{id}/releases/{releaseId}/edit` | "Edit release of {title}": the release as it states itself, "Save" and "Delete" |

Every page returns to the software's page once done.

## Form

- Title (empty: the release is called by its software's title, at most 255 characters), Version (at most 50), Type (required, the fixed list), Release date (a full day), Description.
- Hardware: the hardware of the software's platform, by group, in the picker the software form uses too ([design-system/multi-select-filter.md](../design-system/multi-select-filter.md)). The form holds only the release's own hardware, not what it shows merged with the software's; a group checked here replaces that group of the software.
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
| `GET /api/hardware?platform={code}` | `[{code, name, group}]` by group, then name; empty for an unknown platform | `400 platform.required`, `400 platform.code-invalid` |
| `GET /api/software/{id}/releases/{releaseId}` | `{id, title, version, type, releaseDate, description, hardware: [{code, name, group}]}`: its own title (`null` when none) and only its own hardware | `404 software.not-found`, `404 release.not-found` |
| `POST /api/software/{id}/releases` `{title, version, type, releaseDate, description, hardware: [code]}` | `201`, `Location`, `{id}` | `400 release.type-required`, `400 release.type-invalid`, `400 title.too-long`, `400 release.version-too-long`, `400 hardware.code-invalid`, `422 hardware.unknown` (field `hardware`), `404 software.not-found` |
| `PUT /api/software/{id}/releases/{releaseId}` (same body) | `204` | the errors of `POST`, `404 release.not-found` |
| `DELETE /api/software/{id}/releases/{releaseId}` | `204` | `404 software.not-found`, `404 release.not-found` |
