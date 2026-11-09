import {parsePlatformCode, type PlatformCode} from './platform';

const KEY = 'archive.platform';

/**
 * The platform chosen with the switch, kept in the browser (docs/features/platform-switch.md); null for all
 * platforms. The route loaders read it here, the blocks through the PlatformSelectionProvider.
 */
export function readSelectedPlatform(): PlatformCode | null {
    try {
        return parsePlatformCode(localStorage.getItem(KEY));
    } catch {
        // A browser may refuse its storage: then nothing was chosen.
        return null;
    }
}

/** Keeps the chosen platform in the browser, forgets it for all platforms; a browser refusing its storage keeps nothing. */
export function storeSelectedPlatform(platform: PlatformCode | null): void {
    try {
        if (platform === null) {
            localStorage.removeItem(KEY);
        } else {
            localStorage.setItem(KEY, platform);
        }
    } catch {
        // The choice still holds until the page is left.
    }
}
