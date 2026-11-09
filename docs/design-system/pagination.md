# Pagination

`Pagination` — page navigation of a paged list, as links (`hrefOf(page)`), so a page has its own address.

- `page` counts from 0, `totalPages` is the number of pages; nothing is shown for a single page.
- Shows the first and the last page and the neighbours of the current one; a skipped run is an ellipsis, a skipped single page is shown instead. "Previous" and "Next" are locked at the ends.
- The current page is marked (`aria-current="page"`) and is no link.
- `disabled` locks every link while a page loads.
