# Import from vektor-06c.ru

A backend command that takes the software of vektor-06c.ru into the archive ([domain/imports.md](../domain/imports.md)). Only the reading of the site and the conducting of the run are its own; everything it adds goes through the use cases of the archive, under the importer's account, so the rules of software, releases, screenshots and credits hold for it as for anybody.

## Running

- `make import-vektor` imports the whole site; `make import-vektor CATEGORY=zagruzchiki` imports one section, by the code in its address. Production runs it once on an empty archive, whole; locally a small section is enough to try it.
- The `import-vektor` profile starts no web server and no search worker: the application stops when the run is done. What it added sits in the search outbox and reaches the search once the archive runs again.
- The run ends with a summary in the log: added, already added, left out (PC utilities), failed. A failed program is logged with its id, address and cause.

## Rerunning

- Every added program and every label created for an author name is remembered in `external_origin` by (archive, kind, external id): the site id for software, the name for a label. A program found there is not read again; a name found there credits the label it gave before, wherever the label went since.
- Each program is added in a transaction of its own: its labels, software, release with the archive, screenshot and origin commit together or not at all, so a failed program leaves no trace and is tried again by the next run.
- The importer's account is created by the schema (`V18`); nobody can sign in with it.

## Reading the site

- `VektorSite` is the port the run reads through; `HttpVektorSite` walks the list pages (`?PAGEN_2=N` up to the last page the first one names) and parses pages with `VektorPages`.
- `JdkVektorHttp` asks one thing at a time with a pause before each request, growing with every retry; a server failure (5xx, 429) or a broken connection is tried up to three times, a missing page once.
- The archive and the picture are downloaded whole into memory: the site's files are far below the limits of release archives and screenshots.
- The picture the site shows when it has no screenshot (`Skrinshota-net…`) is not taken.
