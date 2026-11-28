import createClient from 'openapi-fetch';
import type {components, paths} from './schema';
import {API_ORIGIN, apiError, fetchOrFail} from './transport';

type CurrentUserDto = components['schemas']['CurrentUserDto'];
type SessionDto = components['schemas']['SessionDto'];

/** What the browser is: not known yet, a public visitor, or a signed-in user with the body `GET /api/auth/me` gave. */
export type Session =
    | {readonly status: 'unknown'}
    | {readonly status: 'guest'}
    | {readonly status: 'signed-in'; readonly user: CurrentUserDto};

type Listener = () => void;

type SessionMessage =
    | {readonly status: 'guest'}
    | {readonly status: 'signed-in'; readonly user: CurrentUserDto; readonly accessToken: string};


/** The calls that make or end sessions themselves: a 401 of theirs is their answer, not a session to refresh. */
export const SESSION_CALLS: readonly string[] = [
    '/api/auth/login', '/api/auth/logout', '/api/auth/register', '/api/auth/confirm', '/api/auth/resend-confirmation', '/api/auth/refresh',
];

/** The tabs of the archive in this browser tell each other what the session became. */
const CHANNEL = 'archive-session';

const UNKNOWN: Session = {status: 'unknown'};
const GUEST: Session = {status: 'guest'};

let session: Session = UNKNOWN;
let accessToken: string | null = null;
const listeners = new Set<Listener>();
const expiryListeners = new Set<Listener>();
/**
 * What changes the session runs one after another: each starts once the one before is done, so no answer of an
 * earlier session lands after a later one.
 */
let queue: Promise<unknown> = Promise.resolve();
let loading: Promise<void> | null = null;
let refreshing: Promise<boolean> | null = null;
// Typed through its constructor: the Node types the test setup is checked with declare it as a value only.
let channel: InstanceType<typeof BroadcastChannel> | null = null;

/** The channel to the other tabs, opened on first use; jsdom and old browsers may lack it. */
function tabs(): InstanceType<typeof BroadcastChannel> | null {
    if (channel === null && typeof BroadcastChannel !== 'undefined') {
        channel = new BroadcastChannel(CHANNEL);
        channel.onmessage = (event: MessageEvent<SessionMessage>) => {
            // Only the tabs of the archive post on this channel, and only session messages (`become`, `keepAccessToken`).
            const told = event.data;
            if (told.status === 'guest') {
                become(GUEST, false);
            } else {
                accessToken = told.accessToken;
                become({status: 'signed-in', user: told.user}, false);
            }
        };
    }
    return channel;
}

/** Whether both are the session of the same user, as the archive answered who is signed in. */
function sameUser(current: Session, next: Session): boolean {
    return current.status === 'signed-in' && next.status === 'signed-in'
        && JSON.stringify(current.user) === JSON.stringify(next.user);
}

function become(next: Session, tell = true): void {
    if (next.status !== 'signed-in') {
        accessToken = null;
    }
    // The same user with a new access token (another tab restored the session) is no change for what the page shows.
    if (!sameUser(session, next)) {
        session = next;
        listeners.forEach((listener) => {
            listener();
        });
    }
    if (tell) {
        if (next.status === 'signed-in' && accessToken !== null) {
            tabs()?.postMessage({...next, accessToken});
        } else if (next.status === 'guest') {
            tabs()?.postMessage(next);
        }
    }
}

function keepAccessToken(token: string): void {
    accessToken = token;
    if (session.status === 'signed-in') {
        tabs()?.postMessage({...session, accessToken: token});
    }
}

function enqueue<T>(operation: () => Promise<T>): Promise<T> {
    const run = queue.then(operation);
    queue = run.catch(() => undefined);
    return run;
}

/**
 * The calls of the session itself: sent as they are, with no wait for the session and no refresh, and answered with their status, which tells the
 * session what it became.
 */
const sessionApi = createClient<paths>({baseUrl: API_ORIGIN, fetch: (request: Request) => fetchOrFail(withAccessToken(request))});

