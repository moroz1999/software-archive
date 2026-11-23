import {afterEach, describe, expect, it, vi} from 'vitest';
import {AuthenticationRequiredError} from './errors';
import {getJson, postNoContent} from './http';
import {endSession, getSession, loadSession, startSession, subscribeSession} from './session';

type Answer = readonly [status: number, body?: unknown];

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
    return typeof input === 'string' ? input : input instanceof URL ? input.href : input.url;
}

function paths(fetchMock: ReturnType<typeof respond>): string[] {
    return fetchMock.mock.calls.map(([input]) => pathOf(input));
}

function authorization(fetchMock: ReturnType<typeof respond>, path: string, occurrence = 0): string | null {
    const call = fetchMock.mock.calls.filter(([input]) => pathOf(input) === path)[occurrence];
    return new Headers(call?.[1]?.headers).get('Authorization');
}

const ZED = {id: '00000000-0000-0000-0009-000000000001', name: 'Zed'};
const ADA = {id: '00000000-0000-0000-0009-000000000002', name: 'Ada'};
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
            '/api/auth/me': [first.answer, SIGNED_IN], '/api/auth/refresh': [ACCESS], '/api/x': [[200, {}]],
        });

        const loaded = loadSession();
        const read = getJson('/api/x');
        await Promise.resolve();

        expect(paths(fetchMock)).toStrictEqual(['/api/auth/me']);

        first.give(REQUIRED);
        await loaded;
        await read;

        expect(paths(fetchMock)).toStrictEqual(['/api/auth/me', '/api/auth/refresh', '/api/auth/me', '/api/x']);
        expect(authorization(fetchMock, '/api/x')).toBe('Bearer access-jwt');
    });

    it('lets page reads go once asking who reads them failed', async () => {
        const fetchMock = respond({'/api/auth/me': [[500]], '/api/x': [[200, {}]]});

        const loaded = loadSession().catch(() => undefined);
        await getJson('/api/x');
        await loaded;

        expect(paths(fetchMock)).toStrictEqual(['/api/auth/me', '/api/x']);
        expect(authorization(fetchMock, '/api/x')).toBeNull();
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

        await startSession('/api/auth/login', {login: 'Zed'});

        expect(getSession()).toStrictEqual({status: 'signed-in', user: ZED});
        expect(paths(fetchMock)).toStrictEqual(['/api/auth/me', '/api/auth/login', '/api/auth/me']);
        expect(authorization(fetchMock, '/api/auth/me', 1)).toBe('Bearer access-jwt');
    });

    it('starts after the initial session question without letting its earlier answer undo it', async () => {
        const first = later();
        const fetchMock = respond({'/api/auth/me': [first.answer, SIGNED_IN], '/api/auth/confirm': [ACCESS]});
        const loading = loadSession();

        const confirming = startSession('/api/auth/confirm', {token: 'secret'});
        first.give(GUEST);
        await Promise.all([loading, confirming]);

        expect(getSession()).toStrictEqual({status: 'signed-in', user: ZED});
        expect(paths(fetchMock)).toStrictEqual(['/api/auth/me', '/api/auth/confirm', '/api/auth/me']);
        expect(authorization(fetchMock, '/api/auth/me', 1)).toBe('Bearer access-jwt');
    });

    it('keeps its current session when signing in is refused', async () => {
        respond({'/api/auth/me': [GUEST], '/api/auth/login': [[422, {status: 422, code: 'user.credentials-invalid'}]]});
        await loadSession();

        await expect(startSession('/api/auth/login', {login: 'Zed'})).rejects.toMatchObject({name: 'BusinessError', code: 'user.credentials-invalid'});
        expect(getSession()).toStrictEqual({status: 'guest'});
    });

    it('removes the access token from memory when signing out', async () => {
        const fetchMock = respond({
            '/api/auth/me': [GUEST, SIGNED_IN], '/api/auth/login': [ACCESS], '/api/auth/logout': [[204]], '/api/x': [[200, 'public']],
        });
        await loadSession();
        await startSession('/api/auth/login', {login: 'Zed'});

        await endSession();
        await getJson('/api/x');

        expect(getSession()).toStrictEqual({status: 'guest'});
        expect(authorization(fetchMock, '/api/x')).toBeNull();
    });

    it('refreshes an expired access token once and repeats the request with the replacement', async () => {
        const fetchMock = respond({
            '/api/auth/me': [GUEST, SIGNED_IN],
            '/api/auth/login': [ACCESS],
            '/api/x': [REQUIRED, [200, {ok: true}]],
            '/api/auth/refresh': [NEW_ACCESS],
        });
        await loadSession();
        await startSession('/api/auth/login', {login: 'Zed'});

        await expect(getJson('/api/x')).resolves.toStrictEqual({ok: true});

        expect(authorization(fetchMock, '/api/x', 0)).toBe('Bearer access-jwt');
        expect(authorization(fetchMock, '/api/x', 1)).toBe('Bearer new-access-jwt');
        expect(paths(fetchMock).filter((path) => path === '/api/auth/refresh')).toHaveLength(1);
    });

    it('shares one refresh between requests refused at the same time in one tab', async () => {
        const fetchMock = respond({
            '/api/auth/me': [GUEST, SIGNED_IN],
            '/api/auth/login': [ACCESS],
            '/api/a': [REQUIRED, [200, 'a']],
            '/api/b': [REQUIRED, [200, 'b']],
            '/api/auth/refresh': [NEW_ACCESS],
        });
        await loadSession();
        await startSession('/api/auth/login', {login: 'Zed'});

        await expect(Promise.all([getJson('/api/a'), getJson('/api/b')])).resolves.toStrictEqual(['a', 'b']);

        expect(paths(fetchMock).filter((path) => path === '/api/auth/refresh')).toHaveLength(1);
        expect(authorization(fetchMock, '/api/a', 1)).toBe('Bearer new-access-jwt');
        expect(authorization(fetchMock, '/api/b', 1)).toBe('Bearer new-access-jwt');
    });

    it('becomes a guest when its access token cannot be refreshed', async () => {
        const fetchMock = respond({
            '/api/auth/me': [GUEST, SIGNED_IN], '/api/auth/login': [ACCESS], '/api/x': [REQUIRED], '/api/auth/refresh': [EXPIRED],
        });
        await loadSession();
        await startSession('/api/auth/login', {login: 'Zed'});

        await expect(getJson('/api/x')).rejects.toStrictEqual(new AuthenticationRequiredError('auth.required'));

        expect(getSession()).toStrictEqual({status: 'guest'});
        expect(authorization(fetchMock, '/api/x')).toBe('Bearer access-jwt');
    });

    it.each(['/api/auth/login', '/api/auth/logout', '/api/auth/register', '/api/auth/confirm', '/api/auth/resend-confirmation'])(
        'never refreshes for %s, which makes or ends sessions itself',
        async (path) => {
            const fetchMock = respond({[path]: [EXPIRED]});

            await expect(postNoContent(path, {})).rejects.toStrictEqual(new AuthenticationRequiredError('auth.session-expired'));
            expect(paths(fetchMock)).toStrictEqual([path]);
        },
    );

    it('tells other tabs the signed-in user and their in-memory access token', async () => {
        const tab = otherTab();
        channels.push(tab.channel);
        respond({'/api/auth/me': [GUEST, SIGNED_IN], '/api/auth/login': [ACCESS]});
        await loadSession();

        await startSession('/api/auth/login', {login: 'Zed'});

        await vi.waitFor(() => {
            expect(tab.heard.at(-1)).toStrictEqual({status: 'signed-in', user: ZED, accessToken: 'access-jwt'});
        });
    });

    it('uses the access token received from another tab', async () => {
        const tab = otherTab();
        channels.push(tab.channel);
        const fetchMock = respond({'/api/auth/me': [GUEST], '/api/x': [[200, 'x']]});
        await loadSession();

        tab.channel.postMessage({status: 'signed-in', user: ZED, accessToken: 'other-tab-access'});
        await vi.waitFor(() => {
            expect(getSession()).toStrictEqual({status: 'signed-in', user: ZED});
        });

        await getJson('/api/x');

        expect(authorization(fetchMock, '/api/x')).toBe('Bearer other-tab-access');
    });

    it('stays the same session, only taking the new access token, when another tab restores the same user', async () => {
        const tab = otherTab();
        channels.push(tab.channel);
        const fetchMock = respond({
            '/api/auth/me': [GUEST, SIGNED_IN], '/api/auth/login': [ACCESS], '/api/x': [[200, 'x']],
        });
        await loadSession();
        await startSession('/api/auth/login', {login: 'Zed'});
        const before = getSession();
        const changes = vi.fn();
        const unsubscribe = subscribeSession(changes);

        tab.channel.postMessage({status: 'signed-in', user: ZED, accessToken: 'other-tab-access'});
        // Nothing else shows that the message arrived: each read goes out with the token this tab holds then.
        await vi.waitFor(async () => {
            await getJson('/api/x');

            expect(new Headers(fetchMock.mock.lastCall?.[1]?.headers).get('Authorization')).toBe('Bearer other-tab-access');
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
        await startSession('/api/auth/login', {login: 'Zed'});
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
            '/api/auth/me': [GUEST, SIGNED_IN], '/api/auth/login': [ACCESS], '/api/x': [[200, 'public']],
        });
        await loadSession();
        await startSession('/api/auth/login', {login: 'Zed'});

        tab.channel.postMessage({status: 'guest'});
        await vi.waitFor(() => {
            expect(getSession()).toStrictEqual({status: 'guest'});
        });

        await getJson('/api/x');

        expect(authorization(fetchMock, '/api/x')).toBeNull();
    });
});
