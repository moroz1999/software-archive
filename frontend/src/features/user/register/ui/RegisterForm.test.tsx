import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import i18n from 'i18next';
import {subscribeSystemErrors, type SystemErrorReport} from '@/shared/api';
import {json, noContent, renderWithQueries, stubFetchRoutes} from '@/shared/lib/test';
import {RegisterForm} from './RegisterForm';

function show(answer: () => Response = noContent()) {
    const fetchMock = stubFetchRoutes(new Map([['/api/auth/register', answer]]));
    renderWithQueries(<RegisterForm/>);
    return fetchMock;
}

interface Typed {
    readonly name?: string;
    readonly email?: string;
    readonly password?: string;
    readonly repeat?: string;
}

async function fill({name = 'Zed', email = 'zed@example.com', password = 'password1', repeat = password}: Typed = {}) {
    if (name !== '') {
        await userEvent.type(screen.getByRole('textbox', {name: 'User name'}), name);
    }
    if (email !== '') {
        await userEvent.type(screen.getByRole('textbox', {name: 'Email'}), email);
    }
    if (password !== '') {
        await userEvent.type(screen.getByLabelText('Password'), password);
    }
    if (repeat !== '') {
        await userEvent.type(screen.getByLabelText('Repeat the password'), repeat);
    }
    await userEvent.click(screen.getByRole('button', {name: 'Register'}));
}

function posted(fetchMock: ReturnType<typeof stubFetchRoutes>) {
    return fetchMock.mock.calls.filter(([, init]) => init?.method === 'POST');
}

describe(RegisterForm, () => {
    const unsubscribers: (() => void)[] = [];

    afterEach(() => {
        vi.unstubAllGlobals();
        unsubscribers.forEach((unsubscribe) => {
            unsubscribe();
        });
        unsubscribers.length = 0;
    });

    it('registers and tells where the link went', async () => {
        const fetchMock = show();

        await fill();

        await expect(screen.findByText('Almost done: open the link we sent to zed@example.com within 7 days.')).resolves.toBeInTheDocument();
        expect(posted(fetchMock)[0]?.[1]?.body).toBe('{"name":"Zed","email":"zed@example.com","password":"password1","language":"en"}');
        expect(screen.queryByRole('button', {name: 'Register'})).not.toBeInTheDocument();
    });

    it('asks for the letter in the language of the interface', async () => {
        await i18n.changeLanguage('ru');
        const fetchMock = show();

        await userEvent.type(screen.getByRole('textbox', {name: 'Имя пользователя'}), 'Zed');
        await userEvent.type(screen.getByRole('textbox', {name: 'Эл. почта'}), 'zed@example.com');
        await userEvent.type(screen.getByLabelText('Пароль'), 'password1');
        await userEvent.type(screen.getByLabelText('Повторите пароль'), 'password1');
        await userEvent.click(screen.getByRole('button', {name: 'Зарегистрироваться'}));

        await vi.waitFor(() => {
            expect(posted(fetchMock)[0]?.[1]?.body).toBe('{"name":"Zed","email":"zed@example.com","password":"password1","language":"ru"}');
        });
    });

    it('sends nothing until every field is right, and says so at each', async () => {
        const fetchMock = show();

        await fill({name: '', email: 'zed', password: 'short', repeat: 'other'});

        expect(screen.getByRole('textbox', {name: 'User name'})).toHaveAccessibleDescription('Enter a user name.');
        expect(screen.getByRole('textbox', {name: 'Email'})).toHaveAccessibleDescription('Enter an email like name@example.com.');
        expect(screen.getByLabelText('Password')).toHaveAccessibleDescription('Use at least 8 characters.');
        expect(screen.getByLabelText('Repeat the password')).toHaveAccessibleDescription('The passwords differ.');
        expect(posted(fetchMock)).toHaveLength(0);
    });

    it('asks for an email', async () => {
        show();

        await fill({email: ''});

        expect(screen.getByRole('textbox', {name: 'Email'})).toHaveAccessibleDescription('Enter an email.');
    });

    it('says a taken user name under the name', async () => {
        show(json({status: 422, code: 'user.name-taken', errors: [{field: 'name', code: 'user.name-taken'}]}, 422));

        await fill();

        await vi.waitFor(() => {
            expect(screen.getByRole('textbox', {name: 'User name'})).toHaveAccessibleDescription('This user name is taken.');
        });
    });

    it('says a taken email under the email', async () => {
        show(json({status: 422, code: 'user.email-taken', errors: [{field: 'email', code: 'user.email-taken'}]}, 422));

        await fill();

        await vi.waitFor(() => {
            expect(screen.getByRole('textbox', {name: 'Email'})).toHaveAccessibleDescription('An account with this email already exists.');
        });
    });

    it('hands a system error to the error dialog and keeps the form', async () => {
        const reports: SystemErrorReport[] = [];
        unsubscribers.push(subscribeSystemErrors((report) => {
            reports.push(report);
        }));
        show(json({status: 500}, 500));

        await fill();

        await vi.waitFor(() => {
            expect(reports).toHaveLength(1);
        });

        expect(screen.getByRole('textbox', {name: 'User name'})).toHaveValue('Zed');
    });
});
