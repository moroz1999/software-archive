import {useState} from 'react';
import {describe, expect, it} from 'vitest';
import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import type {LabelValue} from '../model/label-value';
import {LabelsEditor, type LabelsEditorProps} from './LabelsEditor';

const ZED: LabelValue = {id: '00000000-0000-0000-0004-000000000001', name: 'Zed', display: true};
const AXE: LabelValue = {id: '00000000-0000-0000-0004-000000000002', name: 'Axe', display: false};

/** The editor holding its value the way a form provider would. */
function Harness({initial, ...props}: Omit<LabelsEditorProps, 'value' | 'onChange'> & {readonly initial: readonly LabelValue[]}) {
    const [value, setValue] = useState(initial);
    return (
        <>
            <LabelsEditor {...props} value={value} onChange={setValue}/>
            <output aria-label="Value">{JSON.stringify(value)}</output>
        </>
    );
}

function value(): unknown {
    return JSON.parse(screen.getByRole('status', {name: 'Value'}).textContent);
}

/** Whether each of the radios of that name is chosen, in their order. */
function checked(name: string) {
    return screen.getAllByRole('radio', {name}).map((radio) => radio.matches(':checked'));
}

describe(LabelsEditor, () => {
    it('shows every label with its name and whether it is the display name', () => {
        render(<Harness initial={[ZED, AXE]}/>);

        expect(screen.getByRole('group', {name: 'Labels'})).toBeInTheDocument();
        expect(screen.getByRole('textbox', {name: 'Label 1'})).toHaveValue('Zed');
        expect(screen.getByRole('textbox', {name: 'Label 2'})).toHaveValue('Axe');
        expect(checked('Display name')).toStrictEqual([true, false]);
    });

    it('offers no display name for an owner who has none, a group', () => {
        render(<Harness initial={[{...ZED, display: false}, AXE]} withDisplayName={false}/>);

        expect(screen.getByRole('textbox', {name: 'Label 1'})).toHaveValue('Zed');
        expect(screen.queryByRole('radio', {name: 'Display name'})).not.toBeInTheDocument();
    });

    it('marks no new label as the display name of an owner who has none', async () => {
        render(<Harness initial={[]} withDisplayName={false}/>);

        await userEvent.click(screen.getByRole('button', {name: 'Add label'}));
        await userEvent.type(screen.getByRole('textbox', {name: 'Label 1'}), 'Crew');

        expect(value()).toStrictEqual([{id: null, name: 'Crew', display: false}]);
    });

    it('renames a label, keeping its id', async () => {
        render(<Harness initial={[ZED]}/>);

        await userEvent.clear(screen.getByRole('textbox', {name: 'Label 1'}));
        await userEvent.type(screen.getByRole('textbox', {name: 'Label 1'}), 'Zed II');

        expect(value()).toStrictEqual([{...ZED, name: 'Zed II'}]);
    });

    it('adds a new label without an id', async () => {
        render(<Harness initial={[ZED]}/>);

        await userEvent.click(screen.getByRole('button', {name: 'Add label'}));
        await userEvent.type(screen.getByRole('textbox', {name: 'Label 2'}), 'Bob');

        expect(value()).toStrictEqual([ZED, {id: null, name: 'Bob', display: false}]);
    });

    it('takes a label away', async () => {
        render(<Harness initial={[ZED, AXE]}/>);

        await userEvent.click(screen.getByRole('button', {name: 'Remove label 2'}));

        expect(value()).toStrictEqual([ZED]);
    });

    it('leaves the other labels unmarked when the display label is taken away', async () => {
        render(<Harness initial={[ZED, AXE]}/>);

        await userEvent.click(screen.getByRole('button', {name: 'Remove label 1'}));

        expect(value()).toStrictEqual([AXE]);
        expect(checked('Display name')).toStrictEqual([false]);
    });

    it('adds the first label without making it the display name', async () => {
        render(<Harness initial={[]}/>);

        await userEvent.click(screen.getByRole('button', {name: 'Add label'}));

        expect(value()).toStrictEqual([{id: null, name: '', display: false}]);
        expect(checked('Display name')).toStrictEqual([false]);
    });

    it('chooses one display name', async () => {
        render(<Harness initial={[ZED, AXE]}/>);

        await userEvent.click(screen.getAllByRole('radio', {name: 'Display name'})[1] ?? document.body);

        expect(value()).toStrictEqual([{...ZED, display: false}, {...AXE, display: true}]);
        expect(checked('Display name')).toStrictEqual([false, true]);
    });

    it('lets the editor clear the display name and keep all labels', async () => {
        render(<Harness initial={[ZED, AXE]}/>);

        await userEvent.click(screen.getByRole('radio', {name: 'No display name'}));

        expect(value()).toStrictEqual([{...ZED, display: false}, AXE]);
        expect(checked('Display name')).toStrictEqual([false, false]);
    });

    it('says what is wrong with a label under it, and why the labels were refused under them all', () => {
        render(<Harness initial={[ZED, AXE]} rowErrors={[undefined, 'Enter a name.']} error="Reload and try again."/>);

        expect(screen.getByRole('textbox', {name: 'Label 1'})).not.toHaveAccessibleDescription();
        expect(screen.getByRole('textbox', {name: 'Label 2'})).toHaveAccessibleDescription('Enter a name.');
        expect(screen.getByRole('textbox', {name: 'Label 2'})).toBeInvalid();
        expect(screen.getByRole('group', {name: 'Labels'})).toHaveAccessibleDescription('Reload and try again.');
    });

    it('locks every control while disabled', () => {
        render(<Harness initial={[ZED]} disabled/>);

        expect(screen.getByRole('textbox', {name: 'Label 1'})).toBeDisabled();
        expect(screen.getByRole('radio', {name: 'Display name'})).toBeDisabled();
        expect(screen.getByRole('button', {name: 'Remove label 1'})).toBeDisabled();
        expect(screen.getByRole('button', {name: 'Add label'})).toBeDisabled();
    });
});
