import {startSession} from '@/shared/api';

/** Confirms an account by the token of its link: `POST /api/auth/confirm`; the user comes back signed in. */
export async function confirmEmail(token: string): Promise<void> {
    await startSession((client) => client.POST('/api/auth/confirm', {body: {token}}));
}
