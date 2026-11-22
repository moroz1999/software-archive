# Geography

Keeping the countries and the cities from the management ([domain/geo.md](../domain/geo.md#keeping)).

## Pages

| Page | Path | Content |
|---|---|---|
| Geography | `/management/geography` | "Add country" and "Add city" next to the title; the management tabs; the tree of countries, folded to them, each country and city leading to its editing with the count of places in it |
| Add country, Add city | `/management/geography/countries/new`, `/management/geography/cities/new` | Management tabs and an empty form; Cancel and the submit button both return to the tree |
| Edit country, Edit city | `/management/geography/countries/{id}/edit`, `/management/geography/cities/{id}/edit` | Management tabs; "Delete" next to the title; the form with Cancel and "Save" back to the tree; the tree after deleting |

## Form

- Name in Russian (required), Name in English (optional); a city's Country, "Country not specified" for none; Latitude and Longitude.
- Format rules are checked before sending: the Russian name, a latitude from −90 to 90 and a longitude from −180 to 180, both or none. An error shows under its field on submit and clears once the form is edited.
- A country gone in the meantime is refused under Country ("The country is not in the archive any more. Reload and try again.").

## Deleting

"Delete" asks "Delete “{name}” from the archive?"; a country with cities or places and a city places are in are refused inside the dialog.

## Loading and errors

- The tree page preloads the tree; an edit page preloads its country or city, and a city's page the countries too ([data-loading.md](../data-loading.md)). A late answer shows a skeleton first.
- An unknown country or city, and an id in the URL that cannot be one, show the not found page; system errors go to the error dialog.

## REST

| Call | Answer | Errors |
|---|---|---|
| `GET /api/geography` | `200`, `[{id, name, places, cities: [{id, name, places}]}]`, countries and cities by the name in the language of `Accept-Language`, else in Russian | |
| `GET /api/countries/{id}`, `GET /api/cities/{id}` | `200`, `{id, names, latitude, longitude}`, a city with `countryId` too; `names` by language code, the Russian one always there | `404 geo.country-not-found`, `404 geo.city-not-found` |
| `POST /api/countries`, `POST /api/cities` `{names, latitude, longitude}`, a city with `countryId` | `201`, `Location`, `{id}` | `400 geo.name-required` without a Russian name (a blank one is none), `400 geo.coordinates-invalid`, `400 language.unknown`; `422 geo.country-unknown` (field `countryId`); `401`/`403` for anyone but an administrator |
| `PUT /api/countries/{id}`, `PUT /api/cities/{id}` (same body) | `204` | The errors of `POST`; `404` |
| `DELETE /api/countries/{id}`, `DELETE /api/cities/{id}` | `204` | `422 geo.country-in-use`, `422 geo.city-in-use`; `404` |

## Moving a city

A city put in another country (`CityMoved`) has every place in it moved there in the same transaction, as a place keeps the country of its city; the software credited on the people and groups located in it, by themselves or by a label of theirs, goes to the search again, whose documents tell the countries of software by their places.
