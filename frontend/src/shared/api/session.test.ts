import {afterEach, describe, expect, it, vi} from 'vitest';
import {AuthenticationRequiredError} from './errors';
import {getJson, postNoContent} from './http';
import {endSession, getSession, loadSession, startSession} from './session';

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
        const path = typeof input === 'string' ? input : input instanceof URL ? input.href : input.url;
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

function paths(fetchMock: ReturnType<typeof respond>): string[] {
    return fetchMock.mock.calls.map(([input]) => (typeof input === 'string' ? input : input instanceof URL ? input.href : input.url));
}

const ZED = {id: '00000000-0000-0000-0009-000000000001', name: 'Zed'};
const GUEST: Answer = [204];
const SIGNED_IN: Answer = [200, ZED];
const REQUIRED: Answer = [401, {status: 401, code: 'auth.required'}];
const EXPIRED: Answer = [401, {status: 401, code: 'auth.session-expired'}];

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

    it('is the signed-in user', async () => {
        respond({'/api/auth/me': [SIGNED_IN]});

        await loadSession();

        expect(getSession()).toStrictEqual({status: 'signed-in', user: ZED});
    });

    it('is refreshed when the browser holds a session to refresh', async () => {
        const fetchMock = respond({'/api/auth/me': [REQUIRED, SIGNED_IN], '/api/auth/refresh': [[204]]});

        await loadSession();

        expect(getSession()).toStrictEqual({status: 'signed-in', user: ZED});
        expect(paths(fetchMock)).toStrictEqual(['/api/auth/me', '/api/auth/refresh', '/api/auth/me']);
    });

    it('holds what the page reads until it is known who reads it, with the session refreshed by then', async () => {
        const first = later();
        const fetchMock = respond({'/api/auth/me': [first.answer, SIGNED_IN], '/api/auth/refresh': [[204]], '/api/x': [[200, {}]]});

        const loaded = loadSession();
        const read = getJson('/api/x');
        await Promise.resolve();

        expect(paths(fetchMock)).toStrictEqual(['/api/auth/me']);

        first.give(REQUIRED);
        await loaded;
        await read;

        expect(paths(fetchMock)).toStrictEqual(['/api/auth/me', '/api/auth/refresh', '/api/auth/me', '/api/x']);
    });

    it('lets what the page reads go once the question of who reads it failed', async () => {
        const fetchMock = respond({'/api/auth/me': [[500]], '/api/x': [[200, {}]]});

        const loaded = loadSession().catch(() => undefined);
        await getJson('/api/x');
        await loaded;

        expect(paths(fetchMock)).toStrictEqual(['/api/auth/me', '/api/x']);
    });

    it('is a guest once the session it holds cannot be refreshed', async () => {
        respond({'/api/auth/me': [REQUIRED], '/api/auth/refresh': [EXPIRED]});

        await loadSession();

        expect(getSession()).toStrictEqual({status: 'guest'});
    });

    it('is asked once, however many ask', async () => {
        const fetchMock = respond({'/api/auth/me': [GUEST]});

        await Promise.all([loadSession(), loadSession()]);
        await loadSession();

        expect(paths(fetchMock)).toStrictEqual(['/api/auth/me']);
    });

    it('starts by signing in, telling who signed in only once it is done', async () => {
        const fetchMock = respond({'/api/auth/me': [GUEST, SIGNED_IN], '/api/auth/login': [[204]]});
        await loadSession();

        await startSession('/api/auth/login', {login: 'Zed'});

        expect(getSession()).toStrictEqual({status: 'signed-in', user: ZED});
        expect(paths(fetchMock)).toStrictEqual(['/api/auth/me', '/api/auth/login', '/api/auth/me']);
    });

    it('starts after the question of who is signed in still running, whose answer does not undo it', async () => {
        const first = later();
        const fetchMock = respond({'/api/auth/me': [first.answer, SIGNED_IN], '/api/auth/confirm': [[204]]});
        const loading = loadSession();

        const confirming = startSession('/api/auth/confirm', {token: 'secret'});
        first.give(GUEST);
        await Promise.all([loading, confirming]);

        expect(getSession()).toStrictEqual({status: 'signed-in', user: ZED});
        expect(paths(fetchMock)).toStrictEqual(['/api/auth/me', '/api/auth/confirm', '/api/auth/me']);
    });

    it('does not start when the call is refused, and keeps what it was', async () => {
        respond({'/api/auth/me': [GUEST], '/api/auth/login': [[422, {status: 422, code: 'user.credentials-invalid'}]]});
        await loadSession();

        await expect(startSession('/api/auth/login', {login: 'Zed'})).rejects.toMatchObject({name: 'BusinessError', code: 'user.credentials-invalid'});
        expect(getSession()).toStrictEqual({status: 'guest'});
    });

    it('ends by signing out', async () => {
        const fetchMock = respond({'/api/auth/me': [SIGNED_IN], '/api/auth/logout': [[204]]});
        await loadSession();

        await endSession();

        expect(getSession()).toStrictEqual({status: 'guest'});
        expect(paths(fetchMock)).toContain('/api/auth/logout');
    });

    it('is refreshed once a request needs it, and the request is repeated', async () => {
        const fetchMock = respond({'/api/x': [REQUIRED, [200, {ok: true}]], '/api/auth/refresh': [[204]]});

        await expect(getJson('/api/x')).resolves.toStrictEqual({ok: true});
        expect(paths(fetchMock)).toStrictEqual(['/api/x', '/api/auth/refresh', '/api/x']);
        expect(fetchMock.mock.calls[1]?.[1]?.method).toBe('POST');
    });

    it('is refreshed once for requests needing it at the same time', async () => {
        const fetchMock = respond({'/api/a': [REQUIRED, [200, 'a']], '/api/b': [REQUIRED, [200, 'b']], '/api/auth/refresh': [[204]]});

        await expect(Promise.all([getJson('/api/a'), getJson('/api/b')])).resolves.toStrictEqual(['a', 'b']);
        expect(paths(fetchMock).filter((path) => path === '/api/auth/refresh')).toHaveLength(1);
    });

    it('is refreshed for a request only after a sign-in running meanwhile, with the session it made', async () => {
        const login = later();
        const fetchMock = respond({
            '/api/auth/me': [GUEST, SIGNED_IN], '/api/auth/login': [login.answer], '/api/x': [REQUIRED, [200, 'x']], '/api/auth/refresh': [[204]],
        });
        await loadSession();
        const signingIn = startSession('/api/auth/login', {login: 'Zed'});
        await vi.waitFor(() => {
            expect(paths(fetchMock)).toContain('/api/auth/login');
        });

        const request = getJson('/api/x');
        await vi.waitFor(() => {
            expect(paths(fetchMock)).toContain('/api/x');
        });
        login.give([204]);

        await expect(request).resolves.toBe('x');

        await signingIn;

        expect(paths(fetchMock)).toStrictEqual(['/api/auth/me', '/api/auth/login', '/api/x', '/api/auth/me', '/api/auth/refresh', '/api/x']);
        expect(getSession()).toStrictEqual({status: 'signed-in', user: ZED});
    });

    it('becomes a guest when it cannot be refreshed: the request fails as needing a signed-in user', async () => {
        respond({'/api/auth/me': [SIGNED_IN], '/api/x': [REQUIRED], '/api/auth/refresh': [EXPIRED]});
        await loadSession();

        await expect(getJson('/api/x')).rejects.toStrictEqual(new AuthenticationRequiredError('auth.required'));
        expect(getSession()).toStrictEqual({status: 'guest'});
    });

    it.each(['/api/auth/login', '/api/auth/logout', '/api/auth/register', '/api/auth/confirm', '/api/auth/resend-confirmation'])(
        'is never refreshed for %s, which makes or ends sessions itself',
        async (path) => {
            const fetchMock = respond({[path]: [EXPIRED]});

            await expect(postNoContent(path, {})).rejects.toStrictEqual(new AuthenticationRequiredError('auth.session-expired'));
            expect(paths(fetchMock)).toStrictEqual([path]);
        },
    );

    it('tells the other tabs what it became', async () => {
        const tab = otherTab();
        channels.push(tab.channel);
        respond({'/api/auth/me': [GUEST, SIGNED_IN], '/api/auth/login': [[204]]});
        await loadSession();

        await startSession('/api/auth/login', {login: 'Zed'});

        await vi.waitFor(() => {
            expect(tab.heard.at(-1)).toStrictEqual({status: 'signed-in', user: ZED});
        });
    });

    it('becomes what another tab tells', async () => {
        const tab = otherTab();
        channels.push(tab.channel);
        respond({'/api/auth/me': [SIGNED_IN]});
        await loadSession();

        tab.channel.postMessage({status: 'guest'});

        await vi.waitFor(() => {
            expect(getSession()).toStrictEqual({status: 'guest'});
        });
    });
});
