import {afterEach, describe, expect, it, vi} from 'vitest';
import {json, noContent, stubFetch} from '@/shared/lib/test';
import {api} from './http';
import {multipartForm} from './multipart';

const ID = '00000000-0000-0000-0001-000000000001';

describe('a write through the API client', () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('sends the body as JSON and returns the answer', async () => {
        const fetchMock = stubFetch(json({rating: 4, votesAmount: 1, myVote: 4}));

        const {data} = await api.PUT('/api/software/{id}/vote', {params: {path: {id: ID}}, body: {value: 4}});

        expect(data).toStrictEqual({rating: 4, votesAmount: 1, myVote: 4});

        const [path, init] = fetchMock.mock.calls[0] ?? [];

        expect(path).toBe(`/api/software/${ID}/vote`);
        expect(init?.method).toBe('PUT');
        expect(new Headers(init?.headers).get('Content-Type')).toBe('application/json');
        expect(init?.body).toBe('{"value":4}');
    });

    it('takes an answer of no content for done', async () => {
        stubFetch(noContent());

        const {data, error} = await api.DELETE('/api/software/{id}', {params: {path: {id: ID}}});

        expect([data, error]).toStrictEqual([undefined, undefined]);
    });

    it('sends nothing when the call has no body', async () => {
        const fetchMock = stubFetch(noContent());

        await api.POST('/api/auth/logout');

        expect(fetchMock.mock.calls[0]?.[1]).toMatchObject({method: 'POST', body: null});
    });

    it('sends a multipart form of the files as they are and of anything else as a JSON part, leaving its content type to the browser', async () => {
        const fetchMock = stubFetch(json([], 201));
        const png = new File(['png'], 'title.png', {type: 'image/png'});
        const gif = new File(['gif'], 'game.gif', {type: 'image/gif'});

        await api.POST('/api/software/{softwareId}/screenshots', {params: {path: {softwareId: ID}}, body: {files: [png, gif]}, bodySerializer: multipartForm});

        const init = fetchMock.mock.calls[0]?.[1];
        const form = init?.body;

        expect(new Headers(init?.headers).get('Content-Type')).toMatch(/^multipart\/form-data; boundary=/u);
        expect(form instanceof FormData ? form.getAll('files') : null).toStrictEqual([png, gif]);
    });
});

describe(multipartForm, () => {
    it('puts a file as it is, each file of a list, a value as a JSON part and leaves out what is not given', async () => {
        const archive = new File(['zip'], 'elite.zip', {type: 'application/zip'});

        const form = multipartForm({release: {title: 'Elite'}, archive, shots: [archive], missing: undefined});

        expect(form.get('archive')).toBe(archive);
        expect(form.getAll('shots')).toStrictEqual([archive]);
        expect(form.has('missing')).toBe(false);

        const release = form.get('release');

        expect(release instanceof Blob ? [release.type, await release.text()] : null).toStrictEqual(['application/json', '{"title":"Elite"}']);
    });
});
