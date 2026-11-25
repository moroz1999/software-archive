import {describe, expect, it} from 'vitest';
import {toUserGroup, toUserId} from './user';

describe(toUserId, () => {
    it('takes a UUID only', () => {
        expect(toUserId('00000000-0000-0000-0009-000000000001')).toBe('00000000-0000-0000-0009-000000000001');
        expect(() => toUserId('zed')).toThrow(TypeError);
    });
});

describe(toUserGroup, () => {
    it('knows the groups a user can be in', () => {
        expect(toUserGroup('admin')).toBe('admin');
        expect(toUserGroup('moderator')).toBe('moderator');
        expect(toUserGroup('member')).toBe('member');
        expect(toUserGroup('ai-assistant')).toBe('ai-assistant');
        expect(toUserGroup('owner')).toBeNull();
    });
});
