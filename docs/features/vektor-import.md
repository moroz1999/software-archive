# Import from vektor-06c.ru

A backend command that takes the software of vektor-06c.ru into the archive ([domain/imports.md](../domain/imports.md)). Only the reading of the site and the conducting of the run are its own; everything it adds goes through the use cases of the archive, under the importer's account, so the rules of software, releases, screenshots and credits hold for it as for anybody.

## Running

- An administrator starts it on the Imports tab of management (`/management/imports`, `features/import/vektor`): the whole site, or one section chosen by the code in its address. The sections offered are those the import places in categories, so the list is kept in one place, `VektorSections`. Production runs it once on an empty archive, whole; locally a small section is enough to try it.
- The tab says the import has started, or why it could not: another run is going on, or the user is not an administrator.
- Below the form, the report of the run started last: what it imports, its state (running, done, stopped), when it ran, the counts, the programs added with links to their software (one deleted since is named without a link) and the programs failed with their page on the site and the reason. While the run goes on the tab reads it again every 5 seconds.
- A run takes far longer than a request waits, so it goes to the application's pool of background threads (`applicationTaskExecutor`, not the scheduler of the search worker) and the request is answered at once. Only one run goes at a time; restarting the backend stops a run, and the next run takes up what is left.
- Every run is an `ImportRun` (`import_run`, `V21`), written down before it starts so that its report fills from the background thread. Each program writes what it came to as it goes: an added program in the transaction that adds it, so the report never names software that is not there; one already taken, left out (PC utilities) or failed in a transaction of its own. The run ends done; a failure of the whole run ends it stopped, and so does a restart: on start the application stops every run still marked running. The log keeps the summary and every failure with its cause. What a run added reaches the search through the outbox like any other change.

## REST

| Call | Answer | Errors |
|---|---|---|
| `GET /api/imports/vektor/sections` | `["3d-programmy", …]`: the codes of the sections to choose from, in alphabetical order | `401 auth.required`; `403 access.denied` for anybody but an administrator |
| `GET /api/imports/last-run` | `{run}`: `null` before the first run; else `id`, `archive`, `section` (`null` for the whole site), `state` (`running`, `done`, `stopped`), `startedAt`, `endedAt` (`null` while running), the counts `added`, `known`, `skipped`, `failed`, `addedPrograms` `[{externalId, softwareId, title, exists}]` and `failedPrograms` `[{externalId, url, reason}]` in the order the run met them | `401 auth.required`; `403 access.denied` for anybody but an administrator |
| `POST /api/imports/vektor[?section=<code>]` | `202` once the run is started | `401 auth.required`; `403 access.denied` for anybody but an administrator; `409 import.running` while a run goes on |

## Rerunning

- Every added program and every label created for an author name is remembered in `external_origin` by (archive, kind, external id): the site id for software, the name for a label. A program also keeps the address of its page (`url`, `V19`), which the software page links to as its source; the address cannot be built from the id, so it is taken from the list it was found in. A program found there is not read again; a name found there credits the label it gave before, wherever the label went since.
- Deleting software forgets its origin in the same transaction (`SoftwareDeleted`, answered by `importer/app/ForgetDeletedSoftwareHandler`), so the next run adds the program again; `V20` forgot the origins of software deleted before.
- Each program is added in a transaction of its own: its labels, software, release with the archive, screenshot and origin commit together or not at all, so a failed program leaves no trace and is tried again by the next run.
- The importer's account is created by the schema (`V18`); nobody can sign in with it.

## Reading the site

- `VektorSite` is the port the run reads through; `HttpVektorSite` walks the list pages (`?PAGEN_2=N` up to the last page the first one names) and parses pages with `VektorPages`.
- `JdkVektorHttp` asks one thing at a time with a pause before each request, growing with every retry; a server failure (5xx, 429) or a broken connection is tried up to three times, a missing page once.
- The archive and the picture are downloaded whole into memory: the site's files are far below the limits of release archives and screenshots.
- The picture the site shows when it has no screenshot (`Skrinshota-net…`) is not taken.
