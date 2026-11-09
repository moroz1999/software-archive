import type {ReactNode} from 'react';
import {describe, expect, it, vi} from 'vitest';
import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {BusinessError} from '@/shared/api';
import type {SoftwareDraft} from '../model/software-form';
import {SoftwareFormProvider, type SoftwareFormProviderProps} from '../model/SoftwareFormProvider';
import {SoftwareForm} from './SoftwareForm';

type FormProps = Omit<SoftwareFormProviderProps, 'onSubmit' | 'children'> & {readonly actions?: ReactNode};

function renderForm({actions, ...props}: FormProps = {}) {
    const onSubmit = vi.fn<(draft: SoftwareDraft) => void>();
    render(
        <SoftwareFormProvider onSubmit={onSubmit} {...props}>
            <SoftwareForm submitLabel="Save" actions={actions}/>
        </SoftwareFormProvider>,
    );
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
});
