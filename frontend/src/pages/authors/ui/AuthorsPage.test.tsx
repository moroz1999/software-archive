import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {createQueryClient} from '@/shared/api';
import {json, pageResponse, renderWithQueries, stubFetchRoutes} from '@/shared/lib/test';
import {loadAuthorsPage, preloadAuthorsPage} from '../index';

const ZED = '00000000-0000-0000-0003-000000000001';
const FINLAND = '00000000-0000-0000-0002-000000000001';

function renderRoute(path: string) {
    const queryClient = createQueryClient();
    const router = createMemoryRouter([
        {path: '/authors', loader: preloadAuthorsPage(queryClient), lazy: {Component: loadAuthorsPage}, HydrateFallback: () => null},
    ], {initialEntries: [path]});
    renderWithQueries(<RouterProvider router={router}/>, queryClient);
}

describe('AuthorsPage', () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('preloads the catalog of the address, the countries and the cities of the chosen country', async () => {
        const fetchMock = stubFetchRoutes(new Map([
            [`/api/authors?page=0&size=100&letter=Z&country=${FINLAND}`, json(pageResponse([{personId: ZED, name: 'Zed', labelOf: null}], {size: 100}))],
            ['/api/countries', json([{id: FINLAND, name: 'Finland'}])],
            [`/api/countries/${FINLAND}/cities`, json([])],
        ]));

        renderRoute(`/authors?letter=Z&country=${FINLAND}`);

        await expect(screen.findByRole('heading', {level: 1, name: 'Authors'})).resolves.toBeInTheDocument();
        expect(screen.getByRole('link', {name: 'Zed'})).toBeInTheDocument();
        expect(screen.queryByTestId('author-catalog-skeleton')).not.toBeInTheDocument();
        expect(fetchMock).toHaveBeenCalledTimes(3);
    });
});
