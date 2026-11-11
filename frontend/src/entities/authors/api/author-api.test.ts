import {afterEach, describe, expect, it, vi} from 'vitest';
import {SystemError} from '@/shared/api';
import {json, noContent, stubFetch} from '@/shared/lib/test';
import {toAuthorId} from '../model/author';
import {createAuthor, deleteAuthor, fetchAuthor, updateAuthor} from './author-api';

const ID = toAuthorId('00000000-0000-0000-0003-000000000001');
const ZED = {
    id: ID,
    name: 'Zed',
    realName: 'John Doe',
    notes: null,
    labels: [{id: '00000000-0000-0000-0004-000000000001', name: 'Zed', display: true}],
    locations: [{countryId: '00000000-0000-0000-0002-000000000001', country: 'Finland', cityId: null, city: null, primary: true}],
    groups: [
        {id: '00000000-0000-0000-0005-000000000001', name: 'Crew', as: 'Axe'},
        {id: '00000000-0000-0000-0005-000000000002', name: 'Band', as: null},
    ],
};

describe(fetchAuthor, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('reads one author', async () => {
        const fetchMock = stubFetch(json(ZED));

        await expect(fetchAuthor(ID, new AbortController().signal)).resolves.toStrictEqual(ZED);
        expect(fetchMock.mock.calls[0]?.[0]).toBe(`/api/authors/${ID}`);
    });

    it('passes a not found error on', async () => {
        stubFetch(json({status: 404, code: 'author.not-found'}, 404));

        await expect(fetchAuthor(ID, new AbortController().signal)).rejects.toMatchObject({name: 'NotFoundError'});
    });

    it('fails with a system error on a malformed answer', async () => {
        stubFetch(json({...ZED, labels: null}));

        await expect(fetchAuthor(ID, new AbortController().signal)).rejects.toBeInstanceOf(SystemError);
    });
});

const DRAFT = {
    realName: 'John Doe',
    notes: null,
    labels: [{id: '00000000-0000-0000-0004-000000000001', name: 'Zed', display: true}, {id: null, name: 'Axe', display: false}],
    locations: [{country: '00000000-0000-0000-0002-000000000001', city: null, primary: true}],
};
const BODY = '{"realName":"John Doe","notes":null,'
    + '"labels":[{"id":"00000000-0000-0000-0004-000000000001","name":"Zed","display":true},{"id":null,"name":"Axe","display":false}],'
    + '"locations":[{"country":"00000000-0000-0000-0002-000000000001","city":null,"primary":true}]}';

describe(createAuthor, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('posts the draft and returns the new id', async () => {
        const fetchMock = stubFetch(json({id: ID}, 201));

        await expect(createAuthor(DRAFT)).resolves.toBe(ID);
        expect(fetchMock.mock.calls[0]?.[0]).toBe('/api/authors');
        expect(fetchMock.mock.calls[0]?.[1]).toMatchObject({method: 'POST', body: BODY});
    });
});

describe(updateAuthor, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('puts the draft to the author', async () => {
        const fetchMock = stubFetch(noContent());

        await expect(updateAuthor(ID, DRAFT)).resolves.toBeUndefined();
        expect(fetchMock.mock.calls[0]?.[0]).toBe(`/api/authors/${ID}`);
        expect(fetchMock.mock.calls[0]?.[1]).toMatchObject({method: 'PUT', body: BODY});
    });
});

describe(deleteAuthor, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('deletes the author', async () => {
        const fetchMock = stubFetch(noContent());

        await expect(deleteAuthor(ID)).resolves.toBeUndefined();
        expect(fetchMock.mock.calls[0]?.[0]).toBe(`/api/authors/${ID}`);
        expect(fetchMock.mock.calls[0]?.[1]).toMatchObject({method: 'DELETE'});
    });
});
