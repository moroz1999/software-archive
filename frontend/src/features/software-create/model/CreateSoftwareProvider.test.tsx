import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {subscribeSystemErrors, type SystemErrorReport} from '@/shared/api';
import {json, renderWithQueries, stubFetchRoutes} from '@/shared/lib/test';
import {PlatformSelectionProvider} from '@/entities/platforms';
import {SoftwareForm} from '@/entities/software';
import {CreateSoftwareProvider} from './CreateSoftwareProvider';

const ID = '00000000-0000-0000-0001-000000000001';
const PLATFORMS = json([{code: 'agat', name: 'Agat'}, {code: 'bk', name: 'BK'}]);

function answer(software: () => Response) {
    return stubFetchRoutes(new Map([['/api/platforms', PLATFORMS], ['/api/software', software]]));
}

async function fill(title: string) {
    await userEvent.type(screen.getByRole('textbox', {name: 'Title'}), title);
    await userEvent.selectOptions(screen.getByRole('combobox', {name: 'Platform'}), await screen.findByRole('option', {name: 'BK'}));
}

describe(CreateSoftwareProvider, () => {
    const unsubscribers: (() => void)[] = [];

    afterEach(() => {
        localStorage.clear();
        vi.unstubAllGlobals();
        unsubscribers.forEach((unsubscribe) => {
            unsubscribe();
        });
        unsubscribers.length = 0;
    });

    it('adds the software and hands its id on', async () => {
        const fetchMock = answer(json({id: ID}, 201));
        const onCreated = vi.fn<(id: string) => void>();
        renderWithQueries(<PlatformSelectionProvider><CreateSoftwareProvider onCreated={onCreated}><SoftwareForm submitLabel="Add software"/></CreateSoftwareProvider></PlatformSelectionProvider>);

        await fill('Elite');
        await userEvent.click(screen.getByRole('button', {name: 'Add software'}));

        await vi.waitFor(() => {
            expect(onCreated).toHaveBeenCalledExactlyOnceWith(ID);
        });

        expect(fetchMock.mock.calls.find(([url]) => url === '/api/software')?.[1]).toMatchObject({
            method: 'POST',
            body: '{"title":"Elite","description":null,"releaseDate":null,"platform":"bk"}',
        });
    });

    it('starts on the platform chosen with the switch', async () => {
        localStorage.setItem('archive.platform', 'bk');
        answer(json({id: ID}, 201));
        renderWithQueries(<PlatformSelectionProvider><CreateSoftwareProvider onCreated={vi.fn<(id: string) => void>()}><SoftwareForm submitLabel="Add software"/></CreateSoftwareProvider></PlatformSelectionProvider>);

        await screen.findByRole('option', {name: 'BK'});

        expect(screen.getByRole('combobox', {name: 'Platform'})).toHaveValue('bk');
    });

    it('sends nothing until the title is filled', async () => {
        const fetchMock = answer(json({id: ID}, 201));
        renderWithQueries(<PlatformSelectionProvider><CreateSoftwareProvider onCreated={vi.fn<(id: string) => void>()}><SoftwareForm submitLabel="Add software"/></CreateSoftwareProvider></PlatformSelectionProvider>);

        await userEvent.click(screen.getByRole('button', {name: 'Add software'}));

        expect(fetchMock.mock.calls.map(([url]) => url)).not.toContain('/api/software');
    });

    it('hands a system error to the error dialog and keeps the form', async () => {
        const reports: SystemErrorReport[] = [];
        unsubscribers.push(subscribeSystemErrors((report) => {
            reports.push(report);
        }));
        answer(json({status: 500}, 500));
        renderWithQueries(<PlatformSelectionProvider><CreateSoftwareProvider onCreated={vi.fn<(id: string) => void>()}><SoftwareForm submitLabel="Add software"/></CreateSoftwareProvider></PlatformSelectionProvider>);

        await fill('Elite');
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
        answer(json({status: 409, code: 'software.changed'}, 409));
        renderWithQueries(<PlatformSelectionProvider><CreateSoftwareProvider onCreated={vi.fn<(id: string) => void>()}><SoftwareForm submitLabel="Add software"/></CreateSoftwareProvider></PlatformSelectionProvider>);

        await fill('Elite');
        await userEvent.click(screen.getByRole('button', {name: 'Add software'}));

        await expect(screen.findByRole('alert')).resolves.toHaveTextContent('This item was changed in the meantime. Reload and try again.');
        expect(reports).toHaveLength(0);
    });
});
