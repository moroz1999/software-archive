import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen, within} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {toUserId, type UserEntry} from '@/entities/users';
import {subscribeSystemErrors, type SystemErrorReport} from '@/shared/api';
import {json, noContent, renderWithQueries, stubFetchRoutes} from '@/shared/lib/test';
import {EditUserProvider} from '../model/EditUserProvider';
import {EditUserForm} from './EditUserForm';

const BOB: UserEntry = {
    id: toUserId('00000000-0000-0000-0009-000000000003'),
    name: 'Bob',
    email: 'bob@example.com',
    confirmed: true,
    registeredAt: '2026-01-15T10:00:00Z',
    groups: ['member', 'moderator'],
};
const AXE: UserEntry = {
    id: toUserId('00000000-0000-0000-0009-000000000002'),
    name: 'Axe',
    email: 'axe@example.com',
    confirmed: false,
    registeredAt: '2026-02-01T10:00:00Z',
    groups: [],
};

function show(user: UserEntry, answer: () => Response = noContent()) {
    const fetchMock = stubFetchRoutes(new Map([[`/api/users/${user.id}`, answer]]));
    const onSaved = vi.fn<() => void>();
    renderWithQueries(<EditUserProvider user={user} onSaved={onSaved}><EditUserForm/></EditUserProvider>);
    return {fetchMock, onSaved};
}

function sent(fetchMock: ReturnType<typeof stubFetchRoutes>) {
    return fetchMock.mock.calls.filter(([, init]) => init?.method === 'PUT').map(([, init]) => init?.body);
}

function refusal(code: string, status = 422, field?: string) {
    return json({status, code, ...(field === undefined ? {} : {errors: [{field, code}]})}, status);
}

async function save() {
    await userEvent.click(screen.getByRole('button', {name: 'Save'}));
}

