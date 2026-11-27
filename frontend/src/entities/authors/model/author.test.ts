import {describe, expect, it} from 'vitest';
import {NotFoundError} from '@/shared/api';
import {parseAuthorId, requireAuthorId} from './author';

const ID = '00000000-0000-0000-0003-000000000001';

describe(parseAuthorId, () => {
    it('reads an id from outside, null when it cannot be one', () => {
        expect(parseAuthorId(ID)).toBe(ID);
        expect(parseAuthorId('42')).toBeNull();
        expect(parseAuthorId(undefined)).toBeNull();
    });
});

describe(requireAuthorId, () => {
    it('treats an id that cannot be one as a page that does not exist', () => {
        expect(requireAuthorId(ID)).toBe(ID);
        expect(() => requireAuthorId('42')).toThrow(NotFoundError);
    });
});