/** Refreshes the session through its cookie: true once refreshed, false when there is no session to refresh. */
async function refreshNow(): Promise<boolean> {
    const {data, error, response} = await sessionApi.POST('/api/auth/refresh', {fetch: fetchOrFail});
    if (error === undefined) {
        keepAccessToken(data.accessToken);
        return true;
    }
    if (response.status === 401) {
        return false;
    }
    throw apiError(response, error);
}

/** Who is signed in now: the archive answers a guest with no content, and asks to refresh a session it holds. */
async function whoIsSignedIn(refresh = true): Promise<Session> {
    const {data, error, response} = await sessionApi.GET('/api/auth/me');
    if (error !== undefined) {
        if (response.status === 401) {
            return refresh && await refreshNow() ? whoIsSignedIn(false) : GUEST;
        }
        throw apiError(response, error);
    }
    return data === undefined ? GUEST : {status: 'signed-in', user: data};
}

export function getSession(): Session {
    return session;
}

export function subscribeSession(listener: Listener): () => void {
    tabs();
    listeners.add(listener);
    return () => {
        listeners.delete(listener);
    };
}

export function subscribeSessionExpiry(listener: Listener): () => void {
    expiryListeners.add(listener);
    return () => {
        expiryListeners.delete(listener);
    };
}

/** Adds the in-memory access token to an API request, if this tab has one. */
export function withAccessToken(request: Request): Request {
    if (accessToken === null) {
        return request;
    }
    const headers = new Headers(request.headers);
    headers.set('Authorization', `Bearer ${accessToken}`);
    return new Request(request, {headers});
}

/**
 * Settles once the question of who is signed in, if asked, is answered either way: a request made meanwhile goes out
 * with the session the answer refreshed, so it reads what the signed-in user may see.
 */
export function sessionSettled(): Promise<void> {
    return loading === null ? Promise.resolve() : loading.catch(() => undefined);
}

/** Asks who is signed in, once per page load; asked again only after the question failed. */
export function loadSession(): Promise<void> {
    loading ??= enqueue(async () => {
        become(await whoIsSignedIn());
    }).catch((error: unknown) => {
        loading = null;
        throw error;
    });
    return loading;
}

/**
 * Makes a session by a call of its own (sign-in, confirmation), then asks who is signed in. A refused call keeps the
 * session as it was and fails with its error.
 */
export function startSession(
    start: (client: typeof sessionApi) => Promise<{readonly data?: SessionDto; readonly error?: unknown; readonly response: Response}>,
): Promise<void> {
    return enqueue(async () => {
        const {data, error, response} = await start(sessionApi);
        if (data === undefined) {
            throw apiError(response, error);
        }
        keepAccessToken(data.accessToken);
        become(await whoIsSignedIn(false));
    });
}

/** Signs out: the archive forgets the session and clears its refresh cookie. */
export function endSession(): Promise<void> {
    return enqueue(async () => {
        const {error, response} = await sessionApi.POST('/api/auth/logout');
        if (error !== undefined) {
            throw apiError(response, error);
        }
        become(GUEST);
    });
}

/**
 * Refreshes the session for a request refused for want of a signed-in user, after whatever changes the session now;
 * requests refused meanwhile share it. A session that cannot be refreshed is over: the browser becomes a guest.
 */
export function refreshSession(): Promise<boolean> {
    refreshing ??= enqueue(async () => {
        const refreshed = await refreshNow();
        if (!refreshed) {
            const wasSignedIn = session.status === 'signed-in';
            become(GUEST);
            if (wasSignedIn) {
                expiryListeners.forEach((listener) => {
                    listener();
                });
            }
        }
        return refreshed;
    }).finally(() => {
        refreshing = null;
    });
    return refreshing;
}

/** Forgets the session of this page and stops hearing other tabs: every test starts from nothing. */
export function resetSessionForTests(): void {
    session = UNKNOWN;
    accessToken = null;
    queue = Promise.resolve();
    loading = null;
    refreshing = null;
    channel?.close();
    channel = null;
}
