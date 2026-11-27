import {api, apiError} from '@/shared/api';
import type {UiLanguage} from '@/shared/lib/i18n';

export interface Registration {
    readonly name: string;
    readonly email: string;
    readonly password: string;
    /** The language of the letter with the confirmation link. */
    readonly language: UiLanguage;
}

/** Registers an account: `POST /api/auth/register`; the archive mails the confirmation link. */
export async function register(registration: Registration): Promise<void> {
    const {error, response} = await api.POST('/api/auth/register', {
        body: {name: registration.name, email: registration.email, password: registration.password, language: registration.language},
    });
    if (error !== undefined) {
        throw apiError(response, error);
    }
}
