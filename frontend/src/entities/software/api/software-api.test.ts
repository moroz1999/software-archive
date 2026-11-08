import {afterEach, describe, expect, it, vi} from 'vitest';
import {BusinessError, SystemError} from '../../../shared/api';
import {json, noContent, stubFetch} from '../../../shared/lib/test';
import {toReleaseDate, toSoftwareId, toSoftwareTitle} from '../model/software';
import {createSoftware, deleteSoftware, fetchSoftware, updateSoftware} from './software-api';

const ID = toSoftwareId('00000000-0000-0000-0001-000000000001');
const DRAFT = {title: toSoftwareTitle('Elite'), description: null, releaseDate: toReleaseDate('1985-09-20')};

describe(fetchSoftware, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('reads one software', async () => {
        const fetchMock = stubFetch(json({id: ID, title: 'Elite', description: 'Space trading', releaseDate: null}));

        await expect(fetchSoftware(ID, new AbortController().signal)).resolves.toStrictEqual({
            id: ID, title: 'Elite', description: 'Space trading', releaseDate: null,
        });
        expect(fetchMock.mock.calls[0]?.[0]).toBe(`/api/software/${ID}`);
    });

    it('passes a not found error on as a business error', async () => {
        stubFetch(json({status: 404, code: 'software.not-found'}, 404));

        await expect(fetchSoftware(ID, new AbortController().signal)).rejects.toBeInstanceOf(BusinessError);
    });

    it('fails with a system error on a malformed answer', async () => {
        stubFetch(json({id: ID, title: ''}));

        await expect(fetchSoftware(ID, new AbortController().signal)).rejects.toBeInstanceOf(SystemError);
    });
});

describe(createSoftware, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('posts the draft and returns the new id', async () => {
        const fetchMock = stubFetch(json({id: ID}, 201));

        await expect(createSoftware(DRAFT)).resolves.toBe(ID);
        expect(fetchMock.mock.calls[0]?.[0]).toBe('/api/software');
        expect(fetchMock.mock.calls[0]?.[1]).toMatchObject({method: 'POST', body: '{"title":"Elite","description":null,"releaseDate":"1985-09-20"}'});
    });
});

describe(updateSoftware, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('puts the draft to the software', async () => {
        const fetchMock = stubFetch(noContent());

        await expect(updateSoftware(ID, DRAFT)).resolves.toBeUndefined();
        expect(fetchMock.mock.calls[0]?.[0]).toBe(`/api/software/${ID}`);
        expect(fetchMock.mock.calls[0]?.[1]).toMatchObject({method: 'PUT', body: '{"title":"Elite","description":null,"releaseDate":"1985-09-20"}'});
    });
});

describe(deleteSoftware, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('deletes the software', async () => {
        const fetchMock = stubFetch(noContent());

        await expect(deleteSoftware(ID)).resolves.toBeUndefined();
        expect(fetchMock.mock.calls[0]?.[0]).toBe(`/api/software/${ID}`);
        expect(fetchMock.mock.calls[0]?.[1]).toMatchObject({method: 'DELETE'});
    });
});
