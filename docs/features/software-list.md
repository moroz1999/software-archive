# Software List

The software page ("Software" in the main menu, [main-menu.md](main-menu.md)) is the catalog of software ([domain/search.md](../domain/search.md#software-catalog)): a grid of cards, 20 to a page, sorted by title whatever the case, of the platform chosen in the switch ([platform-switch.md](platform-switch.md)), narrowed by a keyword, a year, a category and a country. It is read from the search index ([search.md](search.md)).

## Address

- `/software` — every category; `/software/category/{categoryId}` — the software of the category and its whole subtree.
- The rest is in the query: `q` the keyword, `year`, `country` (an id), `page` counted from 1; what is not chosen and the first page are left out. Every filter is changed by going to its address, so a filter can be shared and the back button works.
- A query value that cannot be a filter is ignored. A category in the path that is not an id is the not found page; an id that no category has finds nothing.

## Page

- "Add software" next to the page title leads to adding software ([software-editing.md](software-editing.md)).
- The filters, the category chips, then the cards with the pages under them.

## Filters

- **Search** — a field over the cards. The keyword finds software by the beginning of the words of its title and alternative titles; it is looked for once the visitor stops typing for 300 ms, from the first page.
- **Year** and **Country** — selects with "Any year" and "Any country", each value with its count, as "1987 (2)"; years go in order, countries by count. Choosing a value goes to its first page and keeps the category and the other filters.
- **Categories**, as chips under the filters:
  - with a category chosen, the path to it: "All categories", its ancestors and the category itself, marked as current;
  - the subcategories of the chosen category, or the top of the tree without one, that have software among the result, each with its count, as "Games (3)". A category without software is not offered.
  - A chip leads to the first page of its category, keeping the keyword and the other filters.

## Card

- A 4:3 picture slot. Software has no screenshots yet, so every card shows the image placeholder there.
- The title in one line, cut with an ellipsis when too long; it leads to the software's page ([software-view.md](software-view.md)).
- The release year under it, muted; nothing when the release date is unknown.

## Loading and errors

- Entering the catalog preloads its page of the address with the category tree and the countries ([data-loading.md](../data-loading.md)): within 300 ms the page appears with them, otherwise with card skeletons in the same grid.
- Another filter, category or page loads with the current cards kept under a spinner; the selects, chips and pages are locked until it arrives. The search field never is, so typing goes on.
- When nothing matches: "Nothing found." in place of the grid.
- A failed read, as with the search index down, is a system error and goes to the error dialog ([error-handling.md](../error-handling.md)); no cards are shown.

## REST

`GET /api/software?q={keyword}&platform={code}&year={year}&category={categoryId}&country={countryId}&page={n}&size={size}` — every parameter optional; a blank keyword is none.

- `content` — the page of software (`id`, `title`, `releaseDate` as an ISO day or `null`); `page` — `size`, `number`, `totalElements`, `totalPages`.
- `facets` — `years`, `categories`, `countries`: the values of each filter, `{value, count}`, counted over the whole result; a category counts the software of its subtree. The values of a chosen filter are counted without its own condition.
- 400 for an invalid platform code (`platform.code-invalid`) or a year, category or country that is not a number or an id.
