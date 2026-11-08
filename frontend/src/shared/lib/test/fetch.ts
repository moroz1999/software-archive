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

/** A one-page backend page response holding the given items. */
export function pageResponse(items: readonly unknown[]): unknown {
    return {content: items, page: {size: 20, number: 0, totalElements: items.length, totalPages: 1}};
}

/** An empty successful response, as for an update or a delete. */
export function noContent(): () => Response {
    return () => new Response(null, {status: 204});
}
