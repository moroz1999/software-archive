import {afterEach, describe, expect, it, vi} from 'vitest';
import {SystemError} from '@/shared/api';
import {json, noContent, stubFetch} from '@/shared/lib/test';
import {toReleaseId} from '../model/release';
import {toReleaseDate, toSoftwareId, toSoftwarePlatformCode} from '../model/software';
import {createRelease, deleteRelease, fetchPlatformHardware, fetchSoftwareRelease, updateRelease} from './release-api';

const SOFTWARE = toSoftwareId('00000000-0000-0000-0001-000000000001');
const RELEASE = toReleaseId('00000000-0000-0000-0007-000000000002');
const AY = {code: 'ay8910', name: 'AY-3-8910', group: 'sound'};
const DRAFT = {title: null, version: '1.1', type: 'mod' as const, releaseDate: toReleaseDate('1990-03-01'), description: null, hardware: ['ay8910']};
const RELEASE_PATH = `/api/software/${SOFTWARE}/releases/${RELEASE}`;

describe(fetchSoftwareRelease, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('reads a release as it states itself', async () => {
        const fetchMock = stubFetch(json({
            id: RELEASE, title: null, version: '1.1', type: 'mod', releaseDate: '1990-03-01', description: 'With AY music', hardware: [AY],
        }));

        await expect(fetchSoftwareRelease(SOFTWARE, RELEASE, new AbortController().signal)).resolves.toStrictEqual({
            id: RELEASE, title: null, version: '1.1', type: 'mod', releaseDate: '1990-03-01', description: 'With AY music', hardware: [AY],
        });
        expect(fetchMock.mock.calls[0]?.[0]).toBe(RELEASE_PATH);
    });

    it('passes a not found error on', async () => {
        stubFetch(json({status: 404, code: 'release.not-found'}, 404));

        await expect(fetchSoftwareRelease(SOFTWARE, RELEASE, new AbortController().signal)).rejects.toMatchObject({name: 'NotFoundError'});
    });

    it('fails with a system error on an unknown type', async () => {
        stubFetch(json({id: RELEASE, title: null, version: null, type: 'beta', releaseDate: null, description: null, hardware: []}));

        await expect(fetchSoftwareRelease(SOFTWARE, RELEASE, new AbortController().signal)).rejects.toBeInstanceOf(SystemError);
    });
});

describe(createRelease, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('adds a release to its software and returns its id', async () => {
        const fetchMock = stubFetch(json({id: RELEASE}, 201));

        await expect(createRelease(SOFTWARE, DRAFT)).resolves.toBe(RELEASE);
        expect(fetchMock).toHaveBeenCalledExactlyOnceWith(`/api/software/${SOFTWARE}/releases`, expect.objectContaining({
            method: 'POST',
            body: '{"title":null,"version":"1.1","type":"mod","releaseDate":"1990-03-01","description":null,"hardware":["ay8910"]}',
        }));
    });
});

describe(updateRelease, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('saves a release', async () => {
        const fetchMock = stubFetch(noContent());

        await updateRelease(SOFTWARE, RELEASE, DRAFT);

        expect(fetchMock).toHaveBeenCalledExactlyOnceWith(RELEASE_PATH, expect.objectContaining({method: 'PUT'}));
    });
});

describe(deleteRelease, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('deletes a release', async () => {
        const fetchMock = stubFetch(noContent());

        await deleteRelease(SOFTWARE, RELEASE);

        expect(fetchMock).toHaveBeenCalledExactlyOnceWith(RELEASE_PATH, expect.objectContaining({method: 'DELETE'}));
    });
});

describe(fetchPlatformHardware, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('reads the hardware of a platform', async () => {
        const fetchMock = stubFetch(json([AY]));

        await expect(fetchPlatformHardware(toSoftwarePlatformCode('vector'), new AbortController().signal)).resolves.toStrictEqual([AY]);
        expect(fetchMock.mock.calls[0]?.[0]).toBe('/api/hardware?platform=vector');
    });
});
