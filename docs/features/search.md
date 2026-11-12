# Search (Typesense)

The rules: [domain/search.md](../domain/search.md). Being built in stages (below); this document is the agreed design and is narrowed to the built state as each stage lands.

PostgreSQL stays the source of truth: every change and every full page of an entity is served from it. Typesense serves search and catalog reads only. The frontend talks to the backend API, never to Typesense.

## Stages

1. **Index infrastructure** — the `software` collection, the outbox, the worker, the rebuild. *Built.*
2. **Software catalog** — `/software` reads from Typesense, with the year, category and country filters ([software-list.md](software-list.md)). *Built.*
3. **Quick search** — the `authors` and `groups` collections, the header button and the window with brief results. ([quick-search.md](quick-search.md)). *Built.*
4. Later: the full search section; the author and group catalogs move to Typesense.

## Collections

One document per entity, with the entity's id as the document id. Facet values are ids, never titles: renaming a category or a country changes no document, the frontend already has the titles.

| Collection | Matched by the keyword | Filters and facets |
|---|---|---|
| `software` | title, alternative titles | platform; release year; categories (each one of the software with all its ancestors); countries |
| `authors` | every label of the person, real name | platforms |
| `groups` | every label of the group, name | platforms |

Platforms of people and groups are those of the software their labels are credited on. People and groups are stored under the main name, with every label beside it; a person without any name has no document.

## Reads

- **Software catalog.** The chosen filters go to `filter_by`; year, category and country are asked through `facet_by`; an empty keyword is `q=*`. The answer is the page and, for each facet, the values with counts over the whole filtered result. `max_facet_values` fits the interface. The values of an active filter come from a separate search without its own condition, keeping the keyword and the other conditions; all searches of one view go in one `multi_search`.
  - As built: one `multi_search` with the main search and, for each chosen filter, a search with `per_page=0` that asks only its own facet; `max_facet_values` is 1000. The keyword is matched over `title` and `alternative_titles`, the page sorted by `title_sort`, the lowercased title. A card's release day is stored with the document, not indexed.
- **Quick search.** One `multi_search` over the three collections with the keyword and the platform: a few hits and the count of matches per section.
  - As built: five hits per section, by `_text_match` and then by the lowercased title or name; a blank keyword finds nothing without asking Typesense.
- **Full search** (later): the same, with a page of its own per section.
- **Typesense unavailable.** A read fails as a system error and goes to the error dialog ([error-handling.md](../error-handling.md)). Edits are not affected.

## Synchronization

1. **Outbox.** In the same transaction as a change, the type and id of every entity whose document changed go to `search_outbox`, written by in-transaction listeners of the domain events ([backend.md](../backend.md)). One row per entity: a new change of a waiting entity renews its row instead of adding one, and the worker removes a row only if it was not renewed while being processed, so no change is lost. Changes reach other documents:
   - software saved or deleted → the software, and the people and groups of its credits before and after (platforms);
   - a label attached to or taken from an owner, deleted → its old and new owner, and the software it is credited on (countries);
   - locations of a person or a group changed → the software their labels are credited on;
   - a category moved or deleted → the software in it and in its subtree.
2. **Worker.** One background worker takes the due tasks after commit. For each it reads the current state from PostgreSQL and upserts the full document under its stable id. A missing entity deletes its document; a document already missing counts as deleted.
3. **Acknowledgement.** A task is removed only after Typesense confirmed it. A failed task stays and is retried with a growing delay.

### As built

- The changes are told by domain events, published by the use cases: `SoftwareChanged`, `AuthorChanged` and `GroupChanged` (added, edited, deleted), `CategoryMoved`, `CategoryDeleted`, and `OwnerOfLabelsChanged` from a person or a group edited or deleted, with the labels it had before (new labels have no credits yet). A deletion tells before it deletes, while the subtree and the credits can still be found. `search/app/EnqueueSearchDocumentsHandler` answers them all.
- A row of `search_outbox` carries a renewal number from a sequence: a new change renews it, and the worker removes the row only with the number it took.
- The worker (`ProcessSearchOutboxHandler`) runs every `search.worker.delay`; the retry delay doubles from 1 s to at most 10 minutes. A call to Typesense that timed out may still be applied once Typesense answers again: a retry then only upserts the same document once more.
- Credits are not edited in the archive yet, so a change of credits enqueues nothing.

## Initial load and rebuild

The rebuild runs inside the backend, in the process of the worker: by itself when a collection alias does not exist yet or points at a collection without every field of the current schema, and on demand by a start option.

1. Pause the worker before reading PostgreSQL; changes keep writing tasks.
2. Build the documents by the rules of the worker and import them in batches into new collections, checking the result of every document.
3. Only when every collection loaded, switch the aliases and drop the old collections.
4. Resume the worker: it applies the tasks gathered meanwhile. On any failure the old aliases stay, the new collections are dropped, the worker resumes.

## Acceptance

- After choosing a year, the categories and countries narrow by every match, including software on the following pages.
- With Typesense down, an edit is saved; once it is back, the worker applies the edit or the deletion.
- With Typesense down, a search or catalog read shows the error dialog.
- A failed rebuild leaves the search on the previous collections.
- Full search (later): going to page 2 of software keeps the current pages of people and groups.

## REST

`GET /api/search/quick?q={keyword}&platform={code}` — `software`, `authors`, `groups`, each `{items, total}`: `items` the first five matches, `total` the count of all. Software as in the catalog (`id`, `title`, `releaseDate`); a person `id`, `name` (the main name), `realName` (`null` when unknown or the main name); a group `id`, `name`. A missing or blank keyword gives empty sections. `400 platform.code-invalid`; a system error when Typesense does not answer.
