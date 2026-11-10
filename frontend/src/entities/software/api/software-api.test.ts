import {afterEach, describe, expect, it, vi} from 'vitest';
import {SystemError} from '@/shared/api';
import {json, noContent, stubFetch} from '@/shared/lib/test';
import {toReleaseDate, toSoftwareId, toSoftwarePlatformCode, toSoftwareTitle} from '../model/software';
import {createSoftware, deleteSoftware, fetchSoftware, fetchSoftwareReleases, updateSoftware} from './software-api';

const ID = toSoftwareId('00000000-0000-0000-0001-000000000001');
const DRAFT = {
    title: toSoftwareTitle('Elite'),
    description: null,
    releaseDate: toReleaseDate('1985-09-20'),
    platform: toSoftwarePlatformCode('vector'),
    hardware: ['vector06c', 'ay8910'],
};
const VECTOR = {code: 'vector', name: 'Vector'};
const VECTOR06C = {code: 'vector06c', name: 'Vector-06C', group: 'computers'};
const AY = {code: 'ay8910', name: 'AY-3-8910', group: 'sound'};
const RELEASE = '00000000-0000-0000-0007-000000000001';

describe(fetchSoftware, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('reads one software', async () => {
        const fetchMock = stubFetch(json({
            id: ID, title: 'Elite', description: 'Space trading', releaseDate: null, platform: VECTOR, hardware: [VECTOR06C],
        }));

        await expect(fetchSoftware(ID, new AbortController().signal)).resolves.toStrictEqual({
            id: ID, title: 'Elite', description: 'Space trading', releaseDate: null, platform: VECTOR, hardware: [VECTOR06C],
        });
        expect(fetchMock.mock.calls[0]?.[0]).toBe(`/api/software/${ID}`);
    });

    it('passes a not found error on', async () => {
        stubFetch(json({status: 404, code: 'software.not-found'}, 404));

        await expect(fetchSoftware(ID, new AbortController().signal)).rejects.toMatchObject({name: 'NotFoundError'});
    });

    it('fails with a system error on a malformed answer', async () => {
        stubFetch(json({id: ID, title: ''}));

        await expect(fetchSoftware(ID, new AbortController().signal)).rejects.toBeInstanceOf(SystemError);
    });

    it('fails with a system error on hardware of an unknown group', async () => {
        stubFetch(json({
            id: ID, title: 'Elite', description: null, releaseDate: null, platform: VECTOR, hardware: [{...VECTOR06C, group: 'video'}],
        }));

        await expect(fetchSoftware(ID, new AbortController().signal)).rejects.toBeInstanceOf(SystemError);
    });
});

describe(fetchSoftwareReleases, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('reads the releases of one software', async () => {
        const release = {
            id: RELEASE, title: 'Elite AY', version: '1.1', type: 'mod', releaseDate: '1990-03-01', description: null, hardware: [AY],
        };
        const fetchMock = stubFetch(json([release]));

        await expect(fetchSoftwareReleases(ID, new AbortController().signal)).resolves.toStrictEqual([release]);
        expect(fetchMock.mock.calls[0]?.[0]).toBe(`/api/software/${ID}/releases`);
    });

    it('fails with a system error on a release of an unknown type', async () => {
        stubFetch(json([{id: RELEASE, title: 'Elite', version: null, type: 'remix', releaseDate: null, description: null, hardware: []}]));

        await expect(fetchSoftwareReleases(ID, new AbortController().signal)).rejects.toBeInstanceOf(SystemError);
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
        expect(fetchMock.mock.calls[0]?.[1]).toMatchObject({
            method: 'POST',
            body: '{"title":"Elite","description":null,"releaseDate":"1985-09-20","platform":"vector","hardware":["vector06c","ay8910"]}',
        });
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
        expect(fetchMock.mock.calls[0]?.[1]).toMatchObject({
            method: 'PUT',
            body: '{"title":"Elite","description":null,"releaseDate":"1985-09-20","platform":"vector","hardware":["vector06c","ay8910"]}',
        });
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
