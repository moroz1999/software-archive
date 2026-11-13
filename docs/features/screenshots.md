# Screenshots

The screenshots of software and of its releases ([domain/software.md](../domain/software.md#screenshots)): uploaded, ordered and deleted on the editing pages, shown on the software and release pages, in the release list and on every card.

## Storage

- The pictures are files in the folder `screenshots.storage-path` (`../upload` from the folder the backend runs in, `backend/`, so `upload` at the root of the repository, ignored by git), each named by its screenshot's id; the database keeps which screenshots there are, their order and format. The folder is business data like the database: it lives outside the containers and the build and is backed up with the database ([local-dev.md](../local-dev.md)).
- The files follow the transaction of the change: a picture stored by a change that rolls back is deleted at once; a discarded picture is deleted only after its change commits, so a failed deletion keeps it. Deleting a screenshot, a release or software tells which screenshots are gone with a domain event (`ScreenshotsDiscarded`), whose listener discards their pictures.
- The format is told by the first bytes of the file, never by its name or the type the browser sends. A picture is kept as uploaded; nothing is resized or recompressed.
- An upload never sits in memory whole: the server keeps every uploaded file in a temporary file, its first bytes and its size are checked, and the picture is streamed from there to the folder.
- An id is unique across software and releases; the order is a position per owner, and the uniqueness of ids is checked only at the end of a transaction, so reordering may move ids between positions.
- The fixtures profile copies the fixture pictures from `db/fixtures/screenshots` into the folder on start, leaving pictures already there alone ([testing.md](../testing.md#fixtures)).

## Card

The card of software shows its own screenshots, or when it has none those of its releases, releases in the order of the release list (by release day, unknown days last, then title), each release's in its order. One SQL expression (`software/read/CardScreenshotsSql`) gives them to the latest list and to the search documents, so the home page, the catalog and the search show the same pictures; the search keeps them in the `screenshots` field of the software document, not indexed. Every change of the screenshots of software or of a release changes its card, so the search document is updated as for any change of the software ([search.md](search.md#synchronization)).

## Pages

| Where | Block |
|---|---|
| Software page | "Screenshots" between the details and the releases, a gallery of the software's own; left out without any ([software-view.md](software-view.md)) |
| Release page | "Screenshots" below the release facts, a gallery of the release's own; left out without any ([release-view.md](release-view.md)) |
| Releases on the software page | the first screenshot of a release beside its title in the table; all of them on its card |
| Software editing, release editing | "Screenshots" below the form: the pictures in their order, each with "‹" and "›" to move it a place and "Delete"; "Add screenshots" with the formats and the size under it |

- Pointing across a card goes through its screenshots ([design-system/card-screenshots.md](../design-system/card-screenshots.md)); a thumbnail of the gallery opens the picture whole ([design-system/screenshot-gallery.md](../design-system/screenshot-gallery.md)).
- Files are chosen through the button or dropped on it, several at once ([design-system/file-upload.md](../design-system/file-upload.md)). A file of another format or over 10 MB is refused before anything is sent, with the reason next to the screenshots.
- A move sends every screenshot in the new order. When they changed meanwhile, the refusal is shown and the screenshots are read anew, as they are now.
- Deleting asks "Delete screenshot n from the archive?"; a refusal stays in the confirmation.
- While a change runs, every control of the block is locked.

## Loading and errors

- The blocks are lazy: they load when they come into view, with thumbnail skeletons until then ([data-loading.md](../data-loading.md)).
- A refusal of an upload or a move (format, size, sign-in, permission, changed meanwhile) is shown next to the screenshots; system errors go to the error dialog ([error-handling.md](../error-handling.md)).
- The pictures are served with `Cache-Control: max-age=31536000, public, immutable`: a picture never changes under its id.

## REST

Every call on the screenshots of software has one on the screenshots of a release beside it, under `/api/software/{id}/releases/{releaseId}/screenshots`.

| Call | Answer | Errors |
|---|---|---|
| `GET /api/software/{id}/screenshots` | `[{id, url}]` in their order | `404 software.not-found`, `404 release.not-found` |
| `POST /api/software/{id}/screenshots`, multipart `files` (one or more) | `201`, the new `[{id, url}]` | `422 screenshot.format-unsupported`, `422 screenshot.too-large` (field `files`), `401 auth.required`, `403 access.denied`, the `404`s; `400` without files |
| `PUT /api/software/{id}/screenshots/order` `{order: [id]}` | `204` | `409 screenshot.order-outdated` when the order does not name exactly the screenshots there are; `401`, `403`, the `404`s |
| `DELETE /api/software/{id}/screenshots/{screenshotId}` | `204` | `404 screenshot.not-found`, `401`, `403`, the `404`s |
| `GET /api/screenshots/{screenshotId}` | the picture, its type by its format | `404 screenshot.not-found`; `400` for a malformed id |

An upload is taken whole or refused whole. The answers of the latest list, of the catalog and of the searches, and the releases of the software, carry `screenshots: [{id, url}]` of each card and release.
