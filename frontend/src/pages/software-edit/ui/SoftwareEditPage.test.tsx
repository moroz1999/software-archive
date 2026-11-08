import {afterEach, describe, expect, it, vi} from 'vitest';
import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {json, noContent, stubFetch} from '../../../shared/lib/test';
import {preloadSoftwareEditPage, SoftwareEditPage} from '../index';

const ID = '00000000-0000-0000-0001-000000000001';
const ELITE = {id: ID, title: 'Elite', description: null, releaseDate: null};

function renderRoute(id: string) {
    const router = createMemoryRouter([
        {path: '/', element: <p>Software list</p>},
        {path: '/software/:id/edit', loader: preloadSoftwareEditPage, Component: SoftwareEditPage, HydrateFallback: () => null},
    ], {initialEntries: [`/software/${id}/edit`]});
    render(<RouterProvider router={router}/>);
}

describe(SoftwareEditPage, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('appears with the software in its form when the preload makes it in time', async () => {
        stubFetch(json(ELITE));

        renderRoute(ID);

        await screen.findByRole('heading', {level: 1, name: 'Edit software'});

        expect(screen.getByRole('textbox', {name: 'Title'})).toHaveValue('Elite');
        expect(screen.queryByTestId('software-editor-skeleton')).not.toBeInTheDocument();
    });

    it('returns to the list after saving', async () => {
        stubFetch(json(ELITE), noContent());
        renderRoute(ID);

        await userEvent.click(await screen.findByRole('button', {name: 'Save'}));

        await expect(screen.findByText('Software list')).resolves.toBeInTheDocument();
    });

    it('returns to the list after deleting', async () => {
        stubFetch(json(ELITE), noContent());
        renderRoute(ID);

        await userEvent.click(await screen.findByRole('button', {name: 'Delete'}));
        await userEvent.click(screen.getByRole('button', {name: 'Delete for good'}));

        await expect(screen.findByText('Software list')).resolves.toBeInTheDocument();
    });

    it('says so for unknown software', async () => {
        stubFetch(json({status: 404, code: 'software.not-found'}, 404));

        renderRoute(ID);

        await expect(screen.findByText('Software not found.')).resolves.toBeInTheDocument();
    });
});
