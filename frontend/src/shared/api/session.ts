import {isObject} from './parse';
import {fetchOrFail, readJson, toError} from './transport';

/** What the browser is: not known yet, a public visitor, or a signed-in user with the body `GET /api/auth/me` gave. */
export type Session =
    | {readonly status: 'unknown'}
    | {readonly status: 'guest'}
    | {readonly status: 'signed-in'; readonly user: unknown};

type Listener = () => void;

const ME_PATH = '/api/auth/me';
const REFRESH_PATH = '/api/auth/refresh';
const LOGOUT_PATH = '/api/auth/logout';

/** The calls that make or end sessions themselves: a 401 of theirs is their answer, not a session to refresh. */
export const SESSION_CALLS: readonly string[] = [
    '/api/auth/login', LOGOUT_PATH, '/api/auth/register', '/api/auth/confirm', '/api/auth/resend-confirmation', REFRESH_PATH,
];

/** The tabs of the archive in this browser tell each other what the session became. */
const CHANNEL = 'archive-session';

const UNKNOWN: Session = {status: 'unknown'};
const GUEST: Session = {status: 'guest'};

let session: Session = UNKNOWN;
const listeners = new Set<Listener>();
/**
 * What changes the session runs one after another: each starts once the one before is done, so no answer of an
 * earlier session lands after a later one.
 */
let queue: Promise<unknown> = Promise.resolve();
let loading: Promise<void> | null = null;
let refreshing: Promise<boolean> | null = null;
// Typed through its constructor: the Node types the test setup is checked with declare it as a value only.
let channel: InstanceType<typeof BroadcastChannel> | null = null;

function readSession(data: unknown): Session | null {
    if (!isObject(data)) {
        return null;
    }
    if (data['status'] === 'guest') {
        return GUEST;
    }
    return data['status'] === 'signed-in' && 'user' in data ? {status: 'signed-in', user: data['user']} : null;
}

/** The channel to the other tabs, opened on first use; jsdom and old browsers may lack it. */
function tabs(): InstanceType<typeof BroadcastChannel> | null {
    if (channel === null && typeof BroadcastChannel !== 'undefined') {
        channel = new BroadcastChannel(CHANNEL);
        channel.onmessage = (event: MessageEvent) => {
            const told = readSession(event.data);
            if (told !== null) {
                become(told, false);
            }
        };
    }
    return channel;
}

function become(next: Session, tell = true): void {
    session = next;
    listeners.forEach((listener) => {
        listener();
    });
    if (tell) {
        tabs()?.postMessage(next);
    }
}

function enqueue<T>(operation: () => Promise<T>): Promise<T> {
    const run = queue.then(operation);
    queue = run.catch(() => undefined);
    return run;
}

function post(path: string, body?: unknown): Promise<Response> {
    return fetchOrFail(path, body === undefined
        ? {method: 'POST', headers: {Accept: 'application/json'}}
        : {method: 'POST', headers: {Accept: 'application/json', 'Content-Type': 'application/json'}, body: JSON.stringify(body)});
}

/** Refreshes the session through its cookie: true once refreshed, false when there is no session to refresh. */
async function refreshNow(): Promise<boolean> {
    const response = await post(REFRESH_PATH);
    if (response.ok) {
        return true;
    }
    if (response.status === 401) {
        return false;
    }
    throw await toError(response);
}

/** Who is signed in now: the archive answers a guest with no content, and asks to refresh a session it holds. */
async function whoIsSignedIn(refresh = true): Promise<Session> {
    const response = await fetchOrFail(ME_PATH, {headers: {Accept: 'application/json'}});
    if (response.status === 200) {
        return {status: 'signed-in', user: await readJson(response)};
    }
    if (response.status === 204) {
        return GUEST;
    }
    if (response.status === 401) {
        return refresh && await refreshNow() ? whoIsSignedIn(false) : GUEST;
    }
    throw await toError(response);
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
export function startSession(path: string, body: unknown): Promise<void> {
    return enqueue(async () => {
        const response = await post(path, body);
        if (!response.ok) {
            throw await toError(response);
        }
        become(await whoIsSignedIn(false));
    });
}

/** Signs out: the archive forgets the session and clears its cookies. */
export function endSession(): Promise<void> {
    return enqueue(async () => {
        const response = await post(LOGOUT_PATH);
        if (!response.ok) {
            throw await toError(response);
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
            become(GUEST);
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
    queue = Promise.resolve();
    loading = null;
    refreshing = null;
    channel?.close();
    channel = null;
}
