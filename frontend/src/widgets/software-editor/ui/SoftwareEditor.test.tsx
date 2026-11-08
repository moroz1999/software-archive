import {afterEach, describe, expect, it, vi} from 'vitest';
import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {json, noContent, stubFetch} from '../../../shared/lib/test';
import {SoftwareEditor} from './SoftwareEditor';

const ID = '00000000-0000-0000-0001-000000000001';
const ELITE = {id: ID, title: 'Elite', description: 'Space trading', releaseDate: '1985-09-20'};

describe(SoftwareEditor, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('shows a skeleton, then the form with the software and a delete button', async () => {
        stubFetch(json(ELITE));
        render(<SoftwareEditor id={ID} onDone={vi.fn<() => void>()}/>);

        expect(screen.getByTestId('software-editor-skeleton')).toBeInTheDocument();
        await expect(screen.findByRole('textbox', {name: 'Title'})).resolves.toHaveValue('Elite');
        expect(screen.getByRole('button', {name: 'Delete'})).toBeInTheDocument();
    });

    it('says the software is not found', async () => {
        stubFetch(json({status: 404, code: 'software.not-found'}, 404));
        render(<SoftwareEditor id={ID} onDone={vi.fn<() => void>()}/>);

        await expect(screen.findByText('Software not found.')).resolves.toBeInTheDocument();
        expect(screen.queryByRole('textbox')).not.toBeInTheDocument();
    });

    it('treats a malformed id as not found without asking the backend', async () => {
        const fetchMock = stubFetch(json(ELITE));
        render(<SoftwareEditor id="42" onDone={vi.fn<() => void>()}/>);

        await expect(screen.findByText('Software not found.')).resolves.toBeInTheDocument();
        expect(fetchMock).not.toHaveBeenCalled();
    });

    it('is done once the software is saved', async () => {
        stubFetch(json(ELITE), noContent());
        const onDone = vi.fn<() => void>();
        render(<SoftwareEditor id={ID} onDone={onDone}/>);

        await screen.findByRole('textbox', {name: 'Title'});
        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        await vi.waitFor(() => {
            expect(onDone).toHaveBeenCalledExactlyOnceWith();
        });
    });

    it('is done once the software is deleted', async () => {
        stubFetch(json(ELITE), noContent());
        const onDone = vi.fn<() => void>();
        render(<SoftwareEditor id={ID} onDone={onDone}/>);

        await screen.findByRole('textbox', {name: 'Title'});
        await userEvent.click(screen.getByRole('button', {name: 'Delete'}));
        await userEvent.click(screen.getByRole('button', {name: 'Delete for good'}));

        await vi.waitFor(() => {
            expect(onDone).toHaveBeenCalledExactlyOnceWith();
        });
    });
});
