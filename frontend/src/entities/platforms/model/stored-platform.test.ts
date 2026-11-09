import {afterEach, describe, expect, it, vi} from 'vitest';
import {toPlatformCode} from './platform';
import {readSelectedPlatform, storeSelectedPlatform} from './stored-platform';

const KEY = 'archive.platform';

describe(readSelectedPlatform, () => {
    afterEach(() => {
        localStorage.clear();
        vi.restoreAllMocks();
    });

    it('reads all platforms when nothing was chosen', () => {
        expect(readSelectedPlatform()).toBeNull();
    });

    it('reads the platform kept in the browser', () => {
        localStorage.setItem(KEY, 'bk');

        expect(readSelectedPlatform()).toBe('bk');
    });

    it('reads all platforms for a kept value that is not a code', () => {
        localStorage.setItem(KEY, 'BK!');

        expect(readSelectedPlatform()).toBeNull();
    });

    it('reads all platforms when the browser refuses its storage', () => {
        vi.spyOn(Storage.prototype, 'getItem').mockImplementation(() => {
            throw new DOMException('denied', 'SecurityError');
        });

        expect(readSelectedPlatform()).toBeNull();
    });
});

describe(storeSelectedPlatform, () => {
    afterEach(() => {
        localStorage.clear();
        vi.restoreAllMocks();
    });

    it('keeps the chosen platform in the browser', () => {
        storeSelectedPlatform(toPlatformCode('msx'));

        expect(localStorage.getItem(KEY)).toBe('msx');
    });

    it('forgets the platform when all platforms are chosen', () => {
        localStorage.setItem(KEY, 'msx');

        storeSelectedPlatform(null);

        expect(localStorage.getItem(KEY)).toBeNull();
    });

    it('keeps nothing when the browser refuses its storage', () => {
        vi.spyOn(Storage.prototype, 'setItem').mockImplementation(() => {
            throw new DOMException('denied', 'QuotaExceededError');
        });

        expect(() => {
            storeSelectedPlatform(toPlatformCode('agat'));
        }).not.toThrow();
    });
});
