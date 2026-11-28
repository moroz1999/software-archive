# Import from bk-catalog

A backend command that takes bk-catalog, the catalog of software for the BK family kept in a repository on GitHub, into the archive ([domain/imports.md](../domain/imports.md#bk-catalog)). Only the reading of the catalog and the conducting of the run are its own: `BkCatalogSourcePrograms` turns each entry into what [the one import](imports.md) takes in, by the rules of the catalog.

## Running

- An administrator starts it on the Imports tab of management, choosing the source "bk-catalog" and the whole catalog or one of its sections: games, programs or demoscene. The sections are fixed in the archive (`BkCatalogSection`, the OpenAPI value set `BkCatalogSection`); opening the form reads nothing. It runs in the background like every import, one at a time ([imports.md](imports.md#running)).
- `ImportBkCatalogHandler` reads the list of every section chosen, in the order games, programs, demoscene, logging each as list *n* of the sections chosen with its number of entries, then takes the entries in that order, each in a transaction of its own, logging the files of each as downloaded. Before each entry the run writes which one it is at, leading to its card. An entry for the PC (`Windows`, `MS-DOS`) counts as left out with the reason `platform`; an entry that fails is written into the log with its card and the reason, and the run goes on. The run ends done.
- A rerun reads every entry again; the one import finds the software each became by its origin (`bk-catalog`, `SOFTWARE`, the section and id as `game-555901`) and adds only what it lacks.

## REST

| Call | Answer | Errors |
|---|---|---|
| `POST /api/imports/bk-catalog[?section=<code>]` | `202` once the whole catalog or the section (`games`, `software`, `demoscene`) is started | `400 import.section-invalid` for another section; `401 auth.required`; `403 access.denied` for anybody but an administrator; `409 import.running` while a run goes on |

The report of the run is `GET /api/imports/last-run` ([vektor-import.md](vektor-import.md#rest)), with `archive` `bk-catalog` and `section` the code of the section, or `null` for the whole catalog.

## Reading the catalog

- `BkCatalogSite` is the port the run reads through; `HttpBkCatalogSite` reads the list of a section from `content/{section}.csv` of the repository (`raw.githubusercontent.com/kalininskiy/bk-catalog/main/`) and the files from the folders of the section: `bk_games_files` and `bk_games_screenshots` for games, `bk_files` and `bk_screenshots` for programs and demoscene.
- `CsvRows` reads a list by the names of its first row: fields in double quotes may hold commas, line breaks and doubled quotes; a byte order mark is dropped; a short row leaves its last columns empty. `BkCatalogEntry` gives a column stripped, empty when absent.
- A file name is asked for as it is written: each character an address would read otherwise (spaces, Cyrillic, `%`, `#`, `?`) is percent-encoded, so that `%21` in a name stays part of it.
- A file or a screenshot the repository lacks (`404`, `SourceNotFoundException`) is none: its release is taken without a file, or the screenshot is passed over, each with a warning. Any other failure to read it fails the entry.
- Files and screenshots are downloaded whole into memory and keep their name in the list.

## Meanings

The rules are fixed in code beside the parser, with their tests:

- `BkCatalogPlatforms`: each platform of the catalog as the hardware of the BK family and the firmware of its ROM; one not listed gives no hardware and a warning.
- `BkCatalogGenres`: each genre of each section as a category of the archive's tree (`V46` added Games > Adult and Demo > Wild for it); a game without a genre is a game; a genre not listed is passed over with a warning.
- `BkCatalogDates`: a day as `15.01.1992`, `15.01.92` (from `80` of the 1900s, before it of the 2000s) or `2021.03.07`, a year alone or with an English month; several years tell no date, and anything else is no date with a warning.
- `BkCatalogSourcePrograms`: the people of the columns of authors, publisher, graphics and music, each name whole as a label once with the roles of all its columns; every file a release of its own with its description, dated by the day the program came out; the page, the repository and the video as links, never the catalog itself; the lineage of a game by its kind, reliability and rework, and its original as one line of title, platform and address.
