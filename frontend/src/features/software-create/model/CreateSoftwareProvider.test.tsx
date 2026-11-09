import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {subscribeSystemErrors, type SystemErrorReport} from '@/shared/api';
import {json, renderWithQueries, stubFetch} from '@/shared/lib/test';
import {SoftwareForm} from '@/entities/software';
import {CreateSoftwareProvider} from './CreateSoftwareProvider';

const ID = '00000000-0000-0000-0001-000000000001';

describe(CreateSoftwareProvider, () => {
    const unsubscribers: (() => void)[] = [];

    afterEach(() => {
        vi.unstubAllGlobals();
        unsubscribers.forEach((unsubscribe) => {
            unsubscribe();
        });
        unsubscribers.length = 0;
    });

    it('adds the software and hands its id on', async () => {
        const fetchMock = stubFetch(json({id: ID}, 201));
        const onCreated = vi.fn<(id: string) => void>();
        renderWithQueries(<CreateSoftwareProvider onCreated={onCreated}><SoftwareForm submitLabel="Add software"/></CreateSoftwareProvider>);

        await userEvent.type(screen.getByRole('textbox', {name: 'Title'}), 'Elite');
        await userEvent.click(screen.getByRole('button', {name: 'Add software'}));

        await vi.waitFor(() => {
            expect(onCreated).toHaveBeenCalledExactlyOnceWith(ID);
        });

        expect(fetchMock.mock.calls[0]?.[1]).toMatchObject({method: 'POST', body: '{"title":"Elite","description":null,"releaseDate":null}'});
    });

    it('sends nothing until the title is filled', async () => {
        const fetchMock = stubFetch(json({id: ID}, 201));
        renderWithQueries(<CreateSoftwareProvider onCreated={vi.fn<(id: string) => void>()}><SoftwareForm submitLabel="Add software"/></CreateSoftwareProvider>);

        await userEvent.click(screen.getByRole('button', {name: 'Add software'}));

        expect(fetchMock).not.toHaveBeenCalled();
    });

    it('hands a system error to the error dialog and keeps the form', async () => {
        const reports: SystemErrorReport[] = [];
        unsubscribers.push(subscribeSystemErrors((report) => {
            reports.push(report);
        }));
        stubFetch(json({status: 500}, 500));
        renderWithQueries(<CreateSoftwareProvider onCreated={vi.fn<(id: string) => void>()}><SoftwareForm submitLabel="Add software"/></CreateSoftwareProvider>);

        await userEvent.type(screen.getByRole('textbox', {name: 'Title'}), 'Elite');
        await userEvent.click(screen.getByRole('button', {name: 'Add software'}));

        await vi.waitFor(() => {
            expect(reports).toHaveLength(1);
        });

        expect(screen.getByRole('textbox', {name: 'Title'})).toHaveValue('Elite');
        expect(screen.getByRole('textbox', {name: 'Title'})).toBeEnabled();
    });

    it('shows a refusal of the whole form at its top, not in the error dialog', async () => {
        const reports: SystemErrorReport[] = [];
        unsubscribers.push(subscribeSystemErrors((report) => {
            reports.push(report);
        }));
        stubFetch(json({status: 409, code: 'software.changed'}, 409));
        renderWithQueries(<CreateSoftwareProvider onCreated={vi.fn<(id: string) => void>()}><SoftwareForm submitLabel="Add software"/></CreateSoftwareProvider>);

        await userEvent.type(screen.getByRole('textbox', {name: 'Title'}), 'Elite');
        await userEvent.click(screen.getByRole('button', {name: 'Add software'}));

        await expect(screen.findByRole('alert')).resolves.toHaveTextContent('This item was changed in the meantime. Reload and try again.');
        expect(reports).toHaveLength(0);
    });
});
