import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen, within} from '@testing-library/react';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {createQueryClient} from '@/shared/api';
import {json, renderWithQueries, stubFetchRoutes} from '@/shared/lib/test';
import {loadManagementImportsPage, preloadManagementImportsPage} from '../index';

const SECTIONS = '/api/imports/vektor/sections';

function renderRoute() {
    const queryClient = createQueryClient();
    const router = createMemoryRouter([
        {path: '/management/imports', loader: preloadManagementImportsPage(queryClient), lazy: {Component: loadManagementImportsPage}, HydrateFallback: () => null},
    ], {initialEntries: ['/management/imports']});
    renderWithQueries(<RouterProvider router={router}/>, queryClient);
}

describe('ManagementImportsPage', () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('appears with the sections to import when the preload makes it in time', async () => {
        const fetchMock = stubFetchRoutes(new Map([[SECTIONS, json(['zagruzchiki'])], ['/api/imports/last-run', json({run: null})]]));

        renderRoute();

        await expect(screen.findByRole('heading', {level: 1, name: 'Imports'})).resolves.toBeInTheDocument();

        expect(screen.getByRole('option', {name: 'zagruzchiki'})).toBeInTheDocument();
        expect(fetchMock.mock.calls.filter(([url]) => url === SECTIONS)).toHaveLength(1);
    });

    it('shows the imports among the management sections', async () => {
        stubFetchRoutes(new Map([[SECTIONS, json(['zagruzchiki'])], ['/api/imports/last-run', json({run: null})]]));

        renderRoute();

        const management = within(await screen.findByRole('navigation', {name: 'Management'}));

        expect(management.getByRole('link', {name: 'Imports'})).toHaveAttribute('aria-current', 'page');
        expect(management.getAllByRole('link')).toHaveLength(4);
    });
});
