import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {MemoryRouter} from 'react-router';
import {json, renderWithQueries, stubFetch} from '@/shared/lib/test';
import {CategoryTreeProvider} from '../model/CategoryTreeProvider';
import {CategoryTree} from './CategoryTree';

const GAMES = '00000000-0000-0000-0006-000000000001';
const ACTION = '00000000-0000-0000-0006-000000000002';
const SHOOTERS = '00000000-0000-0000-0006-000000000003';
const TREE = [
    {id: GAMES, title: 'Games', children: [{id: ACTION, title: 'Action', children: [{id: SHOOTERS, title: 'Shooters', children: []}]}]},
    {id: '00000000-0000-0000-0006-000000000004', title: 'Demoscene', children: []},
];

function renderTree() {
    renderWithQueries(<MemoryRouter><CategoryTreeProvider><CategoryTree/></CategoryTreeProvider></MemoryRouter>);
}

describe(CategoryTree, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('shows a skeleton, then the categories at the top, each leading to its editing', async () => {
        stubFetch(json(TREE));
        renderTree();

        expect(screen.getByTestId('category-tree-skeleton')).toBeInTheDocument();
        await expect(screen.findByRole('link', {name: 'Games'})).resolves.toHaveAttribute('href', `/categories/${GAMES}/edit`);
        expect(screen.getByRole('link', {name: 'Demoscene'})).toBeInTheDocument();
        expect(screen.queryByRole('link', {name: 'Action'})).not.toBeInTheDocument();
    });

    it('opens and closes the subcategories of a category', async () => {
        stubFetch(json(TREE));
        renderTree();

        const expand = await screen.findByRole('button', {name: 'Expand Games'});

        expect(expand).toHaveAttribute('aria-expanded', 'false');
        expect(screen.queryByRole('button', {name: 'Expand Demoscene'})).not.toBeInTheDocument();

        await userEvent.click(expand);

        expect(screen.getByRole('link', {name: 'Action'})).toHaveAttribute('href', `/categories/${ACTION}/edit`);
        expect(screen.queryByRole('link', {name: 'Shooters'})).not.toBeInTheDocument();

        await userEvent.click(screen.getByRole('button', {name: 'Collapse Games'}));

        expect(screen.queryByRole('link', {name: 'Action'})).not.toBeInTheDocument();
    });

    it('opens the subcategories of every level', async () => {
        stubFetch(json(TREE));
        renderTree();

        await userEvent.click(await screen.findByRole('button', {name: 'Expand Games'}));
        await userEvent.click(screen.getByRole('button', {name: 'Expand Action'}));

        expect(screen.getByRole('link', {name: 'Shooters'})).toHaveAttribute('href', `/categories/${SHOOTERS}/edit`);
    });

    it('says when there are no categories', async () => {
        stubFetch(json([]));
        renderTree();

        await expect(screen.findByText('No categories yet.')).resolves.toBeInTheDocument();
    });
});
