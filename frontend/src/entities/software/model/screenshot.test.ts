import {describe, expect, it} from 'vitest';
import {movedScreenshot, SCREENSHOT_MAX_BYTES, screenshotFileProblem, toScreenshotId} from './screenshot';

function file(type: string, size = 3): File {
    return new File([new Uint8Array(size)], 'shot', {type});
}

const A = toScreenshotId('00000000-0000-0000-0055-000000000001');
const B = toScreenshotId('00000000-0000-0000-0055-000000000002');
const C = toScreenshotId('00000000-0000-0000-0055-000000000003');

describe(toScreenshotId, () => {
    it('takes a uuid only', () => {
        expect(toScreenshotId(A)).toBe(A);
        expect(() => toScreenshotId('not-an-id')).toThrow(TypeError);
    });
});

describe(screenshotFileProblem, () => {
    it('takes PNG, JPEG, GIF and WebP pictures', () => {
        for (const type of ['image/png', 'image/jpeg', 'image/gif', 'image/webp']) {
            expect(screenshotFileProblem(file(type))).toBeNull();
        }
    });

    it('refuses any other kind of file', () => {
        expect(screenshotFileProblem(file('image/bmp'))).toBe('format');
        expect(screenshotFileProblem(file('text/plain'))).toBe('format');
        expect(screenshotFileProblem(file(''))).toBe('format');
    });

    it('takes a picture of 10 MB and no more', () => {
        expect(screenshotFileProblem(file('image/png', SCREENSHOT_MAX_BYTES))).toBeNull();
        expect(screenshotFileProblem(file('image/png', SCREENSHOT_MAX_BYTES + 1))).toBe('size');
    });
});

describe(movedScreenshot, () => {
    it('moves a screenshot a place back or forth', () => {
        expect(movedScreenshot([A, B, C], B, -1)).toStrictEqual([B, A, C]);
        expect(movedScreenshot([A, B, C], B, 1)).toStrictEqual([A, C, B]);
    });

    it('keeps one at its end where it is', () => {
        expect(movedScreenshot([A, B, C], A, -1)).toStrictEqual([A, B, C]);
        expect(movedScreenshot([A, B, C], C, 1)).toStrictEqual([A, B, C]);
    });
});
