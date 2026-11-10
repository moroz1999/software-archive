import {useState} from 'react';
import {describe, expect, it, vi} from 'vitest';
import {render, screen, within} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {MultiSelectFilter, type MultiSelectGroup} from './MultiSelectFilter';

const GROUPS: readonly MultiSelectGroup[] = [
    {label: 'Computers', options: [{value: 'vector06c', label: 'Vector-06C'}]},
    {label: 'Sound', options: [{value: 'ay8910', label: 'AY-3-8910'}, {value: 'beeper', label: 'Beeper'}]},
];

/** The filter with its value held, as a form holds it. */
function Held({initial = [], disabled = false}: {readonly initial?: readonly string[]; readonly disabled?: boolean}) {
    const [value, setValue] = useState(initial);
    return <MultiSelectFilter label="Hardware" groups={GROUPS} value={value} onChange={setValue} disabled={disabled}/>;
}

describe(MultiSelectFilter, () => {
    it('offers the options under the names of their groups', () => {
        render(<Held/>);

        const sound = screen.getByRole('group', {name: 'Sound'});

        expect(within(sound).getAllByRole('checkbox')).toStrictEqual([
            within(sound).getByRole('checkbox', {name: 'AY-3-8910'}),
            within(sound).getByRole('checkbox', {name: 'Beeper'}),
        ]);
        expect(screen.getByRole('group', {name: 'Computers'})).toContainElement(screen.getByRole('checkbox', {name: 'Vector-06C'}));
        expect(screen.getByRole('group', {name: 'Hardware'})).toContainElement(sound);
    });

    it('hands on the chosen values once one is checked', async () => {
        const onChange = vi.fn<(value: readonly string[]) => void>();
        render(<MultiSelectFilter label="Hardware" groups={GROUPS} value={['vector06c']} onChange={onChange}/>);

        await userEvent.click(screen.getByRole('checkbox', {name: 'AY-3-8910'}));

        expect(onChange).toHaveBeenCalledExactlyOnceWith(['vector06c', 'ay8910']);
    });

    it('shows the chosen values as chips and counts them', () => {
        render(<Held initial={['ay8910', 'vector06c']}/>);

        expect(screen.getByRole('checkbox', {name: 'AY-3-8910'})).toBeChecked();
        expect(screen.getByRole('button', {name: 'Remove AY-3-8910'})).toBeInTheDocument();
        expect(screen.getByRole('button', {name: 'Remove Vector-06C'})).toBeInTheDocument();
        expect(screen.getByText('2 selected')).toBeInTheDocument();
    });

    it('unchooses a value by its chip', async () => {
        render(<Held initial={['ay8910', 'vector06c']}/>);

        await userEvent.click(screen.getByRole('button', {name: 'Remove AY-3-8910'}));

        expect(screen.getByRole('checkbox', {name: 'AY-3-8910'})).not.toBeChecked();
        expect(screen.queryByRole('button', {name: 'Remove AY-3-8910'})).not.toBeInTheDocument();
        expect(screen.getByText('1 selected')).toBeInTheDocument();
    });

    it('filters the options by their names, leaving out groups with nothing found', async () => {
        render(<Held/>);

        await userEvent.type(screen.getByRole('searchbox', {name: 'Filter Hardware'}), 'ay');

        expect(screen.getByRole('checkbox', {name: 'AY-3-8910'})).toBeInTheDocument();
        expect(screen.queryByRole('checkbox', {name: 'Beeper'})).not.toBeInTheDocument();
        expect(screen.queryByRole('group', {name: 'Computers'})).not.toBeInTheDocument();
    });

    it('keeps chosen values the filter hides', async () => {
        render(<Held initial={['beeper']}/>);

        await userEvent.type(screen.getByRole('searchbox', {name: 'Filter Hardware'}), 'ay');

        expect(screen.getByRole('button', {name: 'Remove Beeper'})).toBeInTheDocument();
    });

    it('says so when the filter finds nothing', async () => {
        render(<Held/>);

        await userEvent.type(screen.getByRole('searchbox', {name: 'Filter Hardware'}), 'zx');

        expect(screen.getByText('Nothing found.')).toBeInTheDocument();
        expect(screen.queryByRole('checkbox')).not.toBeInTheDocument();
    });

    it('can be locked', () => {
        render(<Held initial={['beeper']} disabled/>);

        expect(screen.getByRole('checkbox', {name: 'AY-3-8910'})).toBeDisabled();
        expect(screen.getByRole('searchbox', {name: 'Filter Hardware'})).toBeDisabled();
        expect(screen.getByRole('button', {name: 'Remove Beeper'})).toBeDisabled();
    });

    it('tells why the choice was refused and what to know about it', () => {
        render(<MultiSelectFilter label="Hardware" groups={GROUPS} value={[]} onChange={vi.fn<(value: readonly string[]) => void>()} error="Choose hardware of the platform." hint="A group replaces that group."/>);

        expect(screen.getByRole('group', {name: 'Hardware'})).toHaveAccessibleDescription('A group replaces that group. Choose hardware of the platform.');
    });
});
