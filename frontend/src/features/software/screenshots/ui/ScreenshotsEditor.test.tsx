import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen, within} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {type Gallery} from '@/entities/software';
import {subscribeSystemErrors, type SystemErrorReport} from '@/shared/api';
import {json, noContent, renderWithQueries, stubFetch} from '@/shared/lib/test';
import {EditScreenshotsProvider} from '../model/EditScreenshotsProvider';
import {ScreenshotsEditor} from './ScreenshotsEditor';

const ID = '00000000-0000-0000-0001-000000000001';
const AY_MOD = '00000000-0000-0000-0007-000000000002';
const OF_SOFTWARE = `/api/software/${ID}/screenshots`;

function shot(n: number) {
    const id = `00000000-0000-0000-0055-00000000000${String(n)}`;
    return {id, url: `/api/screenshots/${id}`};
}

const A = shot(1);
const B = shot(2);
const C = shot(3);
const PNG = new File(['png'], 'title.png', {type: 'image/png'});
const GIF = new File(['gif'], 'game.gif', {type: 'image/gif'});

function renderEditor({enabled = true, release = false, gallery = 'screenshots'}: {readonly enabled?: boolean; readonly release?: boolean; readonly gallery?: Gallery} = {}) {
    renderWithQueries(
        <EditScreenshotsProvider softwareId={ID} releaseId={release ? AY_MOD : null} gallery={gallery} enabled={enabled}>
            <ScreenshotsEditor/>
        </EditScreenshotsProvider>,
    );
}

/** The pictures of the screenshots, in the order shown. */
async function shownUrls(): Promise<readonly (string | null)[]> {
    const list = await screen.findByRole('list', {name: 'Screenshots'});
    return within(list).getAllByRole('img').map((img) => img.getAttribute('src'));
}

function methodOf(fetchMock: ReturnType<typeof stubFetch>, call: number): string | undefined {
    return fetchMock.mock.calls[call]?.[1]?.method;
}

