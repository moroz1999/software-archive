# Import from caglrc.cc/scalar ("Базис")

A backend command that takes the card index "Базис" of software for the Vector-06C into the archive ([domain/imports.md](../domain/imports.md#caglrcccscalar)). Only the reading of the site and the conducting of the run are its own: `BasisSourcePrograms` turns each card into what [the one import](imports.md) takes in, by the rules of the site.

## Running

- An administrator starts it on the Imports tab of management, choosing the source "caglrc.cc (Basis)" and the whole card index or one category. The category choices are kept with their meanings in the archive; opening the import form does not read the source. Categories containing only PC software or paper are not offered. It runs in the background like every import, one at a time ([imports.md](imports.md#running)).
- `ImportBasisHandler` logs the chosen list read (page 1 of 1) and takes the cards in its order, each in a transaction of its own, logging the files of each as downloaded: before each card the run writes which one of the list it is at; a card in a category that is not software of the archive (PC, paper) counts as left out, with the first such category, a card that fails is written into the log with its page and the reason, and the run goes on. The run ends done.
- A rerun reads every card again; the one import finds the software each became by its origin (`caglrc.cc`, `SOFTWARE`, the card id) and adds only what it lacks.

## REST

| Call | Answer | Errors |
|---|---|---|
| `GET /api/imports/basis/categories` | The locally known names of the categories that can contain imported software | `401 auth.required`; `403 access.denied` for anybody but an administrator |
| `POST /api/imports/basis[?category=<name>]` | `202` once the whole card index or the category is started | `401 auth.required`; `403 access.denied` for anybody but an administrator; `409 import.running` while a run goes on |

The report of the run is `GET /api/imports/last-run` ([vektor-import.md](vektor-import.md#rest)), with `archive` `caglrc.cc` and `section` the category name, or `null` for the whole card index.

## Reading the site

- `BasisSite` is the port the run reads through; `HttpBasisSite` first reads the whole list at `/scalar/categories/all/` or the selected one at `/scalar/categories/{name}/`, then the card of each program at `/scalar/ware/{id}/` and the page of each author at `/scalar/author/{id}/`. An author page is read once per run and kept for the cards that credit the same author.
- `BasisPages` parses those pages: a card gives the title, the year (empty when none), the category names, the description as text with its line breaks, the screenshots, the author ids and the one file (`a#zip-file`), which a card may lack; an author page gives the name, the nickname and the place, each possibly missing.
- The source category catalog and what each name means are fixed together in code, `BasisCategories`: a category of the archive, a computer of the Vector family, hardware the program uses, the storage of the release, or a mark left out. A name not listed is passed over.
- The file and the screenshots are downloaded whole into memory and keep the last part of their address as their name.
