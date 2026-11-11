import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {json, renderWithQueries, stubFetchRoutes} from '@/shared/lib/test';
import {loadGroupNewPage} from '../index';

const ID = '00000000-0000-0000-0005-000000000001';

describe('GroupNewPage', () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('adds a group and opens its page', async () => {
        stubFetchRoutes(new Map([
            ['/api/group-types', json(['demogroup'])],
            ['/api/countries', json([])],
            ['/api/groups', json({id: ID}, 201)],
        ]));
        const router = createMemoryRouter([
            {path: '/groups/:id', element: <p>Group view</p>},
            {path: '/groups/new', lazy: {Component: loadGroupNewPage}, HydrateFallback: () => null},
        ], {initialEntries: ['/groups/new']});
        renderWithQueries(<RouterProvider router={router}/>);

        await expect(screen.findByRole('heading', {level: 1, name: 'Add group'})).resolves.toBeInTheDocument();

        await userEvent.type(screen.getByRole('textbox', {name: 'Name'}), 'Crew Ltd');
        await userEvent.click(await screen.findByRole('checkbox', {name: 'demogroup'}));
        await userEvent.click(screen.getByRole('button', {name: 'Add group'}));

        await expect(screen.findByText('Group view')).resolves.toBeInTheDocument();
        expect(router.state.location.pathname).toBe(`/groups/${ID}`);
    });
});
