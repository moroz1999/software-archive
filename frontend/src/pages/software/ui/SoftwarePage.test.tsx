import {afterEach, describe, expect, it, vi} from 'vitest';
import {render, screen} from '@testing-library/react';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {json, pageResponse, stubFetch} from '../../../shared/lib/test';
import {preloadSoftwarePage, SoftwarePage} from '../index';

const ALPHA = {id: '00000000-0000-0000-0001-000000000001', title: 'Fixture Software Alpha', releaseDate: '1985-03-02'};

function renderRoute() {
    const router = createMemoryRouter([
        {path: '/', loader: preloadSoftwarePage, Component: SoftwarePage, HydrateFallback: () => null},
    ]);
    render(<RouterProvider router={router}/>);
}

describe(SoftwarePage, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('shows the software list under the page title', async () => {
        stubFetch(json(pageResponse([ALPHA])));

        renderRoute();

        await expect(screen.findByRole('heading', {level: 1, name: 'Software'})).resolves.toBeInTheDocument();
        await expect(screen.findByRole('article', {name: 'Fixture Software Alpha'})).resolves.toBeInTheDocument();
    });

    it('offers to add software and leads from a card to its editing', async () => {
        stubFetch(json(pageResponse([ALPHA])));

        renderRoute();

        await expect(screen.findByRole('link', {name: 'Add software'})).resolves.toHaveAttribute('href', '/software/new');
        await expect(screen.findByRole('link', {name: 'Fixture Software Alpha'})).resolves.toHaveAttribute('href', `/software/${ALPHA.id}/edit`);
    });

    it('appears with its data and no skeleton when the preload makes it in time', async () => {
        stubFetch(json(pageResponse([ALPHA])));

        renderRoute();

        await screen.findByRole('heading', {level: 1, name: 'Software'});

        expect(screen.getByRole('article', {name: 'Fixture Software Alpha'})).toBeInTheDocument();
        expect(screen.queryByTestId('software-list-skeleton')).not.toBeInTheDocument();
    });

    it('appears with a skeleton when the preload takes longer than the window', async () => {
        let respond: (response: Response) => void = () => undefined;
        const fetchMock = vi.fn<typeof fetch>(() => new Promise((resolve) => {
            respond = resolve;
        }));
        vi.stubGlobal('fetch', fetchMock);

        renderRoute();

        await expect(screen.findByTestId('software-list-skeleton', {}, {timeout: 1000})).resolves.toBeInTheDocument();

        respond(json(pageResponse([ALPHA]))());

        await expect(screen.findByRole('article', {name: 'Fixture Software Alpha'})).resolves.toBeInTheDocument();
        expect(fetchMock).toHaveBeenCalledExactlyOnceWith('/api/software?page=0&size=20', expect.anything());
    });
});
