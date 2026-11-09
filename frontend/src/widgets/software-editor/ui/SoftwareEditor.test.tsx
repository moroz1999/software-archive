import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {json, noContent, renderWithQueries, stubFetch} from '@/shared/lib/test';
import {type SoftwareId, toSoftwareId} from '@/entities/software';
import {SoftwareEditor} from './SoftwareEditor';

const ID = '00000000-0000-0000-0001-000000000001';
const ELITE = {id: ID, title: 'Elite', description: 'Space trading', releaseDate: '1985-09-20', platform: {code: 'vector', name: 'Vector'}, hardware: []};

describe(SoftwareEditor, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('shows a skeleton, then the form with the software and a delete button', async () => {
        stubFetch(json(ELITE));
        renderWithQueries(<SoftwareEditor id={toSoftwareId(ID)} onSaved={vi.fn<(id: SoftwareId) => void>()} onDeleted={vi.fn<() => void>()}/>);

        expect(screen.getByTestId('software-editor-skeleton')).toBeInTheDocument();
        await expect(screen.findByRole('textbox', {name: 'Title'})).resolves.toHaveValue('Elite');
        expect(screen.getByRole('button', {name: 'Delete'})).toBeInTheDocument();
    });

    it('reports the software saved', async () => {
        stubFetch(json(ELITE), noContent());
        const onSaved = vi.fn<(id: SoftwareId) => void>();
        renderWithQueries(<SoftwareEditor id={toSoftwareId(ID)} onSaved={onSaved} onDeleted={vi.fn<() => void>()}/>);

        await screen.findByRole('textbox', {name: 'Title'});
        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        await vi.waitFor(() => {
            expect(onSaved).toHaveBeenCalledExactlyOnceWith(toSoftwareId(ID));
        });
    });

    it('reports the software deleted', async () => {
        stubFetch(json(ELITE), noContent());
        const onDeleted = vi.fn<() => void>();
        renderWithQueries(<SoftwareEditor id={toSoftwareId(ID)} onSaved={vi.fn<(id: SoftwareId) => void>()} onDeleted={onDeleted}/>);

        await screen.findByRole('textbox', {name: 'Title'});
        await userEvent.click(screen.getByRole('button', {name: 'Delete'}));
        await userEvent.click(screen.getByRole('button', {name: 'Delete for good'}));

        await vi.waitFor(() => {
            expect(onDeleted).toHaveBeenCalledExactlyOnceWith();
        });
    });
});
