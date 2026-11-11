import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {AuthorForm} from '@/entities/authors';
import {subscribeSystemErrors, type SystemErrorReport} from '@/shared/api';
import {json, renderWithQueries, stubFetch} from '@/shared/lib/test';
import {CreateAuthorProvider} from './CreateAuthorProvider';

const ID = '00000000-0000-0000-0003-000000000001';

function showForm(onCreated = vi.fn<(id: string) => void>()) {
    renderWithQueries(<CreateAuthorProvider onCreated={onCreated}><AuthorForm submitLabel="Add author"/></CreateAuthorProvider>);
    return onCreated;
}

describe(CreateAuthorProvider, () => {
    const unsubscribers: (() => void)[] = [];

    afterEach(() => {
        vi.unstubAllGlobals();
        unsubscribers.forEach((unsubscribe) => {
            unsubscribe();
        });
        unsubscribers.length = 0;
    });

    it('adds the author and hands their id on', async () => {
        const fetchMock = stubFetch(json({id: ID}, 201));
        const onCreated = showForm();

        await userEvent.type(screen.getByRole('textbox', {name: 'Real name'}), 'John Doe');
        await userEvent.click(screen.getByRole('button', {name: 'Add author'}));

        await vi.waitFor(() => {
            expect(onCreated).toHaveBeenCalledExactlyOnceWith(ID);
        });

        expect(fetchMock.mock.calls[0]?.[1]).toMatchObject({
            method: 'POST',
            body: '{"realName":"John Doe","notes":null,"labels":[],"locations":[]}',
        });
    });

    it('sends nothing for an author without any name and says so under the real name', async () => {
        const fetchMock = stubFetch(json({id: ID}, 201));
        showForm();

        await userEvent.click(screen.getByRole('button', {name: 'Add author'}));

        expect(screen.getByRole('textbox', {name: 'Real name'})).toHaveAccessibleDescription('Enter a real name or add a label.');
        expect(fetchMock).not.toHaveBeenCalled();
    });

    it('says why the author was refused under the real name', async () => {
        stubFetch(json({status: 422, code: 'author.nameless', errors: [{field: 'realName', code: 'author.nameless'}]}, 422));
        showForm();

        await userEvent.type(screen.getByRole('textbox', {name: 'Real name'}), 'John Doe');
        await userEvent.click(screen.getByRole('button', {name: 'Add author'}));

        await vi.waitFor(() => {
            expect(screen.getByRole('textbox', {name: 'Real name'})).toHaveAccessibleDescription('Enter a real name or add a label.');
        });
    });

    it('hands a system error to the error dialog and keeps the form', async () => {
        const reports: SystemErrorReport[] = [];
        unsubscribers.push(subscribeSystemErrors((report) => {
            reports.push(report);
        }));
        stubFetch(json({status: 500}, 500));
        showForm();

        await userEvent.type(screen.getByRole('textbox', {name: 'Real name'}), 'John Doe');
        await userEvent.click(screen.getByRole('button', {name: 'Add author'}));

        await vi.waitFor(() => {
            expect(reports).toHaveLength(1);
        });

        expect(screen.getByRole('textbox', {name: 'Real name'})).toHaveValue('John Doe');
        expect(screen.getByRole('textbox', {name: 'Real name'})).toBeEnabled();
    });
});
