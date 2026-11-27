import {afterEach, describe, expect, it, vi} from 'vitest';
import type {components} from '@/shared/api';
import {json, noContent, stubFetch} from '@/shared/lib/test';
import type {SoftwareDraft} from '../model/software-form';
import {createSoftware, deleteSoftware, fetchSoftware, fetchSoftwareReleases, updateSoftware} from './software-api';

const GAMES = '00000000-0000-0000-0006-000000000001';
const ACTION = '00000000-0000-0000-0006-000000000002';
const PUBLISHER = '00000000-0000-0000-0003-000000000001';
const DEVELOPER = '00000000-0000-0000-0004-000000000001';
const OWNER = '00000000-0000-0000-0003-000000000002';
const AUTHOR_LABEL = 'author-label';
const GROUP = 'group';
const ID = '00000000-0000-0000-0001-000000000001';
const DRAFT = {
    title: 'Elite',
    description: null,
    releaseDate: '1985-09-20',
    platform: 'vector',
    hardware: ['vector06c', 'ay8910'],
    categories: [GAMES],
    preservation: 'donationware',
    instructions: 'Press Space to start',
    credits: [
        {id: PUBLISHER, ownerId: null, type: AUTHOR_LABEL, title: 'J.P. — Jane Publisher', roles: ['publisher']},
        {id: DEVELOPER, ownerId: null, type: GROUP, title: 'Nebula Studio', roles: ['developer']},
    ],
} satisfies SoftwareDraft;
const VECTOR = {code: 'vector', name: 'Vector'};
const VECTOR06C = {code: 'vector06c', name: 'Vector-06C', group: 'computers', groupName: 'Computers'};
const AY = {code: 'ay8910', name: 'AY-3-8910', group: 'sound', groupName: 'Sound'};
const RELEASE = '00000000-0000-0000-0007-000000000001';
const SHOT = '00000000-0000-0000-0055-000000000001';
const ARCHIVE = {fileName: 'elite-ay.zip', size: 1536, url: `/api/software/${ID}/releases/${RELEASE}/archive`};

type SoftwareDetailsDto = components['schemas']['SoftwareDetailsDto'];

/** Software as the archive answers it: nothing beside what the test gives. */
function details(fields: Partial<SoftwareDetailsDto> = {}): SoftwareDetailsDto {
    return {
        id: ID, title: 'Elite', description: null, releaseDate: null, platform: VECTOR, hardware: [], categories: [], credits: [], sources: [],
        compilationItems: [], compilations: [], preservation: 'missing-in-action', instructions: null, origins: [],
        added: {by: 'Zed', at: '2026-01-01T00:00:00Z'}, canChange: true, ...fields,
    };
}

describe(fetchSoftware, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('reads one software', async () => {
        const fetchMock = stubFetch(json(details({
            description: 'Space trading', hardware: [VECTOR06C],
            categories: [{id: ACTION, title: 'Action', branch: [{id: GAMES, title: 'Games'}, {id: ACTION, title: 'Action'}]}],
            credits: [
                {id: PUBLISHER, type: AUTHOR_LABEL, title: 'J.P. — Jane Publisher', ownerId: OWNER, roles: ['publisher']},
                {id: DEVELOPER, type: GROUP, title: 'Nebula Studio', ownerId: DEVELOPER, roles: ['developer']},
            ],
            sources: [{archive: 'vektor-06c.ru', url: 'https://vektor-06c.ru/programmnoe-obespechenie/arkadnye-igry/joe-blade-/'}],
            instructions: 'Press Space to start',
        })));

        await expect(fetchSoftware(ID, new AbortController().signal)).resolves.toStrictEqual({
            id: ID, title: 'Elite', description: 'Space trading', releaseDate: null, platform: VECTOR, hardware: [VECTOR06C],
            categories: [{id: ACTION, title: 'Action', branch: [{id: GAMES, title: 'Games'}, {id: ACTION, title: 'Action'}]}],
            credits: [
                {id: PUBLISHER, type: AUTHOR_LABEL, title: 'J.P. — Jane Publisher', ownerId: OWNER, roles: ['publisher']},
                {id: DEVELOPER, type: GROUP, title: 'Nebula Studio', ownerId: DEVELOPER, roles: ['developer']},
            ],
            sources: [{archive: 'vektor-06c.ru', url: 'https://vektor-06c.ru/programmnoe-obespechenie/arkadnye-igry/joe-blade-/'}],
            compilationItems: [],
            compilations: [],
            preservation: 'missing-in-action',
            instructions: 'Press Space to start',
            origins: [], added: {by: 'Zed', at: '2026-01-01T00:00:00Z'},
            canChange: true,
        });
        expect(fetchMock.mock.calls[0]?.[0]).toBe(`/api/software/${ID}`);
    });

    it('passes a not found error on', async () => {
        stubFetch(json({status: 404, code: 'software.not-found'}, 404));

        await expect(fetchSoftware(ID, new AbortController().signal)).rejects.toMatchObject({name: 'NotFoundError'});
    });




    it('reads hardware of a group the archive added, with the name of its group', async () => {
        stubFetch(json(details({hardware: [{code: 'gpu', name: 'GPU', group: 'video', groupName: 'Video'}]})));

        await expect(fetchSoftware(ID, new AbortController().signal)).resolves.toMatchObject({
            hardware: [{code: 'gpu', name: 'GPU', group: 'video', groupName: 'Video'}],
        });
    });

});

describe(fetchSoftwareReleases, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('reads the releases of one software with their screenshots and credits', async () => {
        const dto = {
            id: RELEASE, title: 'Elite AY', version: '1.1', type: 'mod', releaseDate: '1990-03-01', description: null, hardware: [AY], added: {by: 'Zed', at: '2026-01-01T00:00:00Z'}, canChange: true,
            screenshots: [{id: SHOT, url: `/api/screenshots/${SHOT}`}],
            archive: ARCHIVE,
            credits: [{id: RELEASE, type: 'group', title: 'Crew', ownerId: RELEASE, roles: ['release']}],
        };
        const fetchMock = stubFetch(json([dto]));

        await expect(fetchSoftwareReleases(ID, new AbortController().signal)).resolves.toStrictEqual([dto]);
        expect(fetchMock.mock.calls[0]?.[0]).toBe(`/api/software/${ID}/releases`);
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
            body: '{"title":"Elite","description":null,"releaseDate":"1985-09-20","platform":"vector","hardware":["vector06c","ay8910"],"categories":["00000000-0000-0000-0006-000000000001"],"credits":[{"id":"00000000-0000-0000-0003-000000000001","type":"author-label","roles":["publisher"]},{"id":"00000000-0000-0000-0004-000000000001","type":"group","roles":["developer"]}],"preservation":"donationware","instructions":"Press Space to start","compilationItemIds":null}',
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
            body: '{"title":"Elite","description":null,"releaseDate":"1985-09-20","platform":"vector","hardware":["vector06c","ay8910"],"categories":["00000000-0000-0000-0006-000000000001"],"credits":[{"id":"00000000-0000-0000-0003-000000000001","type":"author-label","roles":["publisher"]},{"id":"00000000-0000-0000-0004-000000000001","type":"group","roles":["developer"]}],"preservation":"donationware","instructions":"Press Space to start","compilationItemIds":null}',
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
