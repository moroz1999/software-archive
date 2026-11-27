import {afterEach, describe, expect, it, vi} from 'vitest';
import {AuthenticationRequiredError} from './errors';
import {api} from './http';
import {endSession, getSession, loadSession, startSession, subscribeSession} from './session';
import {apiError} from './transport';

type Answer = readonly [status: number, body?: unknown];

/** The calls that make or end sessions themselves. */
const SESSION_CALLS = {
    '/api/auth/login': () => api.POST('/api/auth/login', {body: {login: 'Zed', password: 'secret', remember: false}}),
    '/api/auth/logout': () => api.POST('/api/auth/logout'),
    '/api/auth/register': () => api.POST('/api/auth/register', {body: {name: 'Zed', email: 'zed@example.com', password: 'secret', language: 'en'}}),
    '/api/auth/confirm': () => api.POST('/api/auth/confirm', {body: {token: 'secret'}}),
    '/api/auth/resend-confirmation': () => api.POST('/api/auth/resend-confirmation', {body: {login: 'Zed', password: 'secret', language: 'en'}}),
};

/** An answer the test gives when it chooses. */
function later() {
    let give: (answer: Answer) => void = () => undefined;
    const answer = new Promise<Answer>((resolve) => {
        give = resolve;
    });
    return {answer, give};
}

/** Answers every path with its own answers in order, the last one repeating; an answer may wait for the test. */
function respond(answers: Readonly<Record<string, readonly (Answer | Promise<Answer>)[]>>) {
    const calls = new Map<string, number>();
    const fetchMock = vi.fn<typeof fetch>(async (input) => {
        const path = pathOf(input);
        const list = answers[path];
        if (list === undefined) {
            throw new TypeError(`Unexpected fetch: ${path}`);
        }
        const index = calls.get(path) ?? 0;
        calls.set(path, index + 1);
        const [status, body] = await (list[Math.min(index, list.length - 1)] ?? [500]);
        return new Response(body === undefined ? null : JSON.stringify(body), {
            status,
            headers: {'Content-Type': status < 400 ? 'application/json' : 'application/problem+json'},
        });
    });
    vi.stubGlobal('fetch', fetchMock);
    return fetchMock;
}

function pathOf(input: string | URL | Request): string {
    return new URL(input instanceof Request ? input.url : input, globalThis.location.origin).pathname;
}

/** The headers a call went out with. */
function headersOf([input, init]: Parameters<typeof fetch>): Headers {
    return input instanceof Request ? input.headers : new Headers(init?.headers);
}

/** Reads what a page reads, failing as the page fails: with the error of the answer. */
async function read(path: '/api/platforms' | '/api/categories'): Promise<unknown> {
    const {data, error, response} = await api.GET(path);
    if (error !== undefined) {
        throw apiError(response, error);
    }
    return data;
}

/** Signs Zed in. */
function signIn(): Promise<void> {
    return startSession((client) => client.POST('/api/auth/login', {body: {login: 'Zed', password: 'secret', remember: false}}));
}

function paths(fetchMock: ReturnType<typeof respond>): string[] {
    return fetchMock.mock.calls.map(([input]) => pathOf(input));
}

function authorization(fetchMock: ReturnType<typeof respond>, path: string, occurrence = 0): string | null {
    const call = fetchMock.mock.calls.filter(([input]) => pathOf(input) === path)[occurrence];
    return call === undefined ? null : headersOf(call).get('Authorization');
}

const ZED = {id: '00000000-0000-0000-0009-000000000001', name: 'Zed', email: 'zed@example.com', groups: ['member'], abilities: []};
const ADA = {id: '00000000-0000-0000-0009-000000000002', name: 'Ada', email: 'ada@example.com', groups: ['member'], abilities: []};
const GUEST: Answer = [204];
const SIGNED_IN: Answer = [200, ZED];
const REQUIRED: Answer = [401, {status: 401, code: 'auth.required'}];
const EXPIRED: Answer = [401, {status: 401, code: 'auth.session-expired'}];
const ACCESS: Answer = [200, {accessToken: 'access-jwt'}];
const NEW_ACCESS: Answer = [200, {accessToken: 'new-access-jwt'}];

