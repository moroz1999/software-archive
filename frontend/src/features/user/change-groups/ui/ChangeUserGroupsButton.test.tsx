import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen, within} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {toUserId, type UserEntry, USERS_QUERY_KEY} from '@/entities/users';
import {createQueryClient, subscribeSystemErrors, type SystemErrorReport} from '@/shared/api';
import {json, noContent, renderWithQueries, stubFetch} from '@/shared/lib/test';
import {ChangeUserGroupsButton} from './ChangeUserGroupsButton';

const BOB: UserEntry = {
    id: toUserId('00000000-0000-0000-0009-000000000003'),
    name: 'Bob',
    email: 'bob@example.com',
    confirmed: true,
    registeredAt: '2026-01-15T10:00:00Z',
    groups: ['admin', 'member'],
};

async function openDialog(): Promise<HTMLElement> {
    await userEvent.click(screen.getByRole('button', {name: 'Change groups of Bob'}));
    return screen.getByRole('dialog', {name: 'Groups of Bob'});
}

async function saveWithModerator(): Promise<HTMLElement> {
    const dialog = await openDialog();
    await userEvent.click(within(dialog).getByRole('checkbox', {name: 'Moderator'}));
    await userEvent.click(within(dialog).getByRole('button', {name: 'Save'}));
    return dialog;
}

describe(ChangeUserGroupsButton, () => {
    const unsubscribers: (() => void)[] = [];

    afterEach(() => {
        vi.unstubAllGlobals();
        unsubscribers.forEach((unsubscribe) => {
            unsubscribe();
        });
        unsubscribers.length = 0;
    });

    it('offers the groups given on purpose, those of the user checked', async () => {
        stubFetch(noContent());
        renderWithQueries(<ChangeUserGroupsButton user={BOB}/>);

        const dialog = await openDialog();

        expect(within(dialog).getByRole('checkbox', {name: 'Administrator'})).toBeChecked();
        expect(within(dialog).getByRole('checkbox', {name: 'Moderator'})).not.toBeChecked();
        expect(within(dialog).getByText('Every confirmed user is a member.')).toBeInTheDocument();
    });

    it('gives the user the groups checked, then closes and has the list of users read again', async () => {
        const fetchMock = stubFetch(noContent());
        const queryClient = createQueryClient();
        queryClient.setQueryData([...USERS_QUERY_KEY, 'list'], 'shown');
        renderWithQueries(<ChangeUserGroupsButton user={BOB}/>, queryClient);

        const dialog = await openDialog();
        await userEvent.click(within(dialog).getByRole('checkbox', {name: 'Administrator'}));
        await userEvent.click(within(dialog).getByRole('checkbox', {name: 'Moderator'}));
        await userEvent.click(within(dialog).getByRole('button', {name: 'Save'}));

        await vi.waitFor(() => {
            expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
        });

        expect(fetchMock.mock.calls[0]?.[0]).toBe(`/api/users/${BOB.id}/groups`);
        expect(fetchMock.mock.calls[0]?.[1]).toMatchObject({method: 'PUT', body: '{"groups":["moderator"]}'});
        expect(queryClient.getQueryState([...USERS_QUERY_KEY, 'list'])?.isInvalidated).toBe(true);
    });

    it('changes nothing when cancelled, and opens again with the groups of the user', async () => {
        const fetchMock = stubFetch(noContent());
        renderWithQueries(<ChangeUserGroupsButton user={BOB}/>);

        const dialog = await openDialog();
        await userEvent.click(within(dialog).getByRole('checkbox', {name: 'Moderator'}));
        await userEvent.click(within(dialog).getByRole('button', {name: 'Cancel'}));

        expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
        expect(fetchMock).not.toHaveBeenCalled();
        await expect(openDialog()).resolves.toBeDefined();
        expect(screen.getByRole('checkbox', {name: 'Moderator'})).not.toBeChecked();
    });

    it('offers no groups to an unconfirmed account', () => {
        renderWithQueries(<ChangeUserGroupsButton user={{...BOB, confirmed: false, groups: []}}/>);

        expect(screen.queryByRole('button', {name: 'Change groups of Bob'})).not.toBeInTheDocument();
    });

    it('says inside the dialog that the archive keeps its last administrator', async () => {
        const reports: SystemErrorReport[] = [];
        unsubscribers.push(subscribeSystemErrors((report) => {
            reports.push(report);
        }));
        stubFetch(json({status: 422, code: 'user.last-admin'}, 422));
        renderWithQueries(<ChangeUserGroupsButton user={BOB}/>);

        const dialog = await saveWithModerator();

        await expect(within(dialog).findByRole('alert')).resolves.toHaveTextContent('The archive keeps at least one administrator.');
        expect(reports).toHaveLength(0);
    });

    it('says inside the dialog that an unconfirmed account gets no groups', async () => {
        stubFetch(json({status: 422, code: 'user.unconfirmed-groups'}, 422));
        renderWithQueries(<ChangeUserGroupsButton user={BOB}/>);

        const dialog = await saveWithModerator();

        await expect(within(dialog).findByRole('alert')).resolves.toHaveTextContent('An unconfirmed account gets no groups.');
    });

    it('says who manages users when the archive refuses', async () => {
        stubFetch(json({status: 403, code: 'access.denied'}, 403));
        renderWithQueries(<ChangeUserGroupsButton user={BOB}/>);

        const dialog = await saveWithModerator();

        await expect(within(dialog).findByRole('alert')).resolves.toHaveTextContent('Only administrators manage users.');
    });

    it('tells a signed-out administrator to sign in', async () => {
        stubFetch(json({status: 401, code: 'auth.required'}, 401));
        renderWithQueries(<ChangeUserGroupsButton user={BOB}/>);

        const dialog = await saveWithModerator();

        await expect(within(dialog).findByRole('alert')).resolves.toHaveTextContent('Sign in to change the archive.');
    });

    it('hands a system error to the error dialog and keeps the dialog open', async () => {
        const reports: SystemErrorReport[] = [];
        unsubscribers.push(subscribeSystemErrors((report) => {
            reports.push(report);
        }));
        stubFetch(json({status: 500}, 500));
        renderWithQueries(<ChangeUserGroupsButton user={BOB}/>);

        const dialog = await saveWithModerator();

        await vi.waitFor(() => {
            expect(reports).toHaveLength(1);
        });

        expect(within(dialog).queryByRole('alert')).not.toBeInTheDocument();
        expect(screen.getByRole('dialog', {name: 'Groups of Bob'})).toBeInTheDocument();
    });
});
