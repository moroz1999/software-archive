import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {json, renderWithQueries, stubFetchRoutes} from '@/shared/lib/test';
import {loadAuthorNewPage} from '../index';

const ID = '00000000-0000-0000-0003-000000000001';

describe('AuthorNewPage', () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('adds an author and opens their page', async () => {
        stubFetchRoutes(new Map([
            ['/api/countries', json([])],
            ['/api/authors', json({id: ID}, 201)],
        ]));
        const router = createMemoryRouter([
            {path: '/authors/:id', element: <p>Author view</p>},
            {path: '/authors/new', lazy: {Component: loadAuthorNewPage}, HydrateFallback: () => null},
        ], {initialEntries: ['/authors/new']});
        renderWithQueries(<RouterProvider router={router}/>);

        await expect(screen.findByRole('heading', {level: 1, name: 'Add author'})).resolves.toBeInTheDocument();

        await userEvent.type(screen.getByRole('textbox', {name: 'Real name'}), 'John Doe');
        await userEvent.click(screen.getByRole('button', {name: 'Add author'}));

        await expect(screen.findByText('Author view')).resolves.toBeInTheDocument();
        expect(router.state.location.pathname).toBe(`/authors/${ID}`);
    });
});
