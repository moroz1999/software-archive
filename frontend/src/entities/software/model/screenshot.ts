import type {components} from '@/shared/api';

/** The galleries of pictures of software (docs/domain/software.md#pictures); a release has screenshots only. */
export type Gallery = 'screenshots' | 'maps' | 'covers' | 'adverts';

/** A screenshot of software or of a release and the address of its picture (docs/domain/software.md#screenshots). */
export type Screenshot = components['schemas']['ScreenshotDto'];

/** The kinds of pictures a screenshot may be, as browsers name them. */
export const SCREENSHOT_TYPES = ['image/png', 'image/jpeg', 'image/gif', 'image/webp'] as const;

/** The largest picture of a screenshot: 10 MB. */
export const SCREENSHOT_MAX_BYTES = 10 * 1024 * 1024;

/** Why a file cannot be a screenshot: not a picture of the kinds, or too large. */
export type ScreenshotFileProblem = 'format' | 'size';

/** What keeps the file from being a screenshot, checked before it is sent; null when nothing does. */
export function screenshotFileProblem(file: File): ScreenshotFileProblem | null {
    if (!SCREENSHOT_TYPES.some((type) => type === file.type)) {
        return 'format';
    }
    return file.size > SCREENSHOT_MAX_BYTES ? 'size' : null;
}

/** The screenshots with one of them moved a place back (-1) or forth (1); at an end it stays. */
export function movedScreenshot(ids: readonly string[], id: string, by: -1 | 1): readonly string[] {
    const from = ids.indexOf(id);
    const to = from + by;
    if (from < 0 || to < 0 || to >= ids.length) {
        return ids;
    }
    const moved = ids.filter((other) => other !== id);
    moved.splice(to, 0, id);
    return moved;
}
