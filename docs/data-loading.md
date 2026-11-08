# Data Loading

How every view gets its data from the backend. Mandatory for all views and components that show server data.

## Split by logical calls

- Data is split into **logical calls**: one backend call per logical block of a view (the software list, a software's details, its releases, its credits, …), not one call per page. The backend exposes endpoints accordingly.
- Each block loads its own call; blocks do not wait for each other.

## First entry: preload with a 300 ms window

- Entering a route **preloads** the view: the calls of the components at the top of that view's viewport start immediately, before the view renders.
- If the preloaded calls finish **within 300 ms**, the view is shown at once with its data — no skeleton flashes.
- If they do not, the view is shown **with skeletons**, and each block fills in as its data arrives.
- Each route declares which calls it preloads: those of the blocks visible at the top of the view without scrolling.

## Everything else: lazy by viewport

- Blocks below the preloaded ones load **lazily, when they enter the viewport** (intersection), not on view entry.
- While a block loads, it shows **its skeleton** — the concrete skeleton of its own layout.
- The rule applies to every component with server data, not only to pages.

## Skeletons vs spinners

| Situation | Feedback |
|---|---|
| First load of a block (route entry after 300 ms, or a lazy block entering the viewport) | Skeleton |
| Pagination, filters, sorting, reload of a block that already shows data | Spinner over the existing content (`LoadingState`), controls locked |

A skeleton is never shown again once a block has displayed data.

## Errors

Loading follows [error-handling.md](error-handling.md): a business error is shown inside the block, a system error goes to the error dialog with a retry.

## How it is built

- A logical call of a view is declared once as a **view request** (`viewRequest(load)` from `shared/api`) next to the block that shows it, and exported by its slice. A call of one of many entities (a software's details by id) is a family: `viewRequestFamily(load)` keeps one view request per key, taken with `.for(key)` by the loader and the block alike.
- The app router is created once per page load, in the entry point, outside React: a router starts the first route's loader as soon as it exists and follows the browser history, so a second one (StrictMode renders twice) would repeat every call.
- A route's **loader** preloads its view: `preloadView([...requests])` starts the calls of the top blocks and resolves when they finish or when the 300 ms window is over. It never fails: errors reach the blocks.
- A block loads through `useRequest(request)`. If the route preloaded the call, the block takes it over instead of calling again: a finished call is shown on the first render, a call still in flight is awaited behind the skeleton.
- A lazy block passes `{enabled: visible}` to `useRequest`, with `visible` from `useInViewport()` (`shared/lib`) attached to its skeleton: the call starts when the skeleton enters the viewport. Without intersection support blocks load at once.
- A reload of a block that shows data keeps the data (`reloading` in the state) and shows it in `LoadingState`.
