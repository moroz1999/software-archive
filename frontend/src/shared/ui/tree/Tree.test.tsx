import {describe, expect, it} from 'vitest';
import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {Tree, type TreeItem} from './Tree';

const ITEMS: readonly TreeItem[] = [
    {id: 'a', name: 'Alpha', content: 'Alpha', children: [{id: 'a1', name: 'Alpha one', content: 'Alpha one', children: []}]},
    {id: 'b', name: 'Beta', content: 'Beta', children: []},
];

function renderTree() {
    render(<Tree items={ITEMS} ariaLabel="Things" expandLabel={(name) => `Expand ${name}`} collapseLabel={(name) => `Collapse ${name}`}/>);
}

describe(Tree, () => {
    it('shows the items of the top as a named list, their children folded', () => {
        renderTree();

        expect(screen.getByRole('list', {name: 'Things'})).toBeInTheDocument();
        expect(screen.getByText('Alpha')).toBeInTheDocument();
        expect(screen.getByText('Beta')).toBeInTheDocument();
        expect(screen.queryByText('Alpha one')).not.toBeInTheDocument();
    });

    it('unfolds and folds the children of an item that has them', async () => {
        renderTree();

        expect(screen.queryByRole('button', {name: 'Expand Beta'})).not.toBeInTheDocument();

        await userEvent.click(screen.getByRole('button', {name: 'Expand Alpha'}));

        expect(screen.getByText('Alpha one')).toBeInTheDocument();
        expect(screen.getByRole('button', {name: 'Collapse Alpha'})).toHaveAttribute('aria-expanded', 'true');

        await userEvent.click(screen.getByRole('button', {name: 'Collapse Alpha'}));

        expect(screen.queryByText('Alpha one')).not.toBeInTheDocument();
        expect(screen.getByRole('button', {name: 'Expand Alpha'})).toHaveAttribute('aria-expanded', 'false');
    });
});
