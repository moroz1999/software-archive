import {describe, expect, it, vi} from 'vitest';
import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import type {SoftwareDraft} from '../model/software-form';
import {SoftwareForm} from './SoftwareForm';

function renderForm(props: Partial<Parameters<typeof SoftwareForm>[0]> = {}) {
    const onSubmit = vi.fn<(draft: SoftwareDraft) => void>();
    render(<SoftwareForm submitLabel="Save" onSubmit={onSubmit} {...props}/>);
    return {onSubmit};
}

describe(SoftwareForm, () => {
    it('submits the filled form as a draft', async () => {
        const {onSubmit} = renderForm();

        await userEvent.type(screen.getByRole('textbox', {name: 'Title'}), 'Elite');
        await userEvent.type(screen.getByRole('textbox', {name: 'Description'}), 'Space trading');
        await userEvent.type(screen.getByLabelText('Release date'), '1985-09-20');
        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        expect(onSubmit).toHaveBeenCalledExactlyOnceWith({title: 'Elite', description: 'Space trading', releaseDate: '1985-09-20'});
    });

    it('starts from the given values', () => {
        renderForm({initial: {title: 'Elite', description: '', releaseDate: '1985-09-20'}});

        expect(screen.getByRole('textbox', {name: 'Title'})).toHaveValue('Elite');
        expect(screen.getByLabelText('Release date')).toHaveValue('1985-09-20');
    });

    it('does not submit without a title and says so under the field', async () => {
        const {onSubmit} = renderForm();

        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        expect(onSubmit).not.toHaveBeenCalled();
        expect(screen.getByRole('textbox', {name: 'Title'})).toHaveAccessibleDescription('Enter a title.');
    });

    it('does not submit a title longer than 255 characters', async () => {
        const {onSubmit} = renderForm({initial: {title: 'a'.repeat(256), description: '', releaseDate: ''}});

        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        expect(onSubmit).not.toHaveBeenCalled();
        expect(screen.getByRole('textbox', {name: 'Title'})).toHaveAccessibleDescription('Keep the title within 255 characters.');
    });

    it('locks the form while it is being saved', () => {
        renderForm({busy: true});

        expect(screen.getByRole('textbox', {name: 'Title'})).toBeDisabled();
        expect(screen.getByRole('button', {name: 'Save'})).toBeDisabled();
    });

    it('shows an error of the whole form at its top', () => {
        renderForm({error: 'This software has been deleted.'});

        expect(screen.getByRole('alert')).toHaveTextContent('This software has been deleted.');
    });

    it('shows extra actions next to the submit button', () => {
        renderForm({actions: <span>Delete</span>});

        expect(screen.getByText('Delete')).toBeInTheDocument();
    });
});
