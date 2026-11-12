# Full Search

The rules: [domain/search.md](../domain/search.md#full-search); the read: [search.md](search.md#reads).

## Page

- `/search?q={keyword}&types={kinds}&page={n}`, reached from the quick search window ([quick-search.md](quick-search.md)). Titled "Search results for “…”", with the page after the first; without a keyword it is "Search" and nothing is asked. The search follows the platform switch.
- A box for every kind, Authors, Groups, Software, all checked at first; the last one checked cannot be unchecked; "Select all" checks them all again. Another choice goes to its first page. Every kind checked is left out of the address.
- The matches are one list, fifty a page: people, then groups, then software. A page shows the kinds that fall on it in that order, each titled with the count of all its matches ("Authors (52)"): people as a table of the main name, leading to the page of the person, and the real name; groups as a table of names; software as the cards of the catalog.
- One pagination under the list; the page in the address counts from 1, the first left out.
- With no match at all, "Nothing found.".

## Loading and errors

- Entering the page preloads its matches ([data-loading.md](../data-loading.md)); a skeleton shows only when they are slow to come. Another page or choice keeps the shown matches; after 100 ms they go under a spinner and the boxes and pages are locked.
- A failed read is a system error and goes to the error dialog ([error-handling.md](../error-handling.md)); no matches are shown.

## REST

`GET /api/search` ([search.md](search.md#rest)).
