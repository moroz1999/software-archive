import {api, apiError, endSession, startSession} from '@/shared/api';
import type {UiLanguage} from '@/shared/lib/i18n';

export interface Credentials {
    /** The user name or the email. */
    readonly login: string;
    readonly password: string;
}

/** Signs in: `POST /api/auth/login`; the session comes back in cookies, and the browser learns who signed in. */
export async function logIn(credentials: Credentials & {readonly remember: boolean}): Promise<void> {
    await startSession((client) => client.POST('/api/auth/login', {
        body: {login: credentials.login, password: credentials.password, remember: credentials.remember},
    }));
}

/** Signs out: the archive forgets the session and clears its cookies. */
export async function logOut(): Promise<void> {
    await endSession();
}

/** Sends a new confirmation link to an unconfirmed user: `POST /api/auth/resend-confirmation`. */
export async function resendConfirmation(credentials: Credentials & {readonly language: UiLanguage}): Promise<void> {
    const {error, response} = await api.POST('/api/auth/resend-confirmation', {
        body: {login: credentials.login, password: credentials.password, language: credentials.language},
    });
    if (error !== undefined) {
        throw apiError(response, error);
    }
}
