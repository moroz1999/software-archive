import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {json, renderWithQueries, stubFetch} from '@/shared/lib/test';
import {loadSoftwareNewPage} from '../index';

describe('SoftwareNewPage', () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('adds software and returns to the list', async () => {
        stubFetch(json({id: '00000000-0000-0000-0001-000000000001'}, 201));
        const router = createMemoryRouter([
            {path: '/software', element: <p>Software list</p>},
            {path: '/software/new', lazy: {Component: loadSoftwareNewPage}, HydrateFallback: () => null},
        ], {initialEntries: ['/software/new']});
        renderWithQueries(<RouterProvider router={router}/>);

        await expect(screen.findByRole('heading', {level: 1, name: 'Add software'})).resolves.toBeInTheDocument();

        await userEvent.type(screen.getByRole('textbox', {name: 'Title'}), 'Elite');
        await userEvent.click(screen.getByRole('button', {name: 'Add software'}));

        await expect(screen.findByText('Software list')).resolves.toBeInTheDocument();
    });
});
