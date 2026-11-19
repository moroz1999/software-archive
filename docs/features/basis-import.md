# Import from caglrc.cc/scalar ("Базис")

A backend command that takes the card index "Базис" of software for the Vector-06C into the archive ([domain/imports.md](../domain/imports.md#caglrcccscalar)). Only the reading of the site and the conducting of the run are its own: `BasisSourcePrograms` turns each card into what [the one import](imports.md) takes in, by the rules of the site.

## Running

- An administrator starts it on the Imports tab of management, choosing the source "caglrc.cc (Basis)"; it has no sections, a run reads the whole index. It runs in the background like every import, one at a time ([imports.md](imports.md#running)).
- `ImportBasisHandler` takes the cards in the order of the list, each in a transaction of its own: a card in a category that is not software of the archive (PC, paper) counts as left out, a card that fails is written into the report with its page and the reason, and the run goes on. The run ends done.
- A rerun reads every card again; the one import finds the software each became by its origin (`caglrc.cc`, `SOFTWARE`, the card id) and adds only what it lacks.

## REST

| Call | Answer | Errors |
|---|---|---|
| `POST /api/imports/basis` | `202` once the run is started | `401 auth.required`; `403 access.denied` for anybody but an administrator; `409 import.running` while a run goes on |

The report of the run is `GET /api/imports/last-run` ([vektor-import.md](vektor-import.md#rest)), with `archive` `caglrc.cc` and `section` `null`.

## Reading the site

- `BasisSite` is the port the run reads through; `HttpBasisSite` reads the whole list at `/scalar/categories/all/` (`/scalar/ware/` redirects there), the card of each program at `/scalar/ware/{id}/` and the page of each author at `/scalar/author/{id}/`. An author page is read once per run and kept for the cards that credit the same author.
- `BasisPages` parses the pages: a card gives the title, the year (empty when none), the category names, the description as text with its line breaks, the screenshots, the author ids and the one file (`a#zip-file`), which a card may lack; an author page gives the name, the nickname and the place, each possibly missing.
- What each category name means is fixed in code, `BasisCategories`: a category of the archive, a computer of the Vector family, hardware the program uses, the storage of the release, or a mark left out. A name not listed is passed over.
- The file and the screenshots are downloaded whole into memory and keep the last part of their address as their name.
