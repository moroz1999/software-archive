/** How long entering a route waits for the view's preloaded calls before showing skeletons (docs/data-loading.md). */
export const PRELOAD_WINDOW_MS = 300;

/**
 * The route loader of a view: takes the queries of the blocks at the top of the view, started by the loader,
 * and waits until they settle or the window is over, whichever comes first. Never fails: errors stay in the
 * query cache for the blocks.
 */
export function preloadView(queries: readonly Promise<unknown>[], windowMs = PRELOAD_WINDOW_MS): Promise<null> {
    const timeout = new Promise<void>((resolve) => {
        setTimeout(resolve, windowMs);
    });
    return Promise.race([Promise.allSettled(queries), timeout]).then(() => null);
}