describe(EditUserForm, () => {
    const unsubscribers: (() => void)[] = [];

    afterEach(() => {
        vi.unstubAllGlobals();
        unsubscribers.forEach((unsubscribe) => {
            unsubscribe();
        });
        unsubscribers.length = 0;
    });

    it('starts from the name, the email and the groups of the user', () => {
        show(BOB);

        expect(screen.getByRole('textbox', {name: 'User name'})).toHaveValue('Bob');
        expect(screen.getByRole('textbox', {name: 'Email'})).toHaveValue('bob@example.com');

        const groups = within(screen.getByRole('region', {name: 'Groups'}));

        expect(groups.getByRole('checkbox', {name: 'Administrator'})).not.toBeChecked();
        expect(groups.getByRole('checkbox', {name: 'Moderator'})).toBeChecked();
        expect(groups.getByText('Every confirmed user is a member.')).toBeInTheDocument();
    });

    it('saves the name, the email and the groups chosen', async () => {
        const {fetchMock, onSaved} = show(BOB);

        await userEvent.clear(screen.getByRole('textbox', {name: 'User name'}));
        await userEvent.type(screen.getByRole('textbox', {name: 'User name'}), 'Rob');
        await userEvent.clear(screen.getByRole('textbox', {name: 'Email'}));
        await userEvent.type(screen.getByRole('textbox', {name: 'Email'}), 'rob@example.com');
        await userEvent.click(screen.getByRole('checkbox', {name: 'Administrator'}));
        await userEvent.click(screen.getByRole('checkbox', {name: 'Moderator'}));
        await save();

        await vi.waitFor(() => {
            expect(onSaved).toHaveBeenCalledExactlyOnceWith();
        });

        expect(sent(fetchMock)).toStrictEqual(['{"name":"Rob","email":"rob@example.com","groups":["admin"]}']);
    });

    it('gives and takes the AI assistant by its own checkbox, keeping it for one who has it', async () => {
        const {fetchMock, onSaved} = show({...BOB, groups: ['member', 'ai-assistant']});

        expect(screen.getByRole('checkbox', {name: 'AI assistant'})).toBeChecked();
        expect(screen.getByRole('checkbox', {name: 'Moderator'})).not.toBeChecked();

        await userEvent.click(screen.getByRole('checkbox', {name: 'Moderator'}));
        await save();

        await vi.waitFor(() => {
            expect(onSaved).toHaveBeenCalledExactlyOnceWith();
        });

        await userEvent.click(screen.getByRole('checkbox', {name: 'AI assistant'}));
        await save();

        await vi.waitFor(() => {
            expect(onSaved).toHaveBeenCalledTimes(2);
        });

        expect(sent(fetchMock)).toStrictEqual([
            '{"name":"Bob","email":"bob@example.com","groups":["moderator","ai-assistant"]}',
            '{"name":"Bob","email":"bob@example.com","groups":["moderator"]}',
        ]);
    });

    it('offers no groups to an unconfirmed account and saves it with none', async () => {
        const {fetchMock, onSaved} = show(AXE);

        expect(screen.queryByRole('checkbox')).not.toBeInTheDocument();
        expect(screen.getByText('An account gets its groups once it is confirmed.')).toBeInTheDocument();

        await userEvent.type(screen.getByRole('textbox', {name: 'User name'}), 's');
        await save();

        await vi.waitFor(() => {
            expect(onSaved).toHaveBeenCalledExactlyOnceWith();
        });

        expect(sent(fetchMock)).toStrictEqual(['{"name":"Axes","email":"axe@example.com","groups":[]}']);
    });

    it('sends nothing until the name and the email are right, and says so at each', async () => {
        const {fetchMock} = show(BOB);

        await userEvent.clear(screen.getByRole('textbox', {name: 'User name'}));
        await userEvent.clear(screen.getByRole('textbox', {name: 'Email'}));
        await userEvent.type(screen.getByRole('textbox', {name: 'Email'}), 'bob');
        await save();

        expect(screen.getByRole('textbox', {name: 'User name'})).toHaveAccessibleDescription('Enter a user name.');
        expect(screen.getByRole('textbox', {name: 'Email'})).toHaveAccessibleDescription('Enter an email like name@example.com.');
        expect(sent(fetchMock)).toHaveLength(0);
    });

    it('says a taken user name under the name until it is edited', async () => {
        show(BOB, refusal('user.name-taken', 422, 'name'));

        await save();

        await vi.waitFor(() => {
            expect(screen.getByRole('textbox', {name: 'User name'})).toHaveAccessibleDescription('This user name is taken.');
        });

        await userEvent.type(screen.getByRole('textbox', {name: 'User name'}), 'by');

        expect(screen.getByRole('textbox', {name: 'User name'})).not.toHaveAccessibleDescription();
    });

    it('says a taken email under the email', async () => {
        show(BOB, refusal('user.email-taken', 422, 'email'));

        await save();

        await vi.waitFor(() => {
            expect(screen.getByRole('textbox', {name: 'Email'})).toHaveAccessibleDescription('An account with this email already exists.');
        });
    });

    it('says at the top that the last administrator stays one', async () => {
        show(BOB, refusal('user.last-admin'));

        await save();

        await expect(screen.findByText('The archive keeps at least one administrator.')).resolves.toBeInTheDocument();
    });

    it('says at the top that an unconfirmed account gets no groups', async () => {
        show(AXE, refusal('user.unconfirmed-groups'));

        await save();

        await expect(screen.findByText('An unconfirmed account gets no groups.')).resolves.toBeInTheDocument();
    });

    it('says at the top that only administrators manage users', async () => {
        show(BOB, refusal('access.denied', 403));

        await save();

        await expect(screen.findByText('Only administrators manage users.')).resolves.toBeInTheDocument();
    });

    it('hands a system error to the error dialog and keeps the form', async () => {
        const reports: SystemErrorReport[] = [];
        unsubscribers.push(subscribeSystemErrors((report) => {
            reports.push(report);
        }));
        const {onSaved} = show(BOB, json({status: 500}, 500));

        await userEvent.type(screen.getByRole('textbox', {name: 'User name'}), 'by');
        await save();

        await vi.waitFor(() => {
            expect(reports).toHaveLength(1);
        });

        expect(screen.getByRole('textbox', {name: 'User name'})).toHaveValue('Bobby');
        expect(onSaved).not.toHaveBeenCalled();
    });
});
