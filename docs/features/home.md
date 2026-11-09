# Home Page

The home page (`/`, "Recently added") shows the fresh software ([domain/software.md](../domain/software.md#fresh-software)): the 12 most recently added, newest first, as the cards of the software list ([software-list.md](software-list.md#card)).

## Loading and errors

- The block is at the top of the page and is preloaded ([data-loading.md](../data-loading.md)): card skeletons when it takes longer than the window.
- An empty archive says so; errors go to the error dialog ([error-handling.md](../error-handling.md)).

## REST

`GET /api/software/latest?size={n}` — the page contract of the software list, newest first; only the size is taken from the request.