describe(ScreenshotsEditor, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('shows the screenshots in their order, each with its controls', async () => {
        stubFetch(json([A, B, C]));
        renderEditor();

        await expect(shownUrls()).resolves.toStrictEqual([A.url, B.url, C.url]);
        expect(screen.getByRole('img', {name: 'Screenshot 2'})).toBeInTheDocument();
        expect(screen.getByText('PNG, JPEG, GIF, WebP or BMP, up to 10 MB each.')).toBeInTheDocument();
    });

    it('moves no screenshot past an end', async () => {
        stubFetch(json([A, B, C]));
        renderEditor();
        await shownUrls();

        expect(screen.getByRole('button', {name: 'Move screenshot 1 left'})).toBeDisabled();
        expect(screen.getByRole('button', {name: 'Move screenshot 1 right'})).toBeEnabled();
        expect(screen.getByRole('button', {name: 'Move screenshot 3 right'})).toBeDisabled();
        expect(screen.getByRole('button', {name: 'Delete screenshot 2'})).toBeEnabled();
    });

    it('says so when there are none, offering to add some', async () => {
        stubFetch(json([]));
        renderEditor();

        await expect(screen.findByText('No screenshots yet.')).resolves.toBeInTheDocument();
        expect(screen.getByRole('button', {name: 'Add screenshots'})).toBeEnabled();
    });

    it('holds the place of the screenshots and asks for nothing until in view', () => {
        const fetchMock = stubFetch(json([A]));
        renderEditor({enabled: false});

        expect(screen.getByTestId('screenshots-editor-skeleton')).toBeInTheDocument();
        expect(fetchMock).not.toHaveBeenCalled();
    });

    it('uploads the chosen files and shows the screenshots anew', async () => {
        const fetchMock = stubFetch(json([A]), json([B, C], 201), json([A, B, C]));
        renderEditor();
        await shownUrls();

        await userEvent.upload(screen.getByLabelText('Add screenshots'), [PNG, GIF]);

        await vi.waitFor(async () => {
            await expect(shownUrls()).resolves.toStrictEqual([A.url, B.url, C.url]);
        });

        expect(fetchMock.mock.calls[1]?.[0]).toBe(OF_SOFTWARE);
        expect(methodOf(fetchMock, 1)).toBe('POST');

        const body = fetchMock.mock.calls[1]?.[1]?.body;

        expect(body instanceof FormData ? body.getAll('files') : null).toStrictEqual([PNG, GIF]);
    });

    it('changes the screenshots of a release', async () => {
        const fetchMock = stubFetch(json([A]), json([B], 201), json([A, B]));
        renderEditor({release: true});
        await shownUrls();

        await userEvent.upload(screen.getByLabelText('Add screenshots'), PNG);

        await vi.waitFor(() => {
            expect(fetchMock.mock.calls[1]?.[0]).toBe(`/api/software/${ID}/releases/${AY_MOD}/screenshots`);
        });

        expect(fetchMock.mock.calls[0]?.[0]).toBe(`/api/software/${ID}/releases/${AY_MOD}/screenshots`);
    });

    it('changes the pictures of a gallery of the software under the names of the gallery', async () => {
        const fetchMock = stubFetch(json([A]), json([B], 201), json([A, B]));
        renderEditor({gallery: 'maps'});

        const maps = await screen.findByRole('list', {name: 'Maps'});

        expect(within(maps).getByRole('img', {name: 'Map 1'})).toHaveAttribute('src', A.url);

        await userEvent.upload(screen.getByLabelText('Add maps'), PNG);

        await vi.waitFor(() => {
            expect(fetchMock.mock.calls[1]?.[0]).toBe(`/api/software/${ID}/maps`);
        });

        expect(fetchMock.mock.calls[0]?.[0]).toBe(`/api/software/${ID}/maps`);
    });

    it('deletes a picture of a gallery after asking under the names of the gallery', async () => {
        const fetchMock = stubFetch(json([A, B]), noContent(), json([B]));
        renderEditor({gallery: 'covers'});
        await screen.findByRole('list', {name: 'Covers'});

        await userEvent.click(screen.getByRole('button', {name: 'Delete cover 1'}));
        const dialog = screen.getByRole('alertdialog', {name: 'Delete cover'});

        expect(dialog).toHaveTextContent('Delete cover 1 from the archive?');

        await userEvent.click(within(dialog).getByRole('button', {name: 'Delete for good'}));

        await vi.waitFor(() => {
            expect(fetchMock.mock.calls[1]?.[0]).toBe(`/api/software/${ID}/covers/${A.id}`);
        });
    });

    it('refuses a file that cannot be a screenshot before sending anything', async () => {
        const fetchMock = stubFetch(json([A]));
        renderEditor();
        await shownUrls();

        await userEvent.upload(screen.getByLabelText('Add screenshots'), [PNG, new File(['tiff'], 'old.tiff', {type: 'image/tiff'})], {applyAccept: false});

        expect(screen.getByText('Only PNG, JPEG, GIF, WebP and BMP pictures can be screenshots.')).toBeInTheDocument();

        await userEvent.upload(screen.getByLabelText('Add screenshots'), new File([new Uint8Array(10 * 1024 * 1024 + 1)], 'huge.png', {type: 'image/png'}));

        expect(screen.getByText('A screenshot can be at most 10 MB.')).toBeInTheDocument();
        expect(fetchMock).toHaveBeenCalledTimes(1);
    });

    it('shows a refusal of the archive next to the screenshots', async () => {
        stubFetch(
            json([A]),
            json({status: 422, code: 'screenshot.format-unsupported', errors: [{field: 'files', code: 'screenshot.format-unsupported'}]}, 422),
        );
        renderEditor();
        await shownUrls();

        await userEvent.upload(screen.getByLabelText('Add screenshots'), PNG);

        await expect(screen.findByText('Only PNG, JPEG, GIF, WebP and BMP pictures can be screenshots.')).resolves.toBeInTheDocument();
    });

    it('tells a visitor to sign in and someone who may not that they may not', async () => {
        // The refusal of a visitor tries to refresh their session first, which is refused too.
        stubFetch(json([A]), json({status: 401, code: 'auth.required'}, 401), json({status: 401, code: 'auth.required'}, 401), json({status: 403, code: 'access.denied'}, 403));
        renderEditor();
        await shownUrls();

        await userEvent.upload(screen.getByLabelText('Add screenshots'), PNG);

        await expect(screen.findByText('Sign in to change the archive.')).resolves.toBeInTheDocument();

        await userEvent.upload(screen.getByLabelText('Add screenshots'), PNG);

        await expect(screen.findByText('Only whoever added this, or an administrator, may change it.')).resolves.toBeInTheDocument();
    });

    it('moves a screenshot by sending every screenshot in the new order', async () => {
        const fetchMock = stubFetch(json([A, B, C]), noContent(), json([B, A, C]));
        renderEditor();
        await shownUrls();

        await userEvent.click(screen.getByRole('button', {name: 'Move screenshot 2 left'}));

        await vi.waitFor(async () => {
            await expect(shownUrls()).resolves.toStrictEqual([B.url, A.url, C.url]);
        });

        expect(fetchMock.mock.calls[1]?.[0]).toBe(`${OF_SOFTWARE}/order`);
        expect(methodOf(fetchMock, 1)).toBe('PUT');
        expect(fetchMock.mock.calls[1]?.[1]?.body).toBe(JSON.stringify({order: [B.id, A.id, C.id]}));
    });

    it('shows the screenshots as they are now when they changed before the move', async () => {
        stubFetch(json([A, B]), json({status: 409, code: 'screenshot.order-outdated'}, 409), json([A, B, C]));
        renderEditor();
        await shownUrls();

        await userEvent.click(screen.getByRole('button', {name: 'Move screenshot 1 right'}));

        await expect(screen.findByText('The screenshots have changed meanwhile; here they are as they are now.')).resolves.toBeInTheDocument();

        await vi.waitFor(async () => {
            await expect(shownUrls()).resolves.toStrictEqual([A.url, B.url, C.url]);
        });
    });

    it('deletes a screenshot after asking', async () => {
        const fetchMock = stubFetch(json([A, B]), noContent(), json([B]));
        renderEditor();
        await shownUrls();

        await userEvent.click(screen.getByRole('button', {name: 'Delete screenshot 1'}));
        const dialog = screen.getByRole('alertdialog', {name: 'Delete screenshot'});

        expect(dialog).toHaveTextContent('Delete screenshot 1 from the archive?');

        await userEvent.click(within(dialog).getByRole('button', {name: 'Delete for good'}));

        await vi.waitFor(async () => {
            await expect(shownUrls()).resolves.toStrictEqual([B.url]);
        });

        expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument();
        expect(fetchMock.mock.calls[1]?.[0]).toBe(`${OF_SOFTWARE}/${A.id}`);
        expect(methodOf(fetchMock, 1)).toBe('DELETE');
    });

    it('deletes nothing when the deletion is cancelled', async () => {
        const fetchMock = stubFetch(json([A]));
        renderEditor();
        await shownUrls();

        await userEvent.click(screen.getByRole('button', {name: 'Delete screenshot 1'}));
        await userEvent.click(screen.getByRole('button', {name: 'Cancel'}));

        expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument();
        expect(fetchMock).toHaveBeenCalledTimes(1);
    });

    it('shows a refused deletion inside its confirmation', async () => {
        stubFetch(json([A]), json({status: 403, code: 'access.denied'}, 403));
        renderEditor();
        await shownUrls();

        await userEvent.click(screen.getByRole('button', {name: 'Delete screenshot 1'}));
        await userEvent.click(screen.getByRole('button', {name: 'Delete for good'}));

        await expect(within(screen.getByRole('alertdialog')).findByText('Only whoever added this, or an administrator, may change it.')).resolves.toBeInTheDocument();
    });

    it('locks the controls while a change runs', async () => {
        stubFetch(json([A, B]), () => new Response(new ReadableStream()));
        renderEditor();
        await shownUrls();

        await userEvent.click(screen.getByRole('button', {name: 'Move screenshot 1 right'}));

        await vi.waitFor(() => {
            expect(screen.getByRole('button', {name: 'Add screenshots'})).toBeDisabled();
        });

        expect(screen.getByRole('button', {name: 'Move screenshot 2 left'})).toBeDisabled();
        expect(screen.getByRole('button', {name: 'Delete screenshot 1'})).toBeDisabled();
    });

    it('hands a system error of a change to the error dialog', async () => {
        stubFetch(json([A]), json({status: 500}, 500));
        const reports: SystemErrorReport[] = [];
        const unsubscribe = subscribeSystemErrors((report) => reports.push(report));
        renderEditor();
        await shownUrls();

        await userEvent.upload(screen.getByLabelText('Add screenshots'), PNG);

        await vi.waitFor(() => {
            expect(reports).toHaveLength(1);
        });
        unsubscribe();
    });
});
