import type {ReactNode} from 'react';
import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest';
import {screen, within} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {BusinessError} from '@/shared/api';
import {json, renderWithQueries, stubFetchRoutes} from '@/shared/lib/test';
import type {SoftwareDraft} from '../model/software-form';
import {SoftwareFormProvider, type SoftwareFormProviderProps} from '../model/SoftwareFormProvider';
import {SoftwareForm} from './SoftwareForm';

type FormProps = Omit<SoftwareFormProviderProps, 'onSubmit' | 'children'> & {readonly actions?: ReactNode};

const PLATFORMS = [{code: 'agat', name: 'Agat'}, {code: 'bk', name: 'BK'}];
const BK_HARDWARE = [
    {code: 'bk0010', name: 'BK-0010', group: 'computers'},
    {code: 'bk0011m', name: 'BK-0011M', group: 'computers'},
    {code: 'ay8910', name: 'AY-3-8910', group: 'sound'},
];

function renderForm({actions, ...props}: FormProps = {}) {
    const onSubmit = vi.fn<(draft: SoftwareDraft) => void>();
    renderWithQueries(
        <SoftwareFormProvider onSubmit={onSubmit} platforms={PLATFORMS} {...props}>
            <SoftwareForm submitLabel="Save" actions={actions}/>
        </SoftwareFormProvider>,
    );
    return {onSubmit};
}

