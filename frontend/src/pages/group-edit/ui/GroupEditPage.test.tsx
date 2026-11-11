import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {createQueryClient} from '@/shared/api';
import {json, noContent, renderWithQueries, stubFetchAround} from '@/shared/lib/test';
import {loadGroupEditPage, preloadGroupEditPage} from '../index';

const ID = '00000000-0000-0000-0005-000000000001';
const CREW = {
    id: ID, name: 'Crew', groupName: 'Crew Ltd', notes: null, types: ['demogroup'], labels: [], locations: [], members: [], memberGroups: [], groups: [],
};

/** What the page loads besides the group: the reference book of types and the countries of the locations editor. */
const AROUND = new Map([['/api/group-types', json(['demogroup'])], ['/api/countries', json([])]]);

function renderRoute() {
    const queryClient = createQueryClient();
    const router = createMemoryRouter([
        {path: '/groups', element: <p>Groups list</p>},
        {path: '/groups/:id', element: <p>Group view</p>},
        {path: '/groups/:id/edit', loader: preloadGroupEditPage(queryClient), lazy: {Component: loadGroupEditPage}, HydrateFallback: () => null},
    ], {initialEntries: [`/groups/${ID}/edit`]});
    renderWithQueries(<RouterProvider router={router}/>, queryClient);
}

describe('GroupEditPage', () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('appears with the group in its form when the preload makes it in time', async () => {
        stubFetchAround(AROUND, json(CREW));

        renderRoute();

        await screen.findByRole('heading', {level: 1, name: 'Edit group'});

        expect(screen.getByRole('textbox', {name: 'Name'})).toHaveValue('Crew Ltd');
        expect(screen.getByRole('checkbox', {name: 'demogroup'})).toBeChecked();
        expect(screen.queryByTestId('group-editor-skeleton')).not.toBeInTheDocument();
    });

    it('returns to the group after saving', async () => {
        stubFetchAround(AROUND, json(CREW), noContent());
        renderRoute();

        await userEvent.click(await screen.findByRole('button', {name: 'Save'}));

        await expect(screen.findByText('Group view')).resolves.toBeInTheDocument();
    });

    it('returns to the catalog after deleting', async () => {
        stubFetchAround(AROUND, json(CREW), noContent());
        renderRoute();

        await userEvent.click(await screen.findByRole('button', {name: 'Delete'}));
        await userEvent.click(screen.getByRole('button', {name: 'Delete for good'}));

        await expect(screen.findByText('Groups list')).resolves.toBeInTheDocument();
    });
});
