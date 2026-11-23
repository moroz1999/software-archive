import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {createQueryClient} from '@/shared/api';
import {json, renderWithQueries, stubFetchAround} from '@/shared/lib/test';
import {loadLabelViewPage, preloadLabelViewPage} from '../index';

const ID = '00000000-0000-0000-0004-000000000001';
const GHOST = {id: ID, name: 'Ghost', owner: null, groups: [], locations: [], origins: [], added: {by: 'Zed', at: '2026-01-01T00:00:00Z'}, canChange: false, canMakeOwner: false, canEdit: false, canChangeOwner: false, canHandOver: false};

describe('LabelViewPage', () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('appears with the label when the preload makes it in time', async () => {
        const fetchMock = stubFetchAround(new Map([[`/api/labels/${ID}/software`, json([])], [`/api/labels/${ID}/releases`, json([])]]), json(GHOST));
        const queryClient = createQueryClient();
        const router = createMemoryRouter([
            {path: '/labels/:id', loader: preloadLabelViewPage(queryClient), lazy: {Component: loadLabelViewPage}, HydrateFallback: () => null},
        ], {initialEntries: [`/labels/${ID}`]});
        renderWithQueries(<RouterProvider router={router}/>, queryClient);

        await expect(screen.findByRole('heading', {level: 1, name: 'Ghost'})).resolves.toBeInTheDocument();
        expect(screen.queryByTestId('label-view-skeleton')).not.toBeInTheDocument();
        expect(fetchMock).toHaveBeenCalledExactlyOnceWith(`/api/labels/${ID}`, expect.anything());
    });
});
