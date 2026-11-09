import type {ReleaseType} from '../model/release';

const RELEASE_TYPE_TEXTS: Readonly<Record<ReleaseType, string>> = {
    original: 'Original',
    rerelease: 'Rerelease',
    adaptation: 'Adaptation',
    localization: 'Localization',
    bugfix: 'Bugfix',
    mod: 'Mod',
    crack: 'Crack',
    mia: 'Missing in action',
    corrupted: 'Corrupted',
    compilation: 'Compilation',
    incomplete: 'Incomplete',
    demoversion: 'Demo version',
    unknown: 'Unknown',
};

/** The release type in words. */
export function releaseTypeText(type: ReleaseType): string {
    return RELEASE_TYPE_TEXTS[type];
}
