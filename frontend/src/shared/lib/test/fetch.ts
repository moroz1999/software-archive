import {vi} from 'vitest';

const SENT_FORM = Symbol('the form a request was made with');

/**
 * Makes every Request keep the form it was made with. A Request of Node reads no Blob of jsdom: a multipart body made in
 * a test would hold "undefined" for every part, though a browser sends them. The test reads the form itself then.
 * jsdom's FormData and Blob stay in place, as React makes the FormData of a form submitted.
 */
export function keepSentForms(): void {
    class FormKeepingRequest extends Request {
        readonly [SENT_FORM]: FormData | undefined;

        constructor(input: RequestInfo | URL, init?: RequestInit) {
            super(input, init);
            const body = init?.body ?? (input instanceof FormKeepingRequest ? input[SENT_FORM] : undefined);
            this[SENT_FORM] = body instanceof FormData ? body : undefined;
        }
    }
    globalThis.Request = FormKeepingRequest;
}

function sentForm(request: Request): FormData | undefined {
    return SENT_FORM in request && request[SENT_FORM] instanceof FormData ? request[SENT_FORM] : undefined;
}

/**
 * A request as the test reads it: the path with its query, and how it was sent — the method, the headers and the body, the text of a JSON one
 * or the form of a multipart one. The API client sends a Request; the mocks of a test see it in this shape.
 */
async function asSent(input: RequestInfo | URL, init?: RequestInit): Promise<[string, RequestInit]> {
    if (!(input instanceof Request)) {
        return [input instanceof URL ? input.href : input, init ?? {}];
    }
    const url = new URL(input.url);
    const body = input.body === null ? null : sentForm(input) ?? await input.text();
    return [`${url.pathname}${url.search}`, {method: input.method, headers: input.headers, body, signal: input.signal}];
}

/** The address a fetch is called with: a test's mocks see the path with its query. */
export function urlOf(input: RequestInfo | URL): string {
    return typeof input === 'string' ? input : input instanceof URL ? input.href : input.url;
}

/** Puts the mock in place of fetch, called with each request as the test reads it. */
export function stubGlobalFetch<T extends ReturnType<typeof vi.fn<typeof fetch>>>(fetchMock: T): T {
    vi.stubGlobal('fetch', async (input: RequestInfo | URL, init?: RequestInit) => {
        const [path, sent] = await asSent(input, init);
        return fetchMock(path, sent);
    });
    return fetchMock;
}

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
    return stubGlobalFetch(fetchMock);
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
        const url = urlOf(input);
        const response = routes.get(url);
        return response === undefined ? Promise.reject(new TypeError(`Unexpected fetch: ${url}`)) : Promise.resolve(response());
    });
    return stubGlobalFetch(fetchMock);
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
    stubGlobalFetch(vi.fn<typeof fetch>((input, init) => {
        const url = urlOf(input);
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

/** The body of a backend page response; a response with more beside the page spreads it. */
export interface PageResponse {
    readonly content: readonly unknown[];
    readonly page: Required<PageMeta>;
}

/** A backend page response holding the given items; one page of 20 unless told otherwise. */
export function pageResponse(items: readonly unknown[], meta: PageMeta = {}): PageResponse {
    return {
        content: items,
        page: {size: 20, number: 0, totalElements: items.length, totalPages: 1, ...meta},
    };
}

/** An empty successful response, as for an update or a delete. */
export function noContent(): () => Response {
    return () => new Response(null, {status: 204});
}
