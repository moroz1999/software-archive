# The one import

Every source archive has a parser of its own that only reads the source and hands each program over as a `SourceProgram` (`importer/domain/source`): its releases (`SourceRelease`, each with its description, date, hardware and file, already downloaded, or none), screenshots, links, lineage, and the warnings of what the parser could not take as the source gives it (`SourceWarning`). `ImportProgramHandler` takes it in by the rules of [domain/imports.md](../domain/imports.md), in one transaction per program, through the use cases of the archive and as the importer (`Importer.ACTOR`, an administrator in code only: the account cannot sign in and is not listed as one). It answers whether the program was added or found, and writes that to the report of the run, followed by a warning line for each warning of the program (`ImportLogEntry.Warning`); the report counts those lines of the whole run.

## Finding a program

- First by origin: `external_origin` (archive, `SOFTWARE`, id).
- Otherwise by its files. Each file of its releases is parsed with the release file parser and `ProgramFiles` takes the digests of its program files: by extension, whatever the case, fixed per platform in code (Vector: `rom`, `com`, `r0m`, `vec`, `fdd`, `cas`, `wav`; BK: `bin`, `ovl`, `gme`, `bkd`, `img`, `dsk`, `wav`, `tap`, `cod`, `asc`, `foc`), never looking into a program file. `ReleaseFileTrees` gives the stored trees of releases of the same platform holding any of those digests, and the first whose program files are the same set is the program; software that already came from this source is another entry of it and never the match. A file without program files matches nothing. A match is remembered as an origin of this source too.
- Otherwise the program is added: software with its lineage, an original release for each release of the source with its file, description and date, the screenshots (one per size), the credits, the links (`AddSoftwareLinksHandler`, each type and address once) and the origin with the address of its page.

## Adding to a program found

`SupplementSoftwareHandler` fills the description and the date while empty, joins the categories, credits only participants not credited yet (roles there stay) and adds screenshots of sizes the software has none of, adds links it lacks (`Software.addLink`) and takes the lineage only while the software's is unknown (`Software.supplementLineage`, reported as `lineage`). Titles, hardware and releases are never touched.

## Authors

`CreditImportedAuthorsHandler` credits each author once, with the roles the source tells (`SourceAuthor.roles`, `unknown` when it tells none); several authors of the source that are one subject of the archive are credited once with the roles of all (`SoftwareCreditDraft.joinedWith`).
- Looked for by origin (archive, kind, the source id or the label text): as `AUTHOR`, then `GROUP`, then `LABEL`. A label made into a person moves its origin to `AUTHOR` (`TransferReplacedLabelOriginHandler`); a label made into a group stays the group's label and is credited as such.
- One not found by origin is looked for among the people and groups of the archive (`MatchingAuthors`, `JdbcMatchingAuthors`): people by the full real name and a label of the nickname, both whatever their case; groups by the name or a label compared by letters and digits alone (`regexp_replace` of everything but `[:alnum:]`). A candidate must agree with the place of the source: the place is looked up without being added, a city with its country, a city of no known country telling nothing of its country; a candidate without locations, or a source without a place, always agrees. Only a single candidate left is taken, and its origin is remembered under its kind.
- One found neither way is created as the source tells: a label of nobody, a person with the real name and the label as their display name, or a group without types; its origin is remembered under that kind.
- A person is credited under the label of the nickname the source names them by (`author-label`), not as the person: which nickname a program came out under is kept. Only when the person has no label of that name any more, renamed or given away, is the person credited.
- A person or a group found gets the real name and the location only while empty (`SupplementAuthorHandler`, `SupplementGroupHandler`); a place is looked up only for one without locations.
- Places: a source tells a city, a country, or a place that may be either (`CITY_OR_COUNTRY`), which is the country of that name when the archive has one (`GeoNameRepository.countryNamed`) and a city otherwise. `FindOrAddCityHandler` and `FindOrAddCountryHandler` look a name up among the names of all languages, whatever its case, and add one the archive lacks with the name in the language of the source; a new city has no country (`city.country_id` `NULL`). `V34` took the cities out of the stub country `Unknown` of `V28`, and the places through them: the stub stays, with the places given in it directly, for an administrator to remove; the software of the authors located in those cities went to the search outbox again.

## Running

- An administrator starts an import on the Imports tab of management (`/management/imports`, `features/import/source`): the source first, then the whole source or one of its sections or categories. `StartImportHandler` starts every source (`ImportSource`), so only one import runs at a time, whatever its source; a start while one runs answers `409 import.running`.
- The report of the run started last names its archive: "vektor-06c.ru · section", "caglrc.cc (Basis)" or "bk-catalog · section" ("· Whole catalog" for all of it). A run with warnings shows their count apart from the other counts, and each warning line is marked.
- Every parser reads through one polite client, `SourceHttp` (`JdkSourceHttp`): one request at a time, a pause before each, a server failure or a broken connection tried up to three times, a missing page once. A page that is not there (`404`) fails as `SourceNotFoundException`, so that a parser can tell a missing file from a failure.

## Sources

- [vektor-06c.ru](vektor-import.md)
- [caglrc.cc/scalar, "Базис"](basis-import.md)
- [bk-catalog](bk-catalog-import.md)
