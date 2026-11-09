import {afterEach, describe, expect, it, vi} from 'vitest';
import {SystemError} from '@/shared/api';
import {json, stubFetch} from '@/shared/lib/test';
import {toAuthorId} from '../model/author';
import {fetchAuthor} from './author-api';

const ID = toAuthorId('00000000-0000-0000-0003-000000000001');
const ZED = {
    id: ID,
    name: 'Zed',
    realName: 'John Doe',
    notes: null,
    aliases: [{id: '00000000-0000-0000-0004-000000000001', name: 'Zed'}],
    locations: [{country: 'Finland', city: null, primary: true}],
    groups: [{id: '00000000-0000-0000-0005-000000000001', name: 'Crew'}],
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
        stubFetch(json({...ZED, aliases: null}));

        await expect(fetchAuthor(ID, new AbortController().signal)).rejects.toBeInstanceOf(SystemError);
    });
});
