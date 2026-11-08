/** How long entering a route waits for the view's preloaded calls before showing skeletons (docs/data-loading.md). */
export const PRELOAD_WINDOW_MS = 300;

export type Load<T> = (signal: AbortSignal) => Promise<T>;

export type Settled<T> =
    | {readonly ok: true; readonly value: T}
    | {readonly ok: false; readonly error: unknown};

export interface Preloaded<T> {
    readonly promise: Promise<T>;
    /** Set once the call has finished. */
    settled: Settled<T> | undefined;
}

/** A logical call of a view that its route can start before the view renders. */
export interface ViewRequest<T> {
    readonly load: Load<T>;
    /** Starts the call; resolves when it has finished, successfully or not. */
    readonly preload: () => Promise<void>;
    /** The preloaded call, for the block that shows it; available only right after the preload. */
    readonly take: () => Preloaded<T> | undefined;
}

export function viewRequest<T>(load: Load<T>): ViewRequest<T> {
    let current: Preloaded<T> | undefined;

    return {
        load,
        preload() {
            const entry: Preloaded<T> = {promise: load(new AbortController().signal), settled: undefined};
            current = entry;
            return entry.promise.then(
                (value) => {
                    entry.settled = {ok: true, value};
                },
                (error: unknown) => {
                    entry.settled = {ok: false, error};
                },
            );
        },
        take() {
            const entry = current;
            if (entry !== undefined) {
                // Kept for this render pass (StrictMode renders twice), then a remount loads afresh.
                setTimeout(() => {
                    if (current === entry) {
                        current = undefined;
                    }
                }, 0);
            }
            return entry;
        },
    };
}

/** View requests of one kind, one per key: the details of each software, by its id. */
export interface ViewRequestFamily<K, T> {
    readonly for: (key: K) => ViewRequest<T>;
}

export function viewRequestFamily<K, T>(load: (key: K, signal: AbortSignal) => Promise<T>): ViewRequestFamily<K, T> {
    const requests = new Map<K, ViewRequest<T>>();
    return {
        for(key) {
            let request = requests.get(key);
            if (request === undefined) {
                request = viewRequest((signal) => load(key, signal));
                requests.set(key, request);
            }
            return request;
        },
    };
}

/**
 * The route loader of a view: starts the calls of the blocks at the top of the view and waits until
 * they finish or the window is over, whichever comes first. Never fails: errors reach the blocks.
 */
export function preloadView(requests: readonly Pick<ViewRequest<unknown>, 'preload'>[], windowMs = PRELOAD_WINDOW_MS): Promise<null> {
    const all = Promise.all(requests.map((request) => request.preload()));
    const timeout = new Promise<void>((resolve) => {
        setTimeout(resolve, windowMs);
    });
    return Promise.race([all, timeout]).then(() => null);
}
