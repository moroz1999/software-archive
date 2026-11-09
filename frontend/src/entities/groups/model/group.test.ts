import {describe, expect, it} from 'vitest';
import {NotFoundError} from '@/shared/api';
import {parseGroupId, requireGroupId, toGroupId} from './group';

const ID = '00000000-0000-0000-0005-000000000001';

describe(toGroupId, () => {
    it('accepts a uuid', () => {
        expect(toGroupId(ID)).toBe(ID);
    });

    it('refuses anything else', () => {
        expect(() => toGroupId('42')).toThrow(TypeError);
    });
});

describe(parseGroupId, () => {
    it('reads an id from outside, null when it cannot be one', () => {
        expect(parseGroupId(ID)).toBe(ID);
        expect(parseGroupId('42')).toBeNull();
        expect(parseGroupId(undefined)).toBeNull();
    });
});

describe(requireGroupId, () => {
    it('treats an id that cannot be one as a page that does not exist', () => {
        expect(requireGroupId(ID)).toBe(ID);
        expect(() => requireGroupId('42')).toThrow(NotFoundError);
    });
});
