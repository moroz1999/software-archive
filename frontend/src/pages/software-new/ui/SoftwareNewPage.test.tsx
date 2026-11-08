import {afterEach, describe, expect, it, vi} from 'vitest';
import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {json, stubFetch} from '../../../shared/lib/test';
import {SoftwareNewPage} from '../index';

describe(SoftwareNewPage, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('adds software and returns to the list', async () => {
        stubFetch(json({id: '00000000-0000-0000-0001-000000000001'}, 201));
        const router = createMemoryRouter([
            {path: '/', element: <p>Software list</p>},
            {path: '/software/new', Component: SoftwareNewPage},
        ], {initialEntries: ['/software/new']});
        render(<RouterProvider router={router}/>);

        expect(screen.getByRole('heading', {level: 1, name: 'Add software'})).toBeInTheDocument();

        await userEvent.type(screen.getByRole('textbox', {name: 'Title'}), 'Elite');
        await userEvent.click(screen.getByRole('button', {name: 'Add software'}));

        await expect(screen.findByText('Software list')).resolves.toBeInTheDocument();
    });
});
