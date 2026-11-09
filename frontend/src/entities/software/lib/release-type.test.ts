import {describe, expect, it} from 'vitest';
import {RELEASE_TYPES, type ReleaseType} from '../model/release';
import {releaseTypeText} from './release-type';

describe(releaseTypeText, () => {
    it.each<[ReleaseType, string]>([
        ['original', 'Original'],
        ['rerelease', 'Rerelease'],
        ['adaptation', 'Adaptation'],
        ['localization', 'Localization'],
        ['bugfix', 'Bugfix'],
        ['mod', 'Mod'],
        ['crack', 'Crack'],
        ['mia', 'Missing in action'],
        ['corrupted', 'Corrupted'],
        ['compilation', 'Compilation'],
        ['incomplete', 'Incomplete'],
        ['demoversion', 'Demo version'],
        ['unknown', 'Unknown'],
    ])('names %s %j', (type, text) => {
        expect(releaseTypeText(type)).toBe(text);
    });

    it('names every type', () => {
        expect(RELEASE_TYPES).toHaveLength(13);
    });
});
