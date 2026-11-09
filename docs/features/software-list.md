# Software List

The software page at `/software` ("Software" in the main menu, [main-menu.md](main-menu.md)) shows the archive's software as a grid of cards: the first page of 20, without pagination yet.

## Card

- A 4:3 picture slot. Software has no screenshots yet, so every card shows the image placeholder there.
- The title in one line, cut with an ellipsis when too long; it leads to the software's page ([software-view.md](software-view.md)).
- The release year under it, muted; nothing when the release date is unknown.

## Loading and errors

- The list is the top block of the page, so entering the page preloads it ([data-loading.md](../data-loading.md)): within 300 ms the page appears with the cards, otherwise with card skeletons in the same grid.
- An empty archive says so in place of the grid.
- A business error replaces the grid with an inline message and Retry; a system error goes to the error dialog ([error-handling.md](../error-handling.md)).

## REST

`GET /api/software?page={n}&size={size}` — a page of software (`id`, `title`, `releaseDate` as an ISO day or `null`) with paging metadata.
