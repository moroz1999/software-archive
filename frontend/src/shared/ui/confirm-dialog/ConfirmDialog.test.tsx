import {describe, expect, it, vi} from 'vitest';
import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {ConfirmDialog} from './ConfirmDialog';

function renderDialog(props: {busy?: boolean; error?: string} = {}) {
    const onConfirm = vi.fn<() => void>();
    const onCancel = vi.fn<() => void>();
    render(
        <ConfirmDialog open title="Delete software" confirmLabel="Delete" onConfirm={onConfirm} onCancel={onCancel} {...props}>
            Delete “Elite”?
        </ConfirmDialog>,
    );
    return {onConfirm, onCancel};
}

describe(ConfirmDialog, () => {
    it('asks the question and confirms', async () => {
        const {onConfirm} = renderDialog();

        expect(screen.getByRole('alertdialog', {name: 'Delete software'})).toHaveTextContent('Delete “Elite”?');

        await userEvent.click(screen.getByRole('button', {name: 'Delete'}));

        expect(onConfirm).toHaveBeenCalledExactlyOnceWith();
    });

    it('cancels', async () => {
        const {onCancel} = renderDialog();

        await userEvent.click(screen.getByRole('button', {name: 'Cancel'}));

        expect(onCancel).toHaveBeenCalledExactlyOnceWith();
    });

    it('locks its buttons while the action runs', () => {
        renderDialog({busy: true});

        expect(screen.getByRole('button', {name: 'Delete'})).toBeDisabled();
        expect(screen.getByRole('button', {name: 'Cancel'})).toBeDisabled();
    });

    it('shows a business error of the action inside', () => {
        renderDialog({error: 'This software has already been deleted.'});

        expect(screen.getByRole('alert')).toHaveTextContent('This software has already been deleted.');
    });
});
