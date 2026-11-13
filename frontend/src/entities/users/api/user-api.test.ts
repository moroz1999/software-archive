import {describe, expect, it} from 'vitest';
import {readCurrentUser} from './user-api';

const ID = '00000000-0000-0000-0009-000000000001';

describe(readCurrentUser, () => {
    it('reads the signed-in user, without groups and abilities this interface does not know', () => {
        expect(readCurrentUser({
            id: ID, name: 'Zed', email: 'zed@example.com', groups: ['admin', 'member', 'owner'], abilities: ['add-entries', 'keep-categories', 'fly'],
        })).toStrictEqual({
            id: ID, name: 'Zed', email: 'zed@example.com', groups: ['admin', 'member'], abilities: ['add-entries', 'keep-categories'],
        });
    });

    it('fails as a broken response for a body it cannot read', () => {
        expect(() => readCurrentUser({id: 'zed', name: 'Zed'})).toThrow(expect.objectContaining({name: 'SystemError', kind: 'response'}));
    });
});
