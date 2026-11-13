# Release Files

The optional downloadable archive of a release ([domain/software.md](../domain/software.md#releases)) is changed in the release form and offered on the release page.

## Storage

- Files are stored byte for byte in `release-archives.storage-path` (`../upload/releases` from `backend/`) under internal UUIDs. The database keeps the UUID, original file name and size.
- A newly stored file is deleted if its database change rolls back. A replaced, removed, or cascade-deleted file is deleted only after the database change commits.
- Multipart files stay in server-managed temporary storage and are streamed to their permanent location.

## Errors

- Files over 100 MB are refused beside the archive field with `422 release.archive-too-large`.
- A missing original name is refused with `422 release.archive-name-invalid`. Replacement and removal in the same request are refused with `422 release.archive-change-invalid`.
- A missing release archive download returns `404 release.archive-not-found`. Storage failures are system errors.

## REST

Creation and editing are documented with the rest of the release form in [release-editing.md](release-editing.md#rest). Release answers carry `archive: null` or `{fileName, size, url}`.

| Call | Answer | Errors |
|---|---|---|
| `GET /api/software/{id}/releases/{releaseId}/archive` | `application/octet-stream` attachment under the original file name | `404 release.archive-not-found`; `400` for malformed ids |
