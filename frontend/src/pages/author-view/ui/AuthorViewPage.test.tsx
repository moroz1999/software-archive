import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {createQueryClient} from '@/shared/api';
import {json, renderWithQueries, stubFetch} from '@/shared/lib/test';
import {loadAuthorViewPage, preloadAuthorViewPage} from '../index';

const ID = '00000000-0000-0000-0003-000000000001';
const ZED = {id: ID, name: 'Zed', realName: null, notes: null, aliases: [], locations: [], groups: []};

describe('AuthorViewPage', () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('appears with the author when the preload makes it in time', async () => {
        const fetchMock = stubFetch(json(ZED));
        const queryClient = createQueryClient();
        const router = createMemoryRouter([
            {path: '/authors/:id', loader: preloadAuthorViewPage(queryClient), lazy: {Component: loadAuthorViewPage}, HydrateFallback: () => null},
        ], {initialEntries: [`/authors/${ID}`]});
        renderWithQueries(<RouterProvider router={router}/>, queryClient);

        await expect(screen.findByRole('heading', {level: 1, name: 'Zed'})).resolves.toBeInTheDocument();
        expect(screen.queryByTestId('author-view-skeleton')).not.toBeInTheDocument();
        expect(fetchMock).toHaveBeenCalledExactlyOnceWith(`/api/authors/${ID}`, expect.anything());
    });
});
