# The one import

Every source archive has a parser of its own that only reads the source and hands each program over as a `SourceProgram` (`importer/domain/source`), with its file and screenshots already downloaded. `ImportProgramHandler` takes it in by the rules of [domain/imports.md](../domain/imports.md), in one transaction per program, through the use cases of the archive and as the importer (`Importer.ACTOR`, an administrator in code only: the account cannot sign in and is not listed as one). It answers whether the program was added or found, and writes that to the report of the run.

## Finding a program

- First by origin: `external_origin` (archive, `SOFTWARE`, id).
- Otherwise by its file. The file is parsed with the release file parser and `ProgramFiles` takes the digests of its program files: by extension, whatever the case, fixed per platform in code (Vector: `rom`, `fdd`, `cas`, `wav`), never looking into a program file. `ReleaseFileTrees` gives the stored trees of releases of the same platform holding any of those digests, and the first whose program files are the same set is the program. A file without program files matches nothing. A match is remembered as an origin of this source too.
- Otherwise the program is added: software, its one original release with the file, the screenshots (one per size), the credits and the origin with the address of its page.

## Adding to a program found

`SupplementSoftwareHandler` fills the description and the date while empty, joins the categories, credits only participants not credited yet (roles there stay) and adds screenshots of sizes the software has none of. Titles, hardware and releases are never touched.

## Authors

`CreditImportedAuthorsHandler` credits each author once, with the role `unknown`.
- Looked for by origin (archive, kind, the source id or the label text): as `AUTHOR`, then `GROUP`, then `LABEL`. A label made into a person moves its origin to `AUTHOR` (`TransferReplacedLabelOriginHandler`); a label made into a group stays the group's label and is credited as such.
- One not found by origin is looked for among the people and groups of the archive (`MatchingAuthors`, `JdbcMatchingAuthors`): people by the full real name and a label of the nickname, both whatever their case; groups by the name or a label compared by letters and digits alone (`regexp_replace` of everything but `[:alnum:]`). A candidate must agree with the place of the source: the place is looked up without being added, a city with its country, the country `Unknown` telling nothing; a candidate without locations, or a source without a place, always agrees. Only a single candidate left is taken, and its origin is remembered under its kind.
- One found neither way is created as the source tells: a label of nobody, a person with the real name and the label as their display name, or a group without types; its origin is remembered under that kind.
- A person is credited under the label of the nickname the source names them by (`author-label`), not as the person: which nickname a program came out under is kept. Only when the person has no label of that name any more, renamed or given away, is the person credited.
- A person or a group found gets the real name and the location only while empty (`SupplementAuthorHandler`, `SupplementGroupHandler`); a place is looked up only for one without locations.
- Places: a source tells a city, a country, or a place that may be either (`CITY_OR_COUNTRY`), which is the country of that name when the archive has one (`GeoNameRepository.countryNamed`) and a city otherwise. `FindOrAddCityHandler` and `FindOrAddCountryHandler` look a name up among the names of all languages, whatever its case, and add one the archive lacks with the name in the language of the source; a new city goes to the country `Unknown` (`CountryId.UNKNOWN`, `V28`).

## Running

- An administrator starts an import on the Imports tab of management (`/management/imports`, `features/import/source`): the source first, then for vektor-06c.ru the whole site or one section. `StartImportHandler` starts every source (`ImportSource`), so only one import runs at a time, whatever its source; a start while one runs answers `409 import.running`.
- The report of the run started last names its archive: "vektor-06c.ru · section" (or the whole site) and "caglrc.cc (Basis)".
- Both parsers read through one polite client, `SourceHttp` (`JdkSourceHttp`): one request at a time, a pause before each, a server failure or a broken connection tried up to three times, a missing page once.

## Sources

- [vektor-06c.ru](vektor-import.md)
- [caglrc.cc/scalar, "Базис"](basis-import.md)
