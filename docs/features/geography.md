# Geography

Keeping the countries and the cities from the management ([domain/geo.md](../domain/geo.md#keeping)).

## Pages

| Page | Path | Content |
|---|---|---|
| Geography | `/management/geography` | "Add country" and "Add city" next to the title; the management tabs; "Cities of no country" with a choice of country and "Put in the country" for each, "Every city is in a country." without them; then "Possible duplicates", "No possible duplicates." without them; under it the tree of countries, folded to them, each country and city leading to its editing with the count of places in it |
| Add country, Add city | `/management/geography/countries/new`, `/management/geography/cities/new` | Management tabs and an empty form; Cancel and the submit button both return to the tree |
| Edit country, Edit city | `/management/geography/countries/{id}/edit`, `/management/geography/cities/{id}/edit` | Management tabs; "Merge into another" and "Delete" next to the title; the form with Cancel and "Save" back to the tree; the tree after merging or deleting |

## Cities of no country

- Each city leads to its editing and shows its count of places; "Put in the country" stays locked until a country is chosen.
- Once a city is put in a country, the list and the tree are read again, and the city leaves the list. A country gone in the meantime is refused next to the city ("The country is not in the archive any more. Reload and try again."); choosing another country clears it.

## Possible duplicates

- Each group of countries, then of cities, that may be one is a line of links to their editing, a city with its country ("Helsinki, Finland", "Helsinki, country not specified"), each with its count of places.
- Cities are grouped by country, a city of no country in the group of every country with a city named like it.

## Merging

- "Merge into another" opens "Merge country" or "Merge city": "Its cities and places move to the country chosen, and “{name}” is deleted." or "Its places move to the city chosen, and “{name}” is deleted."
- A country chooses another country; a city chooses a country, then a city of it. The one merged is never offered; "Merge" stays locked until a choice is made.
- Once merged, the lists of places are read again and the tree opens. A refusal shows inside the dialog: a country or a city gone in the meantime ("… is not in the archive any more. Reload and try again."); a new choice clears it.

## Form

- Name in Russian (required), Name in English (optional); a city's Country, "Country not specified" for none; Latitude and Longitude.
- Format rules are checked before sending: the Russian name, a latitude from −90 to 90 and a longitude from −180 to 180, both or none. An error shows under its field on submit and clears once the form is edited.
- A country gone in the meantime is refused under Country ("The country is not in the archive any more. Reload and try again.").

## Deleting

"Delete" asks "Delete “{name}” from the archive?"; a country with cities or places and a city places are in are refused inside the dialog.

## Loading and errors

- The tree page preloads the cities of no country, the countries to put them in, the possible duplicates and the tree; an edit page preloads its country or city, and a city's page the countries too ([data-loading.md](../data-loading.md)). A late answer shows a skeleton first.
- An unknown country or city, and an id in the URL that cannot be one, show the not found page; system errors go to the error dialog.

## REST

| Call | Answer | Errors |
|---|---|---|
| `GET /api/geography` | `200`, `[{id, name, places, cities: [{id, name, places}]}]`, countries and cities by the name in the language of `Accept-Language`, else in Russian | |
| `GET /api/geography/cities-of-no-country` | `200`, `[{id, name, places}]`, by name | |
| `GET /api/geography/duplicates` | `200`, `{countries: [[{id, name, country: null, places}]], cities: [{country, cities: [{id, name, country, places}]}]}`: groups of what may be one, cities by the name of their country, `null` for a group of cities of no country only | |
| `POST /api/countries/{id}/merge`, `POST /api/cities/{id}/merge` `{into}` | `204`; the one merged is deleted | `400 geo.merge-target-required` without `into`, `422 geo.merge-into-itself`, `422 geo.country-unknown` / `422 geo.city-unknown` (field `into`), `404` for the one merged; `401`/`403` for anyone but an administrator |
| `PUT /api/cities/{id}/country` `{countryId}` | `204`; the city keeps its names and coordinates | `400 geo.country-required` without a country, `422 geo.country-unknown`, `404 geo.city-not-found`; `401`/`403` for anyone but an administrator |
| `GET /api/countries/{id}`, `GET /api/cities/{id}` | `200`, `{id, names, latitude, longitude}`, a city with `countryId` too; `names` by language code, the Russian one always there | `404 geo.country-not-found`, `404 geo.city-not-found` |
| `POST /api/countries`, `POST /api/cities` `{names, latitude, longitude}`, a city with `countryId` | `201`, `Location`, `{id}` | `400 geo.name-required` without a Russian name (a blank one is none), `400 geo.coordinates-invalid`, `400 language.unknown`; `422 geo.country-unknown` (field `countryId`); `401`/`403` for anyone but an administrator |
| `PUT /api/countries/{id}`, `PUT /api/cities/{id}` (same body) | `204` | The errors of `POST`; `404` |
| `DELETE /api/countries/{id}`, `DELETE /api/cities/{id}` | `204` | `422 geo.country-in-use`, `422 geo.city-in-use`; `404` |

## Moving and merging

A city put in another country, from its form or from the cities of no country (`CityMoved`), has every place in it moved there in the same transaction, as a place keeps the country of its city; the software credited on the people and groups located in it, by themselves or by a label of theirs, goes to the search again, whose documents tell the countries of software by their places.

A merged city (`CityMerged`) or country (`CountryMerged`) has its places, and a country its cities, moved the same way before it is deleted, an owner who then has a place twice keeping it once, primary when either was; the software credited on the people and groups located in either of the two goes to the search again.
