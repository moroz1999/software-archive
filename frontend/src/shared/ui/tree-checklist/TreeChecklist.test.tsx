import {useState} from 'react';
import {describe, expect, it, vi} from 'vitest';
import {render, screen, within} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {TreeChecklist, type TreeChecklistItem} from './TreeChecklist';

/** Games → Action → Shooters, Games → Puzzle, and Demoscene beside Games. */
const ITEMS: readonly TreeChecklistItem[] = [
    {id: 'games', name: 'Games', children: [
        {id: 'action', name: 'Action', children: [{id: 'shooters', name: 'Shooters', children: []}]},
        {id: 'puzzle', name: 'Puzzle', children: []},
    ]},
    {id: 'demoscene', name: 'Demoscene', children: []},
];

/** The checklist with its value held, as a form holds it. */
function Held({initial = []}: {readonly initial?: readonly string[]}) {
    const [value, setValue] = useState(initial);
    return <TreeChecklist label="Categories" items={ITEMS} value={value} onChange={setValue}/>;
}

describe(TreeChecklist, () => {
    it('offers every item of the tree, each under its parent', () => {
        render(<Held/>);

        const choice = screen.getByRole('group', {name: 'Categories'});
        const games = within(choice).getByRole('group', {name: 'Games'});

        expect(within(choice).getAllByRole('checkbox').map((box) => box.closest('label')?.textContent)).toStrictEqual([
            'Games', 'Action', 'Shooters', 'Puzzle', 'Demoscene',
        ]);
        expect(within(games).getByRole('group', {name: 'Action'})).toContainElement(screen.getByRole('checkbox', {name: 'Shooters'}));
        expect(games).not.toContainElement(screen.getByRole('checkbox', {name: 'Demoscene'}));
    });

    it('hands on the chosen ids once one is checked, a parent not checking its children', async () => {
        const onChange = vi.fn<(value: readonly string[]) => void>();
        render(<TreeChecklist label="Categories" items={ITEMS} value={['puzzle']} onChange={onChange}/>);

        await userEvent.click(screen.getByRole('checkbox', {name: 'Games'}));

        expect(onChange).toHaveBeenCalledExactlyOnceWith(['puzzle', 'games']);
    });

    it('shows the chosen items as chips and counts them', () => {
        render(<Held initial={['shooters', 'demoscene']}/>);

        expect(screen.getByRole('checkbox', {name: 'Shooters'})).toBeChecked();
        expect(screen.getByRole('checkbox', {name: 'Action'})).not.toBeChecked();
        expect(screen.getByRole('button', {name: 'Remove Shooters'})).toBeInTheDocument();
        expect(screen.getByRole('button', {name: 'Remove Demoscene'})).toBeInTheDocument();
        expect(screen.getByText('2 selected')).toBeInTheDocument();
    });

    it('unchooses an item by its chip', async () => {
        render(<Held initial={['shooters', 'demoscene']}/>);

        await userEvent.click(screen.getByRole('button', {name: 'Remove Shooters'}));

        expect(screen.getByRole('checkbox', {name: 'Shooters'})).not.toBeChecked();
        expect(screen.queryByRole('button', {name: 'Remove Shooters'})).not.toBeInTheDocument();
        expect(screen.getByText('1 selected')).toBeInTheDocument();
    });

    it('filters the items by their names, keeping the parents of those found', async () => {
        render(<Held/>);

        await userEvent.type(screen.getByRole('searchbox', {name: 'Filter Categories'}), 'shoot');

        expect(screen.getAllByRole('checkbox').map((box) => box.closest('label')?.textContent)).toStrictEqual(['Games', 'Action', 'Shooters']);
    });

    it('says when the filter finds nothing', async () => {
        render(<Held/>);

        await userEvent.type(screen.getByRole('searchbox', {name: 'Filter Categories'}), 'racing');

        expect(screen.queryByRole('checkbox')).not.toBeInTheDocument();
        expect(screen.getByText('Nothing found.')).toBeInTheDocument();
    });

    it('shows why the choice was refused, tied to the choice', () => {
        render(<TreeChecklist label="Categories" items={ITEMS} value={[]} onChange={vi.fn<(value: readonly string[]) => void>()} error="Not in the archive."/>);

        expect(screen.getByRole('group', {name: 'Categories'})).toHaveAccessibleDescription('Not in the archive.');
    });

    it('can be locked', () => {
        render(<TreeChecklist label="Categories" items={ITEMS} value={['games']} onChange={vi.fn<(value: readonly string[]) => void>()} disabled/>);

        expect(screen.getByRole('checkbox', {name: 'Puzzle'})).toBeDisabled();
        expect(screen.getByRole('searchbox', {name: 'Filter Categories'})).toBeDisabled();
        expect(screen.getByRole('button', {name: 'Remove Games'})).toBeDisabled();
    });
});
