# Author Catalog

The catalog at `/authors` ([domain/authors.md](../domain/authors.md#author-catalog)): entries in four columns (three on tablets, one on phones), 100 per page, only the authors credited on software of the platform chosen in the switch ([platform-switch.md](platform-switch.md)).

The group catalog ([group-catalog.md](group-catalog.md)) works the same way. Their common parts are entities: `entities/labels` holds the letters, the letter filter, an entry and the skeleton of the entries; `entities/locations` holds the countries, the cities and the place filters.

## Page

- Letters: "All", A–Z, А–Я, `#`; the current one is marked.
- Country and City: City offers the cities of the chosen country and is locked until one is chosen; choosing another country forgets the city.
- An entry is the name leading to the person's page ([author-view.md](author-view.md)), with the person's real name muted on the same line; a label of a person without a real name says "label of {main name}" there instead.
- Pages below the entries; "No authors found." when nothing matches.

## Address

The filter lives in the address — `/authors?letter=Б&country={id}&city={id}&page=2` (the page counts from 1) — so it can be shared and the back button works. A letter keeps the place filters; any change of a filter goes back to the first page. A value that cannot be a filter (an unknown letter, a malformed id, a city without a country) is left out and never sent.

## Loading and errors

- Entering the page preloads the entries of the address, the countries and, with a country, its cities; within the window the page appears filled, otherwise with a skeleton of the entries.
- Another letter, place or page is shown at once: the entries stay under the spinner, the letters, the place filters and the pages locked, until the new ones arrive ([data-loading.md](../data-loading.md)).
- System errors go to the error dialog ([error-handling.md](../error-handling.md)); without countries the place filters stay locked and the entries are still shown.

## REST

| Call | Answer | Errors |
|---|---|---|
| `GET /api/authors?letter=&country=&city=&platform=&page=&size=` | page of `{personId, name, realName, labelOf}`; `realName` `null` when unknown or when the entry is the real name, `labelOf` `null` for a main name | `400 label.letter-invalid`, `400 platform.code-invalid`; `400` for a malformed id |
| `GET /api/countries` | `[{id, name}]` by name | |
| `GET /api/countries/{id}/cities` | `[{id, name}]` by name; empty for an unknown country | `400` for a malformed id |
