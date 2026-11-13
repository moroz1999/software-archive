import {useQueryClient} from '@tanstack/react-query';
import {type ReactNode, useEffect, useMemo, useRef, useState, useSyncExternalStore} from 'react';
import {getSession, loadSession, reportSystemError, type Session, subscribeSession, SystemError} from '@/shared/api';
import {readCurrentUser} from '../api/user-api';
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

/** The user of a session, or why its answer could not be read: then nobody is taken for signed in. */
function userOf(session: Session): {readonly user: CurrentUser | null; readonly unreadable: SystemError | null} {
    if (session.status !== 'signed-in') {
        return {user: null, unreadable: null};
    }
    try {
        return {user: readCurrentUser(session.user), unreadable: null};
    } catch (error) {
        return {user: null, unreadable: asSystemError(error)};
    }
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
    const {user, unreadable} = useMemo(() => userOf(session), [session]);
    const previous = useRef(session);

    useEffect(() => {
        // What the page shows depends on who reads it: once that changes, it is read again.
        if (previous.current !== session && previous.current.status !== 'unknown') {
            void queryClient.invalidateQueries();
        }
        previous.current = session;
    }, [session, queryClient]);
    useEffect(() => {
        if (unreadable !== null) {
            reportSystemError({error: unreadable});
        }
    }, [unreadable]);

    const state: CurrentUserState = useMemo(() => ({user, known: session.status !== 'unknown'}), [user, session.status]);
    return <CurrentUserContext value={state}>{children}</CurrentUserContext>;
}
