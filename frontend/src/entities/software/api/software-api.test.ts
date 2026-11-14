import {afterEach, describe, expect, it, vi} from 'vitest';
import {SystemError} from '@/shared/api';
import {json, noContent, stubFetch} from '@/shared/lib/test';
import {toReleaseDate, toSoftwareId, toSoftwarePlatformCode, toSoftwareTitle} from '../model/software';
import type {SoftwareDraft} from '../model/software-form';
import {createSoftware, deleteSoftware, fetchSoftware, fetchSoftwareReleases, updateSoftware} from './software-api';

const GAMES = '00000000-0000-0000-0006-000000000001';
const ACTION = '00000000-0000-0000-0006-000000000002';
const PUBLISHER = '00000000-0000-0000-0003-000000000001';
const DEVELOPER = '00000000-0000-0000-0004-000000000001';
const OWNER = '00000000-0000-0000-0003-000000000002';
const AUTHOR_LABEL = 'author-label';
const GROUP = 'group';
const ID = toSoftwareId('00000000-0000-0000-0001-000000000001');
const DRAFT = {
    title: toSoftwareTitle('Elite'),
    description: null,
    releaseDate: toReleaseDate('1985-09-20'),
    platform: toSoftwarePlatformCode('vector'),
    hardware: ['vector06c', 'ay8910'],
    categories: [GAMES],
    credits: [
        {id: PUBLISHER, type: AUTHOR_LABEL, title: 'J.P. — Jane Publisher', roles: ['publisher'], startDate: '', endDate: ''},
        {id: DEVELOPER, type: GROUP, title: 'Nebula Studio', roles: ['developer'], startDate: '', endDate: ''},
    ],
} satisfies SoftwareDraft;
const VECTOR = {code: 'vector', name: 'Vector'};
const VECTOR06C = {code: 'vector06c', name: 'Vector-06C', group: 'computers'};
const AY = {code: 'ay8910', name: 'AY-3-8910', group: 'sound'};
const RELEASE = '00000000-0000-0000-0007-000000000001';
const SHOT = '00000000-0000-0000-0055-000000000001';
const ARCHIVE = {fileName: 'elite-ay.zip', size: 1536, url: `/api/software/${ID}/releases/${RELEASE}/archive`};

describe(fetchSoftware, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('reads one software', async () => {
        const fetchMock = stubFetch(json({
            id: ID, title: 'Elite', description: 'Space trading', releaseDate: null, platform: VECTOR, hardware: [VECTOR06C],
            categories: [{id: ACTION, title: 'Action', branch: [{id: GAMES, title: 'Games'}, {id: ACTION, title: 'Action'}]}],
            credits: [
                {id: PUBLISHER, type: AUTHOR_LABEL, title: 'J.P. — Jane Publisher', ownerId: OWNER, roles: ['publisher']},
                {id: DEVELOPER, type: GROUP, title: 'Nebula Studio', ownerId: DEVELOPER, roles: ['developer']},
            ],
            sources: [{archive: 'vektor-06c.ru', url: 'https://vektor-06c.ru/programmnoe-obespechenie/arkadnye-igry/joe-blade-/'}],
            canChange: true,
        }));

        await expect(fetchSoftware(ID, new AbortController().signal)).resolves.toStrictEqual({
            id: ID, title: 'Elite', description: 'Space trading', releaseDate: null, platform: VECTOR, hardware: [VECTOR06C],
            categories: [{id: ACTION, title: 'Action', branch: [{id: GAMES, title: 'Games'}, {id: ACTION, title: 'Action'}]}],
            credits: [
                {id: PUBLISHER, type: AUTHOR_LABEL, title: 'J.P. — Jane Publisher', ownerId: OWNER, roles: ['publisher'], startDate: '', endDate: ''},
                {id: DEVELOPER, type: GROUP, title: 'Nebula Studio', ownerId: DEVELOPER, roles: ['developer'], startDate: '', endDate: ''},
            ],
            sources: [{archive: 'vektor-06c.ru', url: 'https://vektor-06c.ru/programmnoe-obespechenie/arkadnye-igry/joe-blade-/'}],
            canChange: true,
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

    it('fails with a system error on a malformed category', async () => {
        stubFetch(json({
            id: ID, title: 'Elite', description: null, releaseDate: null, platform: VECTOR, hardware: [], categories: [{id: GAMES}],
        }));

        await expect(fetchSoftware(ID, new AbortController().signal)).rejects.toBeInstanceOf(SystemError);
    });

    it('fails with a system error on a category without its branch', async () => {
        stubFetch(json({
            id: ID, title: 'Elite', description: null, releaseDate: null, platform: VECTOR, hardware: [], categories: [{id: GAMES, title: 'Games'}],
        }));

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

    it('reads the releases of one software with their screenshots', async () => {
        const release = {
            id: RELEASE, title: 'Elite AY', version: '1.1', type: 'mod', releaseDate: '1990-03-01', description: null, hardware: [AY], canChange: true,
            screenshots: [{id: SHOT, url: `/api/screenshots/${SHOT}`}],
            archive: ARCHIVE,
        };
        const fetchMock = stubFetch(json([release]));

        await expect(fetchSoftwareReleases(ID, new AbortController().signal)).resolves.toStrictEqual([release]);
        expect(fetchMock.mock.calls[0]?.[0]).toBe(`/api/software/${ID}/releases`);
    });

    it('reads a release without screenshots as having none', async () => {
        stubFetch(json([{id: RELEASE, title: 'Elite', version: null, type: 'original', releaseDate: null, description: null, hardware: [], canChange: false}]));

        await expect(fetchSoftwareReleases(ID, new AbortController().signal)).resolves.toMatchObject([{screenshots: []}]);
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
            body: '{"title":"Elite","description":null,"releaseDate":"1985-09-20","platform":"vector","hardware":["vector06c","ay8910"],"categories":["00000000-0000-0000-0006-000000000001"],"credits":[{"id":"00000000-0000-0000-0003-000000000001","type":"author-label","roles":["publisher"]},{"id":"00000000-0000-0000-0004-000000000001","type":"group","roles":["developer"]}]}',
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
            body: '{"title":"Elite","description":null,"releaseDate":"1985-09-20","platform":"vector","hardware":["vector06c","ay8910"],"categories":["00000000-0000-0000-0006-000000000001"],"credits":[{"id":"00000000-0000-0000-0003-000000000001","type":"author-label","roles":["publisher"]},{"id":"00000000-0000-0000-0004-000000000001","type":"group","roles":["developer"]}]}',
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
