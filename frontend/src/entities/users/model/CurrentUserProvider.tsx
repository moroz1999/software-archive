import {useQueryClient} from '@tanstack/react-query';
import {type ReactNode, useEffect, useMemo, useRef, useState, useSyncExternalStore} from 'react';
import {getSession, loadSession, reportSystemError, subscribeSession, SystemError} from '@/shared/api';
import {CurrentUserContext, type CurrentUserState} from './current-user-context';
import type {CurrentUser} from './user';

function asSystemError(error: unknown): SystemError {
    return error instanceof SystemError ? error : new SystemError('response', 'Unexpected failure', {cause: error});
}

function load(): void {
    loadSession().catch((error: unknown) => {
        reportSystemError({error: asSystemError(error), retry: load});
    });
}

/**
 * Who is signed in, asked once per page load and kept by the session of the browser: signing in or out here or in
 * another tab, or a session that ends, changes it.
 */
export function CurrentUserProvider({children}: {readonly children: ReactNode}) {
    const queryClient = useQueryClient();
    // Asked while rendering, before the blocks inside read anything: what they read waits for the answer.
    useState(load);
    const session = useSyncExternalStore(subscribeSession, getSession);
    const user: CurrentUser | null = useMemo(() => (session.status === 'signed-in' ? session.user : null), [session]);
    const previous = useRef(session);

    useEffect(() => {
        // What the page shows depends on who reads it: once that changes, it is read again.
        if (previous.current !== session && previous.current.status !== 'unknown') {
            void queryClient.invalidateQueries();
        }
        previous.current = session;
    }, [session, queryClient]);

    const state: CurrentUserState = useMemo(() => ({user, known: session.status !== 'unknown'}), [user, session.status]);
    return <CurrentUserContext value={state}>{children}</CurrentUserContext>;
}
