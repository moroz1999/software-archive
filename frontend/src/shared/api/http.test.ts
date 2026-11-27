import {afterEach, describe, expect, it, vi} from 'vitest';
import {api} from './http';
import {setContentLanguage} from './content-language';
import {AuthenticationRequiredError, BusinessError, NotFoundError, SystemError} from './errors';
import {apiError} from './transport';

const ID = '00000000-0000-0000-0001-000000000001';

/** Every call is answered so; a fresh answer each time, as a body is read once. */
function respond(status: number, body: unknown, contentType = 'application/problem+json') {
    const fetchMock = vi.fn<typeof fetch>(() => Promise.resolve(
        new Response(body === null ? null : typeof body === 'string' ? body : JSON.stringify(body), {status, headers: {'Content-Type': contentType}}),
    ));
    vi.stubGlobal('fetch', fetchMock);
    return fetchMock;
}

/** The URL of the n-th call, as the page sends it: the path with its query. */
function sentUrl(fetchMock: ReturnType<typeof respond>, n = 0): string | undefined {
    const input = fetchMock.mock.calls[n]?.[0];
    if (!(input instanceof Request)) {
        return undefined;
    }
    const url = new URL(input.url);
    return `${url.pathname}${url.search}`;
}

/** The platforms, failing as the callers of the client fail: with the error of the answer. */
async function platforms(): Promise<unknown> {
    const {data, error, response} = await api.GET('/api/platforms');
    if (error !== undefined) {
        throw apiError(response, error);
    }
    return data;
}

describe('the API client', () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('returns the body of a successful answer', async () => {
        respond(200, [{code: 'vector', name: 'Vector'}], 'application/json');

        await expect(platforms()).resolves.toStrictEqual([{code: 'vector', name: 'Vector'}]);
    });

    it('turns a 404 with a problem code into a not found error', async () => {
        respond(404, {status: 404, code: 'software.not-found'});

        const request = platforms();

        await expect(request).rejects.toBeInstanceOf(NotFoundError);
        await expect(request).rejects.toMatchObject({code: 'software.not-found'});
    });

    it.each([409, 422])('turns %i with a problem code into a business error with the errors of the fields', async (status) => {
        respond(status, {status, code: 'software.title-taken', errors: [{field: 'title', code: 'software.title-taken'}]});

        const request = platforms();

        await expect(request).rejects.toBeInstanceOf(BusinessError);
        await expect(request).rejects.toMatchObject({status, code: 'software.title-taken', fieldErrors: [{field: 'title', code: 'software.title-taken'}]});
    });

    it('turns a 403 with a problem code into a business error of its status', async () => {
        respond(403, {status: 403, code: 'access.denied'});

        await expect(platforms()).rejects.toMatchObject({name: 'BusinessError', status: 403, code: 'access.denied'});
    });

    it('fails a call that wants a signed-in user, once the session cannot be refreshed, with a business error the form shows', async () => {
        respond(401, {status: 401, code: 'auth.required'});

        const error = await platforms().catch((caught: unknown) => caught);

        expect(error).toBeInstanceOf(AuthenticationRequiredError);
        expect(error).toBeInstanceOf(BusinessError);
        expect(error).toMatchObject({status: 401, code: 'auth.required'});
    });

    it('treats a 404 without a problem code as a system error', async () => {
        respond(404, {status: 404, title: 'Not Found'});

        await expect(platforms()).rejects.toMatchObject({name: 'SystemError', kind: 'server'});
    });

    it.each([400, 500, 503])('turns %i into a system error', async (status) => {
        respond(status, {status, code: 'whatever'});

        const request = platforms();

        await expect(request).rejects.toBeInstanceOf(SystemError);
        await expect(request).rejects.toMatchObject({kind: 'server'});
    });

    it('turns a network failure into a system error', async () => {
        vi.stubGlobal('fetch', vi.fn<typeof fetch>().mockRejectedValue(new TypeError('Failed to fetch')));

        await expect(platforms()).rejects.toMatchObject({name: 'SystemError', kind: 'network'});
    });

    it('fills the path parameters into the URL, encoded', async () => {
        const fetchMock = respond(200, {}, 'application/json');

        await api.GET('/api/software/{id}', {params: {path: {id: 'a/b c'}}});

        expect(sentUrl(fetchMock)).toBe('/api/software/a%2Fb%20c');
    });

    it('sends the query parameters given and leaves out the ones given as nothing', async () => {
        const fetchMock = respond(200, {content: [], page: {size: 20, number: 0, totalElements: 0, totalPages: 0}}, 'application/json');

        await api.GET('/api/software/latest', {params: {query: {platform: 'zx spectrum'}}});
        await api.GET('/api/software/latest', {params: {query: {platform: null}}});

        expect([sentUrl(fetchMock, 0), sentUrl(fetchMock, 1)]).toStrictEqual(['/api/software/latest?platform=zx%20spectrum', '/api/software/latest']);
    });

    it('fails on a successful body it cannot read, which the query client takes for a broken answer', async () => {
        respond(200, '{broken', 'application/json');

        await expect(platforms()).rejects.toBeInstanceOf(SyntaxError);
    });
});

describe(setContentLanguage, () => {
    afterEach(() => {
        setContentLanguage('en');
        vi.unstubAllGlobals();
    });

    it('asks every call for the archive content in the language chosen', async () => {
        const fetchMock = respond(204, null, 'application/json');

        setContentLanguage('ru');
        await api.GET('/api/platforms');
        await api.POST('/api/auth/resend-confirmation', {body: {login: 'zed', password: 'secret', language: 'ru'}});
        setContentLanguage('en');
        await api.PUT('/api/software/{softwareId}/screenshots/order', {params: {path: {softwareId: ID}}, body: {order: []}});

        const asked = fetchMock.mock.calls.map(([input]) => (input instanceof Request ? input.headers.get('Accept-Language') : null));

        expect(asked).toStrictEqual(['ru', 'ru', 'en']);
    });
});
