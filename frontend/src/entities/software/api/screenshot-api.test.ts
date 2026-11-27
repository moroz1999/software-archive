import {afterEach, describe, expect, it, vi} from 'vitest';
import {BusinessError, NotFoundError} from '@/shared/api';
import {json, noContent, stubFetch} from '@/shared/lib/test';
import {deleteScreenshot, fetchScreenshots, reorderScreenshots, uploadScreenshots} from './screenshot-api';

const ELITE = '00000000-0000-0000-0001-000000000001';
const AY_MOD = '00000000-0000-0000-0007-000000000002';
const TITLE = '00000000-0000-0000-0055-000000000001';
const GAME = '00000000-0000-0000-0055-000000000002';
const SHOTS = [{id: TITLE, url: `/api/screenshots/${TITLE}`}, {id: GAME, url: `/api/screenshots/${GAME}`}];

function signal(): AbortSignal {
    return new AbortController().signal;
}

describe(fetchScreenshots, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('reads the screenshots of the software itself', async () => {
        const fetchMock = stubFetch(json(SHOTS));

        await expect(fetchScreenshots(ELITE, null, signal())).resolves.toStrictEqual(SHOTS);
        expect(fetchMock.mock.calls[0]?.[0]).toBe(`/api/software/${ELITE}/screenshots`);
    });

    it('reads the screenshots of a release', async () => {
        const fetchMock = stubFetch(json([]));

        await fetchScreenshots(ELITE, AY_MOD, signal());

        expect(fetchMock.mock.calls[0]?.[0]).toBe(`/api/software/${ELITE}/releases/${AY_MOD}/screenshots`);
    });

    it('reads, adds, reorders and deletes the pictures of a gallery of the software', async () => {
        const fetchMock = stubFetch(json(SHOTS), json(SHOTS, 201), noContent(), noContent());
        const png = new File(['png'], 'map.png', {type: 'image/png'});

        await fetchScreenshots(ELITE, null, signal(), 'maps');
        await uploadScreenshots(ELITE, null, [png], 'maps');
        await reorderScreenshots(ELITE, null, [GAME, TITLE], 'maps');
        await deleteScreenshot(ELITE, null, TITLE, 'maps');

        expect(fetchMock.mock.calls.map(([url]) => url)).toStrictEqual([
            `/api/software/${ELITE}/maps`, `/api/software/${ELITE}/maps`, `/api/software/${ELITE}/maps/order`, `/api/software/${ELITE}/maps/${TITLE}`,
        ]);
    });

    it('fails with not found for unknown software', async () => {
        stubFetch(json({status: 404, code: 'software.not-found'}, 404));

        await expect(fetchScreenshots(ELITE, null, signal())).rejects.toBeInstanceOf(NotFoundError);
    });
});

describe(uploadScreenshots, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('sends every file under the files of a form and returns the new screenshots', async () => {
        const fetchMock = stubFetch(json(SHOTS, 201));
        const png = new File(['png'], 'title.png', {type: 'image/png'});
        const gif = new File(['gif'], 'game.gif', {type: 'image/gif'});

        await expect(uploadScreenshots(ELITE, AY_MOD, [png, gif])).resolves.toStrictEqual(SHOTS);

        const [path, init] = fetchMock.mock.calls[0] ?? [];

        expect(path).toBe(`/api/software/${ELITE}/releases/${AY_MOD}/screenshots`);
        expect(init?.method).toBe('POST');

        const body = init?.body;

        expect(body).toBeInstanceOf(FormData);
        expect(body instanceof FormData ? body.getAll('files') : []).toStrictEqual([png, gif]);
    });

    it('fails with the refusal of the archive', async () => {
        stubFetch(json({status: 422, code: 'screenshot.too-large', errors: [{field: 'files', code: 'screenshot.too-large'}]}, 422));

        await expect(uploadScreenshots(ELITE, null, [new File(['x'], 'x.png')])).rejects.toBeInstanceOf(BusinessError);
    });
});

describe(deleteScreenshot, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('deletes the screenshot of the software or of the release', async () => {
        const fetchMock = stubFetch(noContent());

        await deleteScreenshot(ELITE, null, TITLE);
        await deleteScreenshot(ELITE, AY_MOD, GAME);

        expect(fetchMock).toHaveBeenNthCalledWith(1, `/api/software/${ELITE}/screenshots/${TITLE}`, expect.objectContaining({method: 'DELETE'}));
        expect(fetchMock).toHaveBeenNthCalledWith(2, `/api/software/${ELITE}/releases/${AY_MOD}/screenshots/${GAME}`, expect.objectContaining({method: 'DELETE'}));
    });
});

describe(reorderScreenshots, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('sends every screenshot in the new order', async () => {
        const fetchMock = stubFetch(noContent());

        await reorderScreenshots(ELITE, null, [GAME, TITLE]);

        expect(fetchMock).toHaveBeenCalledExactlyOnceWith(`/api/software/${ELITE}/screenshots/order`, expect.objectContaining({
            method: 'PUT',
            body: JSON.stringify({order: [GAME, TITLE]}),
        }));
    });

    it('fails with a conflict when the screenshots changed meanwhile', async () => {
        stubFetch(json({status: 409, code: 'screenshot.order-outdated'}, 409));

        await expect(reorderScreenshots(ELITE, null, [GAME])).rejects.toMatchObject({name: 'BusinessError', status: 409, code: 'screenshot.order-outdated'});
    });
});
