import {describe, expect, it} from 'vitest';
import {requireReleaseId} from './release';

describe(requireReleaseId, () => {
    it('takes the id of a release from the URL', () => {
        expect(requireReleaseId('00000000-0000-0000-0007-000000000002')).toBe('00000000-0000-0000-0007-000000000002');
    });

    it.each([undefined, 'not-a-uuid'])('treats %s as a page that does not exist', (raw) => {
        expect(() => requireReleaseId(raw)).toThrow(expect.objectContaining({name: 'NotFoundError', code: 'release.not-found'}));
    });
});
