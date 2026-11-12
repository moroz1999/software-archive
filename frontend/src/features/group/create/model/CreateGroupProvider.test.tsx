import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {GroupForm} from '@/entities/groups';
import {subscribeSystemErrors, type SystemErrorReport} from '@/shared/api';
import {json, renderWithQueries, stubFetchRoutes} from '@/shared/lib/test';
import {CreateGroupProvider} from './CreateGroupProvider';

const ID = '00000000-0000-0000-0005-000000000001';

function showForm(created = json({id: ID}, 201), onCreated = vi.fn<(id: string) => void>()) {
    const fetchMock = stubFetchRoutes(new Map([
        ['/api/group-types', json(['crack group', 'demogroup'])],
        ['/api/groups', created],
    ]));
    renderWithQueries(<CreateGroupProvider onCreated={onCreated}><GroupForm submitLabel="Add group"/></CreateGroupProvider>);
    return {fetchMock, onCreated};
}

async function fillCrew() {
    await userEvent.type(screen.getByRole('textbox', {name: 'Name'}), 'Crew Ltd');
    await userEvent.click(await screen.findByRole('checkbox', {name: 'demogroup'}));
}

describe(CreateGroupProvider, () => {
    const unsubscribers: (() => void)[] = [];

    afterEach(() => {
        vi.unstubAllGlobals();
        unsubscribers.forEach((unsubscribe) => {
            unsubscribe();
        });
        unsubscribers.length = 0;
    });

    it('adds the group and hands its id on', async () => {
        const {fetchMock, onCreated} = showForm();

        await fillCrew();
        await userEvent.click(screen.getByRole('button', {name: 'Add group'}));

        await vi.waitFor(() => {
            expect(onCreated).toHaveBeenCalledExactlyOnceWith(ID);
        });

        const post = fetchMock.mock.calls.find(([, init]) => init?.method === 'POST');

        expect(post?.[1]?.body).toBe('{"name":"Crew Ltd","notes":null,"types":["demogroup"],"labels":[],"locations":[]}');
    });

    it('sends nothing without a name and a type and says so at each', async () => {
        const {fetchMock} = showForm();

        await screen.findByRole('checkbox', {name: 'demogroup'});
        await userEvent.click(screen.getByRole('button', {name: 'Add group'}));

        expect(screen.getByRole('textbox', {name: 'Name'})).toHaveAccessibleDescription('Enter a title.');
        expect(screen.getByRole('group', {name: 'Types'})).toHaveAccessibleDescription('Choose at least one type.');
        expect(fetchMock.mock.calls.some(([, init]) => init?.method === 'POST')).toBe(false);
    });

    it('says why the types were refused under the types', async () => {
        showForm(json({status: 422, code: 'group.type-unknown', errors: [{field: 'types', code: 'group.type-unknown'}]}, 422));

        await fillCrew();
        await userEvent.click(screen.getByRole('button', {name: 'Add group'}));

        await vi.waitFor(() => {
            expect(screen.getByRole('group', {name: 'Types'})).toHaveAccessibleDescription('One of the types is not in the archive any more. Reload and try again.');
        });
    });

    it('hands a system error to the error dialog and keeps the form', async () => {
        const reports: SystemErrorReport[] = [];
        unsubscribers.push(subscribeSystemErrors((report) => {
            reports.push(report);
        }));
        showForm(json({status: 500}, 500));

        await fillCrew();
        await userEvent.click(screen.getByRole('button', {name: 'Add group'}));

        await vi.waitFor(() => {
            expect(reports).toHaveLength(1);
        });

        expect(screen.getByRole('textbox', {name: 'Name'})).toHaveValue('Crew Ltd');
        expect(screen.getByRole('checkbox', {name: 'demogroup'})).toBeChecked();
    });
});