/** Another tab of the archive in the same browser. */
function otherTab() {
    const channel = new BroadcastChannel('archive-session');
    const heard: unknown[] = [];
    channel.onmessage = (event: MessageEvent) => {
        heard.push(event.data);
    };
    return {channel, heard};
}

describe('the session of the browser', () => {
    const channels: BroadcastChannel[] = [];

    afterEach(() => {
        vi.unstubAllGlobals();
        channels.forEach((channel) => {
            channel.close();
        });
        channels.length = 0;
    });

    it('is not known before it is asked', () => {
        expect(getSession()).toStrictEqual({status: 'unknown'});
    });

    it('is a guest without a session, asked once and never refreshed', async () => {
        const fetchMock = respond({'/api/auth/me': [GUEST]});

        await loadSession();

        expect(getSession()).toStrictEqual({status: 'guest'});
        expect(paths(fetchMock)).toStrictEqual(['/api/auth/me']);
    });

    it('restores a browser session into memory through the refresh cookie', async () => {
        const fetchMock = respond({'/api/auth/me': [REQUIRED, SIGNED_IN], '/api/auth/refresh': [ACCESS]});

        await loadSession();

        expect(getSession()).toStrictEqual({status: 'signed-in', user: ZED});
        expect(paths(fetchMock)).toStrictEqual(['/api/auth/me', '/api/auth/refresh', '/api/auth/me']);
        expect(authorization(fetchMock, '/api/auth/me', 0)).toBeNull();
        expect(authorization(fetchMock, '/api/auth/me', 1)).toBe('Bearer access-jwt');
    });

    it('holds page reads until the restored access token is in memory', async () => {
        const first = later();
        const fetchMock = respond({
            '/api/auth/me': [first.answer, SIGNED_IN], '/api/auth/refresh': [ACCESS], '/api/platforms': [[200, {}]],
        });

        const loaded = loadSession();
        const reading = read('/api/platforms');
        await Promise.resolve();

        expect(paths(fetchMock)).toStrictEqual(['/api/auth/me']);

        first.give(REQUIRED);
        await loaded;
        await reading;

        expect(paths(fetchMock)).toStrictEqual(['/api/auth/me', '/api/auth/refresh', '/api/auth/me', '/api/platforms']);
        expect(authorization(fetchMock, '/api/platforms')).toBe('Bearer access-jwt');
    });

    it('lets page reads go once asking who reads them failed', async () => {
        const fetchMock = respond({'/api/auth/me': [[500]], '/api/platforms': [[200, {}]]});

        const loaded = loadSession().catch(() => undefined);
        await read('/api/platforms');
        await loaded;

        expect(paths(fetchMock)).toStrictEqual(['/api/auth/me', '/api/platforms']);
        expect(authorization(fetchMock, '/api/platforms')).toBeNull();
    });

    it('is a guest once the session in its refresh cookie is over', async () => {
        respond({'/api/auth/me': [REQUIRED], '/api/auth/refresh': [EXPIRED]});

        await loadSession();

        expect(getSession()).toStrictEqual({status: 'guest'});
    });

    it('asks who is signed in once, however many ask', async () => {
        const fetchMock = respond({'/api/auth/me': [GUEST]});

        await Promise.all([loadSession(), loadSession()]);
        await loadSession();

        expect(paths(fetchMock)).toStrictEqual(['/api/auth/me']);
    });

    it('starts a session with its returned access token', async () => {
        const fetchMock = respond({'/api/auth/me': [GUEST, SIGNED_IN], '/api/auth/login': [ACCESS]});
        await loadSession();

        await signIn();

        expect(getSession()).toStrictEqual({status: 'signed-in', user: ZED});
        expect(paths(fetchMock)).toStrictEqual(['/api/auth/me', '/api/auth/login', '/api/auth/me']);
        expect(authorization(fetchMock, '/api/auth/me', 1)).toBe('Bearer access-jwt');
    });

    it('starts after the initial session question without letting its earlier answer undo it', async () => {
        const first = later();
        const fetchMock = respond({'/api/auth/me': [first.answer, SIGNED_IN], '/api/auth/confirm': [ACCESS]});
        const loading = loadSession();

        const confirming = startSession((client) => client.POST('/api/auth/confirm', {body: {token: 'secret'}}));
        first.give(GUEST);
        await Promise.all([loading, confirming]);

        expect(getSession()).toStrictEqual({status: 'signed-in', user: ZED});
        expect(paths(fetchMock)).toStrictEqual(['/api/auth/me', '/api/auth/confirm', '/api/auth/me']);
        expect(authorization(fetchMock, '/api/auth/me', 1)).toBe('Bearer access-jwt');
    });

    it('keeps its current session when signing in is refused', async () => {
        respond({'/api/auth/me': [GUEST], '/api/auth/login': [[422, {status: 422, code: 'user.credentials-invalid'}]]});
        await loadSession();

        await expect(signIn()).rejects.toMatchObject({name: 'BusinessError', code: 'user.credentials-invalid'});
        expect(getSession()).toStrictEqual({status: 'guest'});
    });

    it('removes the access token from memory when signing out', async () => {
        const fetchMock = respond({
            '/api/auth/me': [GUEST, SIGNED_IN], '/api/auth/login': [ACCESS], '/api/auth/logout': [[204]], '/api/platforms': [[200, 'public']],
        });
        await loadSession();
        await signIn();

        await endSession();
        await read('/api/platforms');

        expect(getSession()).toStrictEqual({status: 'guest'});
        expect(authorization(fetchMock, '/api/platforms')).toBeNull();
    });

    it('refreshes an expired access token once and repeats the request with the replacement', async () => {
        const fetchMock = respond({
            '/api/auth/me': [GUEST, SIGNED_IN],
            '/api/auth/login': [ACCESS],
            '/api/platforms': [REQUIRED, [200, {ok: true}]],
            '/api/auth/refresh': [NEW_ACCESS],
        });
        await loadSession();
        await signIn();

        await expect(read('/api/platforms')).resolves.toStrictEqual({ok: true});

        expect(authorization(fetchMock, '/api/platforms', 0)).toBe('Bearer access-jwt');
        expect(authorization(fetchMock, '/api/platforms', 1)).toBe('Bearer new-access-jwt');
        expect(paths(fetchMock).filter((path) => path === '/api/auth/refresh')).toHaveLength(1);
    });

    it('shares one refresh between requests refused at the same time in one tab', async () => {
        const fetchMock = respond({
            '/api/auth/me': [GUEST, SIGNED_IN],
            '/api/auth/login': [ACCESS],
            '/api/platforms': [REQUIRED, [200, 'a']],
            '/api/categories': [REQUIRED, [200, 'b']],
            '/api/auth/refresh': [NEW_ACCESS],
        });
        await loadSession();
        await signIn();

        await expect(Promise.all([read('/api/platforms'), read('/api/categories')])).resolves.toStrictEqual(['a', 'b']);

        expect(paths(fetchMock).filter((path) => path === '/api/auth/refresh')).toHaveLength(1);
        expect(authorization(fetchMock, '/api/platforms', 1)).toBe('Bearer new-access-jwt');
        expect(authorization(fetchMock, '/api/categories', 1)).toBe('Bearer new-access-jwt');
    });

    it('becomes a guest when its access token cannot be refreshed', async () => {
        const fetchMock = respond({
            '/api/auth/me': [GUEST, SIGNED_IN], '/api/auth/login': [ACCESS], '/api/platforms': [REQUIRED], '/api/auth/refresh': [EXPIRED],
        });
        await loadSession();
        await signIn();

        await expect(read('/api/platforms')).rejects.toStrictEqual(new AuthenticationRequiredError('auth.required'));

        expect(getSession()).toStrictEqual({status: 'guest'});
        expect(authorization(fetchMock, '/api/platforms')).toBe('Bearer access-jwt');
    });

    it.each(Object.entries(SESSION_CALLS))(
        'never refreshes for %s, which makes or ends sessions itself',
        async (path, call) => {
            const fetchMock = respond({[path]: [EXPIRED]});

            const {error, response} = await call();

            expect(apiError(response, error)).toStrictEqual(new AuthenticationRequiredError('auth.session-expired'));
            expect(paths(fetchMock)).toStrictEqual([path]);
        },
    );

    it('tells other tabs the signed-in user and their in-memory access token', async () => {
        const tab = otherTab();
        channels.push(tab.channel);
        respond({'/api/auth/me': [GUEST, SIGNED_IN], '/api/auth/login': [ACCESS]});
        await loadSession();

        await signIn();

        await vi.waitFor(() => {
            expect(tab.heard.at(-1)).toStrictEqual({status: 'signed-in', user: ZED, accessToken: 'access-jwt'});
        });
    });

    it('uses the access token received from another tab', async () => {
        const tab = otherTab();
        channels.push(tab.channel);
        const fetchMock = respond({'/api/auth/me': [GUEST], '/api/platforms': [[200, 'x']]});
        await loadSession();

        tab.channel.postMessage({status: 'signed-in', user: ZED, accessToken: 'other-tab-access'});
        await vi.waitFor(() => {
            expect(getSession()).toStrictEqual({status: 'signed-in', user: ZED});
        });

        await read('/api/platforms');

        expect(authorization(fetchMock, '/api/platforms')).toBe('Bearer other-tab-access');
    });

    it('stays the same session, only taking the new access token, when another tab restores the same user', async () => {
        const tab = otherTab();
        channels.push(tab.channel);
        const fetchMock = respond({
            '/api/auth/me': [GUEST, SIGNED_IN], '/api/auth/login': [ACCESS], '/api/platforms': [[200, 'x']],
        });
        await loadSession();
        await signIn();
        const before = getSession();
        const changes = vi.fn();
        const unsubscribe = subscribeSession(changes);

        tab.channel.postMessage({status: 'signed-in', user: ZED, accessToken: 'other-tab-access'});
        // Nothing else shows that the message arrived: each read goes out with the token this tab holds then.
        await vi.waitFor(async () => {
            await read('/api/platforms');

            expect(fetchMock.mock.lastCall === undefined ? null : headersOf(fetchMock.mock.lastCall).get('Authorization')).toBe('Bearer other-tab-access');
        });
        unsubscribe();

        expect(getSession()).toBe(before);
        expect(changes).not.toHaveBeenCalled();
    });

    it('becomes the other user another tab signed in', async () => {
        const tab = otherTab();
        channels.push(tab.channel);
        respond({'/api/auth/me': [GUEST, SIGNED_IN], '/api/auth/login': [ACCESS]});
        await loadSession();
        await signIn();
        const changes = vi.fn();
        const unsubscribe = subscribeSession(changes);

        tab.channel.postMessage({status: 'signed-in', user: ADA, accessToken: 'ada-access'});
        await vi.waitFor(() => {
            expect(getSession()).toStrictEqual({status: 'signed-in', user: ADA});
        });
        unsubscribe();

        expect(changes).toHaveBeenCalledExactlyOnceWith();
    });

    it('removes its access token when another tab signs out', async () => {
        const tab = otherTab();
        channels.push(tab.channel);
        const fetchMock = respond({
            '/api/auth/me': [GUEST, SIGNED_IN], '/api/auth/login': [ACCESS], '/api/platforms': [[200, 'public']],
        });
        await loadSession();
        await signIn();

        tab.channel.postMessage({status: 'guest'});
        await vi.waitFor(() => {
            expect(getSession()).toStrictEqual({status: 'guest'});
        });

        await read('/api/platforms');

        expect(authorization(fetchMock, '/api/platforms')).toBeNull();
    });
});
