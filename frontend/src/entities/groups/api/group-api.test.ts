import {afterEach, describe, expect, it, vi} from 'vitest';
import {SystemError} from '@/shared/api';
import {json, noContent, stubFetch} from '@/shared/lib/test';
import {toGroupId} from '../model/group';
import {createGroup, deleteGroup, fetchGroup, fetchGroupTypes, updateGroup} from './group-api';

const ID = toGroupId('00000000-0000-0000-0005-000000000001');
const CREW = {
    id: ID,
    name: 'Crew',
    groupName: 'Crew Ltd',
    notes: 'Since 1990',
    types: ['demogroup'],
    labels: [{id: '00000000-0000-0000-0004-000000000001', name: 'Crew', display: true}],
    locations: [{countryId: '00000000-0000-0000-0002-000000000001', country: 'Finland', cityId: null, city: null, primary: true}],
    members: [
        {name: 'хакер федя', personId: '00000000-0000-0000-0003-000000000001', personName: 'MASTERSOFT'},
        {name: 'Ghost', personId: null, personName: null},
    ],
    memberGroups: [{id: '00000000-0000-0000-0005-000000000002', name: 'Band'}],
    groups: [{id: '00000000-0000-0000-0005-000000000003', name: 'Alpha'}],
};

describe(fetchGroup, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('reads one group', async () => {
        const fetchMock = stubFetch(json(CREW));

        await expect(fetchGroup(ID, new AbortController().signal)).resolves.toStrictEqual(CREW);
        expect(fetchMock.mock.calls[0]?.[0]).toBe(`/api/groups/${ID}`);
    });

    it('passes a not found error on', async () => {
        stubFetch(json({status: 404, code: 'group.not-found'}, 404));

        await expect(fetchGroup(ID, new AbortController().signal)).rejects.toMatchObject({name: 'NotFoundError'});
    });

    it('fails with a system error on a malformed answer', async () => {
        stubFetch(json({...CREW, members: null}));

        await expect(fetchGroup(ID, new AbortController().signal)).rejects.toBeInstanceOf(SystemError);
    });
});

const DRAFT = {
    name: 'Crew Ltd',
    notes: null,
    types: ['demogroup'],
    labels: [{id: '00000000-0000-0000-0004-000000000001', name: 'Crew', display: true}, {id: null, name: 'CRW', display: false}],
    locations: [{country: '00000000-0000-0000-0002-000000000001', city: null, primary: true}],
};
const BODY = '{"name":"Crew Ltd","notes":null,"types":["demogroup"],'
    + '"labels":[{"id":"00000000-0000-0000-0004-000000000001","name":"Crew","display":true},{"id":null,"name":"CRW","display":false}],'
    + '"locations":[{"country":"00000000-0000-0000-0002-000000000001","city":null,"primary":true}]}';

describe(createGroup, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('posts the draft and returns the new id', async () => {
        const fetchMock = stubFetch(json({id: ID}, 201));

        await expect(createGroup(DRAFT)).resolves.toBe(ID);
        expect(fetchMock.mock.calls[0]?.[0]).toBe('/api/groups');
        expect(fetchMock.mock.calls[0]?.[1]).toMatchObject({method: 'POST', body: BODY});
    });
});

describe(updateGroup, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('puts the draft to the group', async () => {
        const fetchMock = stubFetch(noContent());

        await expect(updateGroup(ID, DRAFT)).resolves.toBeUndefined();
        expect(fetchMock.mock.calls[0]?.[0]).toBe(`/api/groups/${ID}`);
        expect(fetchMock.mock.calls[0]?.[1]).toMatchObject({method: 'PUT', body: BODY});
    });
});

describe(deleteGroup, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('deletes the group', async () => {
        const fetchMock = stubFetch(noContent());

        await expect(deleteGroup(ID)).resolves.toBeUndefined();
        expect(fetchMock.mock.calls[0]?.[0]).toBe(`/api/groups/${ID}`);
        expect(fetchMock.mock.calls[0]?.[1]).toMatchObject({method: 'DELETE'});
    });
});

describe(fetchGroupTypes, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('reads the reference book of group types', async () => {
        const fetchMock = stubFetch(json(['crack group', 'demogroup']));

        await expect(fetchGroupTypes(new AbortController().signal)).resolves.toStrictEqual(['crack group', 'demogroup']);
        expect(fetchMock.mock.calls[0]?.[0]).toBe('/api/group-types');
    });
});
