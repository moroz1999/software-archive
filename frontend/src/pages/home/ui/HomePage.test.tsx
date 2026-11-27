import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {PlatformSelectionProvider} from '@/entities/platforms';
import {type components, createQueryClient} from '@/shared/api';
import {CurrentUserProvider} from '@/entities/users';
import {json, noContent, pageResponse, renderWithQueries, stubFetchRoutes} from '@/shared/lib/test';
import {loadHomePage, preloadHomePage} from '../index';

const ELITE = {id: '00000000-0000-0000-0001-000000000001', title: 'Elite', releaseDate: '1984-09-20', screenshots: [], madeBy: [], rating: 0, votesAmount: 0} satisfies components['schemas']['SoftwareListItemDto'];

describe('HomePage', () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('appears with the latest software when the preload makes it in time', async () => {
        const fetchMock = stubFetchRoutes(new Map([['/api/software/latest', json(pageResponse([ELITE], {size: 12}))], ['/api/auth/me', noContent()]]));
        const queryClient = createQueryClient();
        const router = createMemoryRouter([
            {path: '/', loader: preloadHomePage(queryClient), lazy: {Component: loadHomePage}, HydrateFallback: () => null},
        ], {initialEntries: ['/']});
        renderWithQueries(<CurrentUserProvider><PlatformSelectionProvider><RouterProvider router={router}/></PlatformSelectionProvider></CurrentUserProvider>, queryClient);

        await expect(screen.findByRole('heading', {level: 1, name: 'Recently added'})).resolves.toBeInTheDocument();
        expect(screen.getByRole('article', {name: 'Elite'})).toBeInTheDocument();
        expect(screen.queryByTestId('latest-software-skeleton')).not.toBeInTheDocument();
        expect(fetchMock.mock.calls.filter(([url]) => url === '/api/software/latest')).toHaveLength(1);
    });
});
