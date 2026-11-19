import {afterEach, describe, expect, it, vi} from 'vitest';
import {deleteResource, postForm, postJson, postNoContent, putJson} from './http';
import {AuthenticationRequiredError, BusinessError, SystemError} from './errors';

function respond(status: number, body: unknown = null) {
    const fetchMock = vi.fn<typeof fetch>().mockResolvedValue(new Response(
        body === null ? null : JSON.stringify(body),
        {status, headers: {'Content-Type': status < 400 ? 'application/json' : 'application/problem+json'}},
    ));
    vi.stubGlobal('fetch', fetchMock);
    return fetchMock;
}

describe(postJson, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('posts the body as JSON and returns the parsed answer', async () => {
        const fetchMock = respond(201, {id: '1'});

        await expect(postJson('/api/x', {title: 'Elite'})).resolves.toStrictEqual({id: '1'});
        expect(fetchMock).toHaveBeenCalledExactlyOnceWith('/api/x', {
            method: 'POST',
            headers: {Accept: 'application/json', 'Content-Type': 'application/json', 'Accept-Language': 'en'},
            body: '{"title":"Elite"}',
        });
    });

    it('turns a 422 with a code into a business error', async () => {
        respond(422, {status: 422, code: 'software.title-taken'});

        await expect(postJson('/api/x', {})).rejects.toBeInstanceOf(BusinessError);
    });

    it('turns a 400 into a system error', async () => {
        respond(400, {status: 400, code: 'software.title-required'});

        await expect(postJson('/api/x', {})).rejects.toMatchObject({name: 'SystemError', kind: 'server'});
    });

    it('turns a network failure into a system error', async () => {
        vi.stubGlobal('fetch', vi.fn<typeof fetch>().mockRejectedValue(new TypeError('Failed to fetch')));

        await expect(postJson('/api/x', {})).rejects.toMatchObject({name: 'SystemError', kind: 'network'});
    });
});

describe(putJson, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('puts the body as JSON and expects no answer', async () => {
        const fetchMock = respond(204);

        await expect(putJson('/api/x/1', {title: 'Elite'})).resolves.toBeUndefined();
        expect(fetchMock).toHaveBeenCalledExactlyOnceWith('/api/x/1', {
            method: 'PUT',
            headers: {Accept: 'application/json', 'Content-Type': 'application/json', 'Accept-Language': 'en'},
            body: '{"title":"Elite"}',
        });
    });

    it('turns a 404 with a code into a not found error', async () => {
        respond(404, {status: 404, code: 'software.not-found'});

        await expect(putJson('/api/x/1', {})).rejects.toMatchObject({name: 'NotFoundError', code: 'software.not-found'});
    });
});

describe(deleteResource, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('deletes and expects no answer', async () => {
        const fetchMock = respond(204);

        await expect(deleteResource('/api/x/1')).resolves.toBeUndefined();
        expect(fetchMock).toHaveBeenCalledExactlyOnceWith('/api/x/1', {method: 'DELETE', headers: {Accept: 'application/json', 'Accept-Language': 'en'}});
    });

    it('turns a 500 into a system error', async () => {
        respond(500, {status: 500});

        await expect(deleteResource('/api/x/1')).rejects.toBeInstanceOf(SystemError);
    });
});

describe(postNoContent, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('posts the body as JSON and expects no answer', async () => {
        const fetchMock = respond(204);

        await expect(postNoContent('/api/x', {login: 'Zed'})).resolves.toBeUndefined();
        expect(fetchMock).toHaveBeenCalledExactlyOnceWith('/api/x', {
            method: 'POST',
            headers: {Accept: 'application/json', 'Content-Type': 'application/json', 'Accept-Language': 'en'},
            body: '{"login":"Zed"}',
        });
    });

    it('posts nothing when there is no body', async () => {
        const fetchMock = respond(204);

        await postNoContent('/api/x');

        expect(fetchMock).toHaveBeenCalledExactlyOnceWith('/api/x', {method: 'POST', headers: {Accept: 'application/json', 'Accept-Language': 'en'}});
    });
});

describe(postForm, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('posts the form as it is, leaving its content type to the browser, and returns the JSON answer', async () => {
        const fetchMock = respond(201, [{id: '1'}]);
        const form = new FormData();
        form.append('files', new File(['png'], 'shot.png', {type: 'image/png'}));

        await expect(postForm('/api/x', form)).resolves.toStrictEqual([{id: '1'}]);
        expect(fetchMock).toHaveBeenCalledExactlyOnceWith('/api/x', {method: 'POST', headers: {Accept: 'application/json', 'Accept-Language': 'en'}, body: form});
    });

    it('turns a 422 with a code into a business error', async () => {
        respond(422, {status: 422, code: 'screenshot.format-unsupported', errors: [{field: 'files', code: 'screenshot.format-unsupported'}]});

        await expect(postForm('/api/x', new FormData())).rejects.toMatchObject({name: 'BusinessError', status: 422, code: 'screenshot.format-unsupported'});
    });
});

describe('a refused write', () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('turns a 403 with a code into a business error of its status', async () => {
        respond(403, {status: 403, code: 'access.denied'});

        await expect(postJson('/api/x', {})).rejects.toMatchObject({name: 'BusinessError', status: 403, code: 'access.denied'});
    });

    it('fails a write that wants a signed-in user with a business error the form shows', async () => {
        respond(401, {status: 401, code: 'auth.required'});

        const error = await postJson('/api/x', {}).catch((caught: unknown) => caught);

        expect(error).toBeInstanceOf(AuthenticationRequiredError);
        expect(error).toBeInstanceOf(BusinessError);
        expect(error).toMatchObject({status: 401, code: 'auth.required'});
    });
});
