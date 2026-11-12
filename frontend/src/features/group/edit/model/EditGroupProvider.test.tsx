import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {GroupForm, toGroupId} from '@/entities/groups';
import {subscribeSystemErrors, type SystemErrorReport} from '@/shared/api';
import {json, noContent, renderWithQueries, stubFetchAround} from '@/shared/lib/test';
import {EditGroupProvider} from './EditGroupProvider';

const GROUP = {
    id: toGroupId('00000000-0000-0000-0005-000000000001'),
    name: 'Crew',
    groupName: 'Crew Ltd',
    notes: 'Since 1990',
    types: ['demogroup'],
    labels: [],
    locations: [],
    members: [],
    memberGroups: [],
    groups: [],
};

const AROUND = new Map([['/api/group-types', json(['crack group', 'demogroup'])]]);

describe(EditGroupProvider, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('starts from the group and saves the changes', async () => {
        const fetchMock = stubFetchAround(AROUND, noContent());
        const onSaved = vi.fn<() => void>();
        renderWithQueries(<EditGroupProvider group={GROUP} onSaved={onSaved}><GroupForm submitLabel="Save"/></EditGroupProvider>);

        expect(screen.getByRole('textbox', {name: 'Notes'})).toHaveValue('Since 1990');
        await expect(screen.findByRole('checkbox', {name: 'demogroup'})).resolves.toBeChecked();

        await userEvent.clear(screen.getByRole('textbox', {name: 'Name'}));
        await userEvent.type(screen.getByRole('textbox', {name: 'Name'}), 'Crew');
        await userEvent.click(screen.getByRole('checkbox', {name: 'crack group'}));
        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        await vi.waitFor(() => {
            expect(onSaved).toHaveBeenCalledExactlyOnceWith();
        });

        expect(fetchMock.mock.calls[0]?.[0]).toBe(`/api/groups/${GROUP.id}`);
        expect(fetchMock.mock.calls[0]?.[1]).toMatchObject({
            method: 'PUT',
            body: '{"name":"Crew","notes":"Since 1990","types":["demogroup","crack group"],"labels":[],"locations":[]}',
        });
    });

    it('shows a refusal of the whole form at its top, not in the error dialog', async () => {
        const reports: SystemErrorReport[] = [];
        const unsubscribe = subscribeSystemErrors((report) => {
            reports.push(report);
        });
        stubFetchAround(AROUND, json({status: 409, code: 'group.changed'}, 409));
        renderWithQueries(<EditGroupProvider group={GROUP} onSaved={vi.fn<() => void>()}><GroupForm submitLabel="Save"/></EditGroupProvider>);

        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        await expect(screen.findByRole('alert')).resolves.toHaveTextContent('This item was changed in the meantime. Reload and try again.');
        expect(reports).toHaveLength(0);

        unsubscribe();
    });
});
