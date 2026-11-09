import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {createQueryClient} from '@/shared/api';
import {json, pageResponse, renderWithQueries, stubFetch} from '@/shared/lib/test';
import {loadHomePage, preloadHomePage} from '../index';

const ELITE = {id: '00000000-0000-0000-0001-000000000001', title: 'Elite', releaseDate: '1984-09-20'};

describe('HomePage', () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('appears with the latest software when the preload makes it in time', async () => {
        const fetchMock = stubFetch(json(pageResponse([ELITE], {size: 12})));
        const queryClient = createQueryClient();
        const router = createMemoryRouter([
            {path: '/', loader: preloadHomePage(queryClient), lazy: {Component: loadHomePage}, HydrateFallback: () => null},
        ], {initialEntries: ['/']});
        renderWithQueries(<RouterProvider router={router}/>, queryClient);

        await expect(screen.findByRole('heading', {level: 1, name: 'Recently added'})).resolves.toBeInTheDocument();
        expect(screen.getByRole('article', {name: 'Elite'})).toBeInTheDocument();
        expect(screen.queryByTestId('latest-software-skeleton')).not.toBeInTheDocument();
        expect(fetchMock).toHaveBeenCalledExactlyOnceWith('/api/software/latest?size=12', expect.anything());
    });
});
