import {describe, expect, it, vi} from 'vitest';
import {createEvent, fireEvent, render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {FileUpload} from './FileUpload';

const PNG = new File(['png'], 'title.png', {type: 'image/png'});
const GIF = new File(['gif'], 'game.gif', {type: 'image/gif'});

function drop(target: HTMLElement, files: readonly File[]) {
    const event = createEvent.drop(target);
    Object.defineProperty(event, 'dataTransfer', {value: {files, types: ['Files']}});
    fireEvent(target, event);
}

describe(FileUpload, () => {
    it('hands over the files chosen through its button', async () => {
        const onFiles = vi.fn<(files: readonly File[]) => void>();
        render(<FileUpload label="Add screenshots" accept="image/png,image/gif" multiple onFiles={onFiles}/>);

        await userEvent.upload(screen.getByLabelText('Add screenshots'), [PNG, GIF]);

        expect(onFiles).toHaveBeenCalledExactlyOnceWith([PNG, GIF]);
        expect(screen.getByLabelText('Add screenshots')).toHaveAttribute('accept', 'image/png,image/gif');
        expect(screen.getByLabelText('Add screenshots')).toHaveAttribute('multiple');
    });

    it('opens the choice of files by its button', async () => {
        render(<FileUpload label="Add screenshots" onFiles={vi.fn<(files: readonly File[]) => void>()}/>);
        const input = screen.getByLabelText('Add screenshots');
        const click = vi.spyOn(input, 'click');

        await userEvent.click(screen.getByRole('button', {name: 'Add screenshots'}));

        expect(click).toHaveBeenCalledExactlyOnceWith();
    });

    it('hands over the files dropped on it', () => {
        const onFiles = vi.fn<(files: readonly File[]) => void>();
        render(<FileUpload label="Add screenshots" multiple onFiles={onFiles}/>);

        drop(screen.getByRole('button', {name: 'Add screenshots'}), [PNG, GIF]);

        expect(onFiles).toHaveBeenCalledExactlyOnceWith([PNG, GIF]);
    });

    it('takes nothing while disabled', () => {
        const onFiles = vi.fn<(files: readonly File[]) => void>();
        render(<FileUpload label="Add screenshots" disabled onFiles={onFiles}/>);

        drop(screen.getByRole('button', {name: 'Add screenshots'}), [PNG]);

        expect(screen.getByRole('button', {name: 'Add screenshots'})).toBeDisabled();
        expect(screen.getByLabelText('Add screenshots')).toBeDisabled();
        expect(onFiles).not.toHaveBeenCalled();
    });

    it('lets the same file be chosen again', async () => {
        const onFiles = vi.fn<(files: readonly File[]) => void>();
        render(<FileUpload label="Add screenshots" onFiles={onFiles}/>);

        await userEvent.upload(screen.getByLabelText('Add screenshots'), PNG);
        await userEvent.upload(screen.getByLabelText('Add screenshots'), PNG);

        expect(onFiles).toHaveBeenCalledTimes(2);
    });
});
