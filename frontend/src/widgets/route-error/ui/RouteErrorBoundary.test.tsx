import {describe, expect, it} from 'vitest';
import {render, screen} from '@testing-library/react';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {NotFoundError} from '@/shared/api';
import {RouteErrorBoundary} from './RouteErrorBoundary';

function renderAt(path: string, loader: () => unknown) {
    const router = createMemoryRouter([{
        ErrorBoundary: RouteErrorBoundary,
        children: [
            {path: '/', element: <p>Software list</p>},
            {path: '/broken', loader, element: <p>Never shown</p>},
        ],
    }], {initialEntries: [path]});
    render(<RouterProvider router={router}/>);
}

const nothing = () => null;

describe(RouteErrorBoundary, () => {
    it('shows the not found page for a missing entity', async () => {
        renderAt('/broken', () => {
            throw new NotFoundError('software.not-found');
        });

        await expect(screen.findByRole('heading', {level: 1, name: 'Not found'})).resolves.toBeInTheDocument();
        expect(screen.getByText('This page does not exist or has been deleted.')).toBeInTheDocument();
    });

    it('shows the not found page for an address the app does not know', async () => {
        renderAt('/nowhere', nothing);

        await expect(screen.findByRole('heading', {level: 1, name: 'Not found'})).resolves.toBeInTheDocument();
    });

    it('leads from the not found page to the software list', async () => {
        renderAt('/nowhere', nothing);

        await expect(screen.findByRole('link', {name: 'Go to the software list'})).resolves.toHaveAttribute('href', '/');
    });

    it('shows a page of its own for anything else that breaks', async () => {
        renderAt('/broken', () => {
            throw new Error('boom');
        });

        await expect(screen.findByRole('heading', {level: 1, name: 'Something went wrong'})).resolves.toBeInTheDocument();
    });
});
