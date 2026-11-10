import type {ReactNode} from 'react';
import {describe, expect, it, vi} from 'vitest';
import {render, screen, within} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {BusinessError} from '@/shared/api';
import type {ReleaseDraft} from '../model/release-form';
import {ReleaseFormProvider, type ReleaseFormProviderProps} from '../model/ReleaseFormProvider';
import type {Hardware} from '../model/software';
import {ReleaseForm} from './ReleaseForm';

type FormProps = Omit<ReleaseFormProviderProps, 'onSubmit' | 'children'> & {readonly actions?: ReactNode};

const HARDWARE: readonly Hardware[] = [
    {code: 'vector06c', name: 'Vector-06C', group: 'computers'},
    {code: 'ay8910', name: 'AY-3-8910', group: 'sound'},
    {code: 'beeper', name: 'Beeper', group: 'sound'},
];

function renderForm({actions, ...props}: FormProps = {}) {
    const onSubmit = vi.fn<(draft: ReleaseDraft) => void>();
    render(
        <ReleaseFormProvider onSubmit={onSubmit} hardware={HARDWARE} {...props}>
            <ReleaseForm submitLabel="Save" actions={actions}/>
        </ReleaseFormProvider>,
    );
    return {onSubmit};
}

describe(ReleaseForm, () => {
    it('submits the filled form as a draft', async () => {
        const {onSubmit} = renderForm();

        await userEvent.type(screen.getByRole('textbox', {name: 'Title'}), 'Elite AY');
        await userEvent.type(screen.getByRole('textbox', {name: 'Version'}), '1.1');
        await userEvent.selectOptions(screen.getByRole('combobox', {name: 'Type'}), 'Mod');
        await userEvent.type(screen.getByLabelText('Release date'), '1990-03-01');
        await userEvent.type(screen.getByRole('textbox', {name: 'Description'}), 'With AY music');
        await userEvent.click(screen.getByRole('checkbox', {name: 'AY-3-8910'}));
        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        expect(onSubmit).toHaveBeenCalledExactlyOnceWith({
            title: 'Elite AY', version: '1.1', type: 'mod', releaseDate: '1990-03-01', description: 'With AY music', hardware: ['ay8910'],
        });
    });

    it('starts a new release of unknown type, offering every type', () => {
        renderForm();

        expect(screen.getByRole('combobox', {name: 'Type'})).toHaveValue('unknown');
        expect(within(screen.getByRole('combobox', {name: 'Type'})).getAllByRole('option').map((option) => option.textContent))
            .toContain('Demo version');
    });

    it('offers the hardware by group and says how a group of the release works', () => {
        renderForm();

        const sound = screen.getByRole('group', {name: 'Sound'});

        expect(within(sound).getAllByRole('checkbox')).toStrictEqual([
            within(sound).getByRole('checkbox', {name: 'AY-3-8910'}),
            within(sound).getByRole('checkbox', {name: 'Beeper'}),
        ]);
        expect(screen.getByRole('group', {name: 'Computers'})).toContainElement(screen.getByRole('checkbox', {name: 'Vector-06C'}));
        expect(screen.getByText('A group checked here replaces that group of the software.')).toBeInTheDocument();
    });

    it('finds hardware by its name', async () => {
        renderForm();

        await userEvent.type(screen.getByRole('searchbox', {name: 'Filter Hardware'}), 'beep');

        expect(screen.getByRole('checkbox', {name: 'Beeper'})).toBeInTheDocument();
        expect(screen.queryByRole('checkbox', {name: 'AY-3-8910'})).not.toBeInTheDocument();
    });

    it('unchecks hardware the release had', async () => {
        const {onSubmit} = renderForm({initial: {title: '', version: '', type: 'mod', releaseDate: '', description: '', hardware: ['ay8910', 'beeper']}});

        expect(screen.getByRole('checkbox', {name: 'Beeper'})).toBeChecked();

        await userEvent.click(screen.getByRole('checkbox', {name: 'Beeper'}));
        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        expect(onSubmit).toHaveBeenCalledWith(expect.objectContaining({hardware: ['ay8910']}));
    });

    it('does not submit a too long version and says so under the field until it is edited', async () => {
        const {onSubmit} = renderForm();

        await userEvent.type(screen.getByRole('textbox', {name: 'Version'}), '1'.repeat(51));
        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        expect(onSubmit).not.toHaveBeenCalled();
        expect(screen.getByRole('textbox', {name: 'Version'})).toHaveAccessibleDescription('Keep the version within 50 characters.');

        await userEvent.type(screen.getByRole('textbox', {name: 'Version'}), '{backspace}');

        expect(screen.getByRole('textbox', {name: 'Version'})).not.toHaveAccessibleDescription();
    });

    it('shows a refusal of the hardware under the hardware', () => {
        renderForm({
            error: new BusinessError(422, 'hardware.unknown', [{field: 'hardware', code: 'hardware.unknown'}]),
            messages: {'hardware.unknown': 'Choose hardware of the platform of the software.'},
        });

        expect(screen.getByRole('group', {name: 'Hardware'})).toHaveAccessibleDescription('A group checked here replaces that group of the software. Choose hardware of the platform of the software.');
    });

    it('shows a refusal of the whole form at its top', () => {
        renderForm({error: new BusinessError(422, 'release.refused', [])});

        expect(screen.getByRole('alert')).toHaveTextContent('The request was refused.');
    });

    it('locks the fields and the submit button while saving', () => {
        renderForm({busy: true});

        expect(screen.getByRole('textbox', {name: 'Title'})).toBeDisabled();
        expect(screen.getByRole('checkbox', {name: 'Beeper'})).toBeDisabled();
        expect(screen.getByRole('button', {name: 'Save'})).toBeDisabled();
    });
});
