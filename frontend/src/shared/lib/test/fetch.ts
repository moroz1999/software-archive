import {vi} from 'vitest';

/** Answers every fetch with the given responses in order; the last one repeats. */
export function stubFetch(...responses: (() => Response)[]): ReturnType<typeof vi.fn<typeof fetch>> {
    const fetchMock = vi.fn<typeof fetch>();
    responses.forEach((response, index) => {
        if (index === responses.length - 1) {
            fetchMock.mockImplementation(() => Promise.resolve(response()));
        } else {
            fetchMock.mockImplementationOnce(() => Promise.resolve(response()));
        }
    });
    vi.stubGlobal('fetch', fetchMock);
    return fetchMock;
}

export function json(body: unknown, status = 200): () => Response {
    return () => new Response(JSON.stringify(body), {
        status,
        headers: {'Content-Type': status < 400 ? 'application/json' : 'application/problem+json'},
    });
}

/**
 * Answers every fetch by its URL; the routes may change while the test runs. A URL without a route fails like
 * a network failure, so a call the test did not expect shows up as a system error.
 */
export function stubFetchRoutes(routes: Map<string, () => Response>): ReturnType<typeof vi.fn<typeof fetch>> {
    const fetchMock = vi.fn<typeof fetch>((input) => {
        const url = typeof input === 'string' ? input : input instanceof URL ? input.href : input.url;
        const response = routes.get(url);
        return response === undefined ? Promise.reject(new TypeError(`Unexpected fetch: ${url}`)) : Promise.resolve(response());
    });
    vi.stubGlobal('fetch', fetchMock);
    return fetchMock;
}

/**
 * Answers the fixed routes by their URL whenever they are asked, such as what every page loads, and every other fetch
 * with the given responses in order, the last one repeating. Returns the mock of those other fetches only.
 */
export function stubFetchAround(fixed: Map<string, () => Response>, ...responses: (() => Response)[]): ReturnType<typeof vi.fn<typeof fetch>> {
    const others = vi.fn<typeof fetch>();
    responses.forEach((response, index) => {
        if (index === responses.length - 1) {
            others.mockImplementation(() => Promise.resolve(response()));
        } else {
            others.mockImplementationOnce(() => Promise.resolve(response()));
        }
    });
    vi.stubGlobal('fetch', vi.fn<typeof fetch>((input, init) => {
        const url = typeof input === 'string' ? input : input instanceof URL ? input.href : input.url;
        const response = fixed.get(url);
        return response === undefined ? others(input, init) : Promise.resolve(response());
    }));
    return others;
}

export interface PageMeta {
    readonly size?: number;
    readonly number?: number;
    readonly totalElements?: number;
    readonly totalPages?: number;
}

/** A backend page response holding the given items; one page of 20 unless told otherwise. */
export function pageResponse(items: readonly unknown[], meta: PageMeta = {}): unknown {
    return {
        content: items,
        page: {size: 20, number: 0, totalElements: items.length, totalPages: 1, ...meta},
    };
}

/** An empty successful response, as for an update or a delete. */
export function noContent(): () => Response {
    return () => new Response(null, {status: 204});
}
