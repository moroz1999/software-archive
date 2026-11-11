# Data Loading

How every view gets its data from the backend. Mandatory for all views and components that show server data.

## Split by logical calls

- Data is split into **logical calls**: one backend call per logical block of a view (the software list, a software's details, its releases, its credits, …), not one call per page. The backend exposes endpoints accordingly.
- Each block loads its own call; blocks do not wait for each other.

## First entry: preload with a 300 ms window

- Entering a route **preloads** the view: the calls of the components at the top of that view's viewport start immediately, before the view renders.
- If the preloaded calls finish **within 300 ms**, the view is shown at once with its data — no skeleton flashes.
- If they do not, the view is shown **with skeletons**, and each block fills in as its data arrives.
- A skeleton never flashes: once shown, it stays at least **500 ms** from the start of the block's call, and data that comes sooner waits for that.
- Each route declares which calls it preloads: those of the blocks visible at the top of the view without scrolling.

## Everything else: lazy by viewport

- Blocks below the preloaded ones load **lazily, when they come near the viewport** (intersection, 200 px ahead), not on view entry, so a fast call usually finishes before the block is seen.
- While a block loads, it shows **its skeleton** — the concrete skeleton of its own layout.
- The rule applies to every component with server data, not only to pages.

## Skeletons vs spinners

| Situation | Feedback |
|---|---|
| First load of a block (route entry after 300 ms, or a lazy block entering the viewport) | Skeleton |
| Pagination, filters, sorting, reload of a block that already shows data | Spinner over the existing content (`LoadingState`), controls locked |

A skeleton is never shown again once a block has displayed data.

A reload of a block that shows data waits **100 ms** before the spinner and the lock: an answer that comes sooner swaps the data with nothing flashing. Controls stay usable for that moment; a choice made in it simply loads next.

## Errors

Loading follows [error-handling.md](error-handling.md): a business error is shown inside the block, a system error goes to the error dialog with a retry.

## How it is built

- Calls are TanStack Query queries. A logical call is declared once as a `queryOptions` factory in the `api` segment of the slice that owns it (`softwareDetailsQuery(id)`, `softwareListQuery()`), and exported for the route loader. Every software query key starts with the entity's prefix, so a change invalidates them all.
- The query client and the app router are created once per page load, in the entry point, outside React: a router starts the first route's loader as soon as it exists and follows the browser history, so a second one (StrictMode renders twice) would repeat every call. Route loaders are factories that take the query client.
- Every page is its own lazily loaded chunk, while the route's loader stays in the main bundle: entering a route starts its data calls and the download of its page at the same time.
- A route's **loader** preloads its view: `preloadView([queryClient.query(…)])` starts the queries of the top blocks and resolves when they settle or when the 300 ms window is over. It never fails: errors stay in the query cache for the blocks.
- A block's provider reads its query with `useQuery` and hands the query result down as it is. The markup shows the skeleton while `useSkeleton(query) || query.isPending` (`shared/lib/loading`: pending, or held for the minimum after its call started); on `isLoadingError` nothing, as the error dialog shows why it failed; otherwise the data. A missing entity never reaches the block: it is thrown to the route's not found page. A query the route preloaded is taken over: finished data is shown on the first render, a query still in flight is awaited behind the skeleton.
- Loaded data stays fresh for 30 seconds, so a block mounting right after its preload does not call again; a mutation invalidates the queries it changes (without refetching), and the next route entry loads them anew. A failed query is called again by the next route entry, not by every block that mounts.
- A lazy block's provider passes `enabled: visible` to `useQuery`, with `visible` from `useInViewport()` (`shared/lib`) attached to its skeleton: the call starts when the skeleton enters the viewport. Without intersection support blocks load at once.
- A reload of a block that shows data keeps the data and shows it in `LoadingState` while `isRefetching`.
- Filters and pages that live in the address (the author catalog) are changed by navigating; the route skips its preload for a change of the address within the page (`shouldRevalidate`), and the block's query keeps the shown data (`placeholderData: keepPreviousData`) under `LoadingState` while `isPlaceholderData`. The lock and the spinner follow `useLoadingLock` (`shared/lib/loading`), on only once the reload has lasted 100 ms.
- A filter outside the address (the platform switch) changes no route, so no loader runs for it: the queries that follow it keep the shown data the same way.
