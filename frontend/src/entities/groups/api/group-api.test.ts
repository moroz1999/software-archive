import {afterEach, describe, expect, it, vi} from 'vitest';
import {SystemError} from '@/shared/api';
import {json, stubFetch} from '@/shared/lib/test';
import {toGroupId} from '../model/group';
import {fetchGroup} from './group-api';

const ID = toGroupId('00000000-0000-0000-0005-000000000001');
const CREW = {
    id: ID,
    name: 'Crew',
    groupName: 'Crew Ltd',
    notes: 'Since 1990',
    types: ['demogroup'],
    labels: [{id: '00000000-0000-0000-0004-000000000001', name: 'Crew'}],
    locations: [{country: 'Finland', city: null, primary: true}],
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
