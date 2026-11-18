# UI Localization

The interface ships English and Russian translations with the frontend. The language control is in the main menu beside the platform switch.

The explicit choice is kept in browser storage under `archive.language`. Without one, the frontend uses a supported browser language and falls back to English. The document language follows the active choice for assistive technology, and locale-sensitive UI values use the same language.

## Names in several languages

The names of categories, countries, cities, platforms and hardware follow the UI language ([domain/localization.md](../domain/localization.md)). Every call of the frontend sends the UI language as `Accept-Language`, from the first one on; switching the language loads what is shown again in the new one.

The backend answers in the most wanted language of `Accept-Language` the site speaks, and in English without one; a name missing in that language comes in Russian. Search keeps categories, countries and platforms by their ids and codes, not their names ([search.md](search.md)), so it needs no languages.

## Storage

- `language` lists the languages of the site by their codes (`en`, `ru`).
- Each kind of name has a table of its own, one row per owner and language: `category_title`, `country_name`, `city_name`, `platform_name`, `hardware_name`. A row goes with its owner by `ON DELETE CASCADE`.
- The Russian titles of the tree of zxart come from zxart.ee with the tree; the names of platforms and hardware in both languages are reference data of the schema migration that brings them. The fixtures name countries and cities in both languages.