describe(SoftwareForm, () => {
    beforeEach(() => {
        stubFetchRoutes(new Map([
            ['/api/hardware?platform=bk', json(BK_HARDWARE)],
            ['/api/hardware?platform=agat', json([])],
        ]));
    });

    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('submits the filled form as a draft', async () => {
        const {onSubmit} = renderForm();

        await userEvent.type(screen.getByRole('textbox', {name: 'Title'}), 'Elite');
        await userEvent.type(screen.getByRole('textbox', {name: 'Description'}), 'Space trading');
        await userEvent.type(screen.getByLabelText('Release date'), '1985-09-20');
        await userEvent.selectOptions(screen.getByRole('combobox', {name: 'Platform'}), 'BK');
        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        expect(onSubmit).toHaveBeenCalledExactlyOnceWith({title: 'Elite', description: 'Space trading', releaseDate: '1985-09-20', platform: 'bk', hardware: []});
    });

    it('offers the platforms to choose from', () => {
        renderForm();

        expect(screen.getAllByRole('option').map((option) => option.textContent)).toStrictEqual(['Choose a platform', 'Agat', 'BK']);
    });

    it('does not submit without a platform and says so under the field', async () => {
        const {onSubmit} = renderForm({initial: {title: 'Elite', description: '', releaseDate: '', platform: '', hardware: []}});

        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        expect(onSubmit).not.toHaveBeenCalled();
        expect(screen.getByRole('combobox', {name: 'Platform'})).toHaveAccessibleDescription('Choose a platform.');
    });

    it('starts from the given values', () => {
        renderForm({initial: {title: 'Elite', description: '', releaseDate: '1985-09-20', platform: 'bk', hardware: []}});

        expect(screen.getByRole('textbox', {name: 'Title'})).toHaveValue('Elite');
        expect(screen.getByLabelText('Release date')).toHaveValue('1985-09-20');
        expect(screen.getByRole('combobox', {name: 'Platform'})).toHaveValue('bk');
    });

    it('does not submit without a title and says so under the field', async () => {
        const {onSubmit} = renderForm();

        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        expect(onSubmit).not.toHaveBeenCalled();
        expect(screen.getByRole('textbox', {name: 'Title'})).toHaveAccessibleDescription('Enter a title.');
    });

    it('does not submit a title longer than 255 characters', async () => {
        const {onSubmit} = renderForm({initial: {title: 'a'.repeat(256), description: '', releaseDate: '', platform: 'bk', hardware: []}});

        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        expect(onSubmit).not.toHaveBeenCalled();
        expect(screen.getByRole('textbox', {name: 'Title'})).toHaveAccessibleDescription('Keep the title within 255 characters.');
    });

    it('locks the form while it is being saved', () => {
        renderForm({busy: true});

        expect(screen.getByRole('textbox', {name: 'Title'})).toBeDisabled();
        expect(screen.getByRole('combobox', {name: 'Platform'})).toBeDisabled();
        expect(screen.getByRole('button', {name: 'Save'})).toBeDisabled();
    });

    it('shows a business error of the whole form at its top', () => {
        renderForm({error: new BusinessError(409, 'software.changed', [])});

        expect(screen.getByRole('alert')).toHaveTextContent('This item was changed in the meantime. Reload and try again.');
    });

    it('shows a business error of a field under that field, in the slice\'s words', () => {
        renderForm({
            error: new BusinessError(422, 'software.title-taken', [{field: 'title', code: 'software.title-taken'}]),
            messages: {'software.title-taken': 'This title is already taken.'},
        });

        expect(screen.getByRole('textbox', {name: 'Title'})).toHaveAccessibleDescription('This title is already taken.');
        expect(screen.queryByRole('alert')).not.toBeInTheDocument();
    });

    it('clears a business error of a field once the field is edited', async () => {
        renderForm({error: new BusinessError(422, 'software.title-taken', [{field: 'title', code: 'software.title-taken'}])});

        await userEvent.type(screen.getByRole('textbox', {name: 'Title'}), 'x');

        expect(screen.getByRole('textbox', {name: 'Title'})).not.toHaveAccessibleDescription();
    });

    it('shows a business error of a field the form does not have at its top', () => {
        renderForm({error: new BusinessError(422, 'software.year-locked', [{field: 'year', code: 'software.year-locked'}])});

        expect(screen.getByRole('alert')).toHaveTextContent('The request was refused.');
    });

    it('shows extra actions next to the submit button', () => {
        renderForm({actions: <span>Delete</span>});

        expect(screen.getByText('Delete')).toBeInTheDocument();
    });

    it('offers no hardware until a platform is chosen', () => {
        renderForm();

        expect(screen.queryByRole('group', {name: 'Hardware'})).not.toBeInTheDocument();
    });

    it('offers the hardware of the chosen platform by group and submits what is checked', async () => {
        const {onSubmit} = renderForm({initial: {title: 'Elite', description: '', releaseDate: '', platform: '', hardware: []}});

        await userEvent.selectOptions(screen.getByRole('combobox', {name: 'Platform'}), 'BK');
        const computers = await screen.findByRole('group', {name: 'Computers'});
        await userEvent.click(within(computers).getByRole('checkbox', {name: 'BK-0011M'}));
        await userEvent.click(screen.getByRole('checkbox', {name: 'AY-3-8910'}));
        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        expect(onSubmit).toHaveBeenCalledExactlyOnceWith(expect.objectContaining({platform: 'bk', hardware: ['bk0011m', 'ay8910']}));
    });

    it('starts from the hardware of the software', async () => {
        renderForm({initial: {title: 'Elite', description: '', releaseDate: '', platform: 'bk', hardware: ['bk0010']}});

        await expect(screen.findByRole('checkbox', {name: 'BK-0010'})).resolves.toBeChecked();
        expect(screen.getByRole('button', {name: 'Remove BK-0010'})).toBeInTheDocument();
    });

    it('locks the platform while hardware is checked and says why', async () => {
        renderForm({initial: {title: 'Elite', description: '', releaseDate: '', platform: 'bk', hardware: []}});

        await userEvent.click(await screen.findByRole('checkbox', {name: 'BK-0010'}));

        expect(screen.getByRole('combobox', {name: 'Platform'})).toBeDisabled();
        expect(screen.getByRole('combobox', {name: 'Platform'})).toHaveAccessibleDescription('Remove the hardware to change the platform.');

        await userEvent.click(screen.getByRole('button', {name: 'Remove BK-0010'}));

        expect(screen.getByRole('combobox', {name: 'Platform'})).toBeEnabled();
    });

    it('shows a refusal of the hardware under the hardware', async () => {
        renderForm({
            initial: {title: 'Elite', description: '', releaseDate: '', platform: 'bk', hardware: []},
            error: new BusinessError(422, 'hardware.unknown', [{field: 'hardware', code: 'hardware.unknown'}]),
            messages: {'hardware.unknown': 'Choose hardware of the platform of the software.'},
        });

        await expect(screen.findByRole('group', {name: 'Hardware'})).resolves.toHaveAccessibleDescription('Choose hardware of the platform of the software.');
    });

    it('locks the hardware while it is being saved', async () => {
        renderForm({initial: {title: 'Elite', description: '', releaseDate: '', platform: 'bk', hardware: []}, busy: true});

        await expect(screen.findByRole('checkbox', {name: 'BK-0010'})).resolves.toBeDisabled();
    });
});
