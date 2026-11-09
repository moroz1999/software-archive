import {afterEach, describe, expect, it, vi} from 'vitest';
import {getJson} from './http';
import {BusinessError, NotFoundError, SystemError} from './errors';

function respond(status: number, body: unknown, contentType = 'application/problem+json') {
    vi.stubGlobal('fetch', vi.fn<typeof fetch>().mockResolvedValue(
        new Response(typeof body === 'string' ? body : JSON.stringify(body), {status, headers: {'Content-Type': contentType}}),
    ));
}

describe(getJson, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('returns the parsed body of a successful response', async () => {
        respond(200, {value: 1}, 'application/json');

        await expect(getJson('/api/x')).resolves.toStrictEqual({value: 1});
    });

    it('turns a 404 with a problem code into a not found error', async () => {
        respond(404, {status: 404, code: 'software.not-found'});

        const request = getJson('/api/x');

        await expect(request).rejects.toBeInstanceOf(NotFoundError);
        await expect(request).rejects.toMatchObject({code: 'software.not-found'});
    });

    it.each([409, 422])('turns %i with a problem code into a business error', async (status) => {
        respond(status, {status, code: 'software.title-taken', errors: [{field: 'title', code: 'software.title-taken'}]});

        const request = getJson('/api/x');

        await expect(request).rejects.toBeInstanceOf(BusinessError);
        await expect(request).rejects.toMatchObject({status, code: 'software.title-taken', fieldErrors: [{field: 'title', code: 'software.title-taken'}]});
    });

    it('treats a 404 without a problem code as a system error', async () => {
        respond(404, {status: 404, title: 'Not Found'});

        await expect(getJson('/api/x')).rejects.toMatchObject({name: 'SystemError', kind: 'server'});
    });

    it.each([400, 500, 503])('turns %i into a system error', async (status) => {
        respond(status, {status, code: 'whatever'});

        const request = getJson('/api/x');

        await expect(request).rejects.toBeInstanceOf(SystemError);
        await expect(request).rejects.toMatchObject({kind: 'server'});
    });

    it('turns a network failure into a system error', async () => {
        vi.stubGlobal('fetch', vi.fn<typeof fetch>().mockRejectedValue(new TypeError('Failed to fetch')));

        await expect(getJson('/api/x')).rejects.toMatchObject({name: 'SystemError', kind: 'network'});
    });

    it('turns an unreadable successful body into a system error', async () => {
        respond(200, '{broken', 'application/json');

        await expect(getJson('/api/x')).rejects.toMatchObject({name: 'SystemError', kind: 'response'});
    });
});
