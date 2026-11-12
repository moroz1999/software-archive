import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {AuthorForm, toAuthorId} from '@/entities/authors';
import {subscribeSystemErrors, type SystemErrorReport} from '@/shared/api';
import {json, noContent, renderWithQueries, stubFetch} from '@/shared/lib/test';
import {EditAuthorProvider} from './EditAuthorProvider';

const AUTHOR = {
    id: toAuthorId('00000000-0000-0000-0003-000000000001'),
    name: 'John Doe',
    realName: 'John Doe',
    notes: 'Coder since 1984',
    labels: [],
    locations: [],
    groups: [],
};

describe(EditAuthorProvider, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('starts from the author and saves the changes', async () => {
        const fetchMock = stubFetch(noContent());
        const onSaved = vi.fn<() => void>();
        renderWithQueries(<EditAuthorProvider author={AUTHOR} onSaved={onSaved}><AuthorForm submitLabel="Save"/></EditAuthorProvider>);

        expect(screen.getByRole('textbox', {name: 'Notes'})).toHaveValue('Coder since 1984');

        await userEvent.clear(screen.getByRole('textbox', {name: 'Real name'}));
        await userEvent.type(screen.getByRole('textbox', {name: 'Real name'}), 'John Q. Doe');
        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        await vi.waitFor(() => {
            expect(onSaved).toHaveBeenCalledExactlyOnceWith();
        });

        expect(fetchMock.mock.calls[0]?.[0]).toBe(`/api/authors/${AUTHOR.id}`);
        expect(fetchMock.mock.calls[0]?.[1]).toMatchObject({
            method: 'PUT',
            body: '{"realName":"John Q. Doe","notes":"Coder since 1984","labels":[],"locations":[]}',
        });
    });

    it('shows a refusal of the whole form at its top, not in the error dialog', async () => {
        const reports: SystemErrorReport[] = [];
        const unsubscribe = subscribeSystemErrors((report) => {
            reports.push(report);
        });
        stubFetch(json({status: 409, code: 'author.changed'}, 409));
        renderWithQueries(<EditAuthorProvider author={AUTHOR} onSaved={vi.fn<() => void>()}><AuthorForm submitLabel="Save"/></EditAuthorProvider>);

        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        await expect(screen.findByRole('alert')).resolves.toHaveTextContent('This item was changed in the meantime. Reload and try again.');
        expect(reports).toHaveLength(0);

        unsubscribe();
    });
});
