# Release Files

The optional downloadable archive of a release ([domain/software.md](../domain/software.md#releases)) is changed in the release form and offered on the release page.

## Storage

- Files are stored byte for byte in `release-archives.storage-path` (`../upload/releases` from `backend/`) under internal UUIDs. The database keeps the UUID, original file name and size.
- A newly stored file is deleted if its database change rolls back. A replaced, removed, or cascade-deleted file is deleted only after the database change commits.
- Multipart files stay in server-managed temporary storage and are streamed to their permanent location.

## Parsed structure

- Adding, replacing or removing a release file, and moving its software to another platform, discard the parsed structure in the transaction of the change. After that change commits, the file is parsed from storage in a transaction of its own; a failure is logged and leaves the file waiting for its structure. Deleting the release removes the structure by cascade.
- One row represents the root file and every contained file or directory. Every row carries the release id for a direct lookup, while the parent id and position preserve the tree. Files carry their MD5 digest, executable flag and unreadable flag (only a root can have it); directories carry neither a digest nor an executable flag.
- Executable extensions are fixed in code per platform, not edited by administrators. Extension matching is case-insensitive; Vector has `rom`.
- ZIP is recognized by its signature, independently of its name, and nested ZIP files are opened recursively. An executable is a leaf even when its bytes have a container signature. A tree is never partial: a ZIP that cannot be read at any level, entry names outside UTF-8 included, leaves only the root row, flagged unreadable. Only a root that is a container is held in memory; any other root is hashed as it streams.
- A container nested deeper than ten levels, or combined expanded content over 500 MB, flags the root unreadable the same way. The file itself remains available.
- Future disk and tape readers use the same tree contract but disable executable recognition for every descendant of the medium.

Existing files are deliberately reparsed only by the offline command:

```bash
docker compose --env-file .env.prod -f compose.prod.yaml run --rm backend reparse-release-files
```

The command runs without the web server or search worker and replaces every stored tree. A release whose file fails is logged with its id and the rest go on; the command ends with the counts and exits with 1 when any failed. It is optional: a file without a tree is shown as waiting for its structure. It is run after parser changes that should affect existing files.

## Errors

- Files over 100 MB are refused beside the archive field with `422 release.archive-too-large`.
- A missing original name is refused with `422 release.archive-name-invalid`. Replacement and removal in the same request are refused with `422 release.archive-change-invalid`.
- A missing release archive download returns `404 release.archive-not-found`. Storage failures are system errors.

## REST

Creation and editing are documented with the rest of the release form in [release-editing.md](release-editing.md#rest). Release answers carry `archive: null` or `{fileName, size, url}`.

| Call | Answer | Errors |
|---|---|---|
| `GET /api/software/{id}/releases/{releaseId}/archive` | `application/octet-stream` attachment under the original file name | `404 release.archive-not-found`; `400` for malformed ids |
| `POST /api/software/{id}/releases/{releaseId}/files/reparse` | `204`; the file is parsed again within the request, by whoever may change the release | `401`/`403` by release rights, `404 release.archive-not-found` for a release without a file, `404 release.not-found` / `software.not-found`; storage failures are system errors |
| `GET /api/software/{id}/releases/{releaseId}/files` | `{root}`: `null` while the file waits for parsing, otherwise `{id, name, directory, executable, unreadable, children}`; an unreadable root has no children with recursive children in stored order | `404 release.not-found` for an unknown software or release, `404 release.archive-not-found` for a release without a file; `400` for malformed ids |
