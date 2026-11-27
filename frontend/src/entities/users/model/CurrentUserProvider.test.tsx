import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import {useQuery} from '@tanstack/react-query';
import {api, endSession, startSession} from '@/shared/api';
import {json, noContent, renderWithQueries, stubFetchRoutes} from '@/shared/lib/test';
import {CurrentUserProvider} from './CurrentUserProvider';
import {useAbility, useCurrentUser} from './current-user-context';

const ZED = {id: '00000000-0000-0000-0009-000000000001', name: 'Zed', email: 'zed@example.com', groups: ['member'], abilities: ['add-entries']};
const REQUIRED = json({status: 401, code: 'auth.required'}, 401);
const EXPIRED = json({status: 401, code: 'auth.session-expired'}, 401);

function Who() {
    const {user, known} = useCurrentUser();
    if (!known) {
        return <p>Unknown yet</p>;
    }
    return <p>{user === null ? 'Visitor' : user.name}</p>;
}

function Abilities() {
    const addsEntries = useAbility('add-entries');
    const keepsCategories = useAbility('keep-categories');
    return <p>{`adds: ${String(addsEntries)}, keeps categories: ${String(keepsCategories)}`}</p>;
}

/** Shows what the page read, as everything a page shows depends on who reads it. */
function Shown() {
    const query = useQuery({queryKey: ['platforms'], queryFn: async ({signal}) => (await api.GET('/api/platforms', {signal})).data ?? null});
    return <p>{query.isSuccess ? 'Shown' : 'Loading'}</p>;
}

function calls(fetchMock: ReturnType<typeof stubFetchRoutes>, url: string): number {
    return fetchMock.mock.calls.filter(([input]) => input === url).length;
}

describe(CurrentUserProvider, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('tells the signed-in user', async () => {
        stubFetchRoutes(new Map([['/api/auth/me', json(ZED)]]));
        renderWithQueries(<CurrentUserProvider><Who/></CurrentUserProvider>);

        expect(screen.getByText('Unknown yet')).toBeInTheDocument();
        await expect(screen.findByText('Zed')).resolves.toBeInTheDocument();
    });

    it('tells a public visitor', async () => {
        stubFetchRoutes(new Map([['/api/auth/me', noContent()]]));
        renderWithQueries(<CurrentUserProvider><Who/></CurrentUserProvider>);

        await expect(screen.findByText('Visitor')).resolves.toBeInTheDocument();
    });

    it('turns the user into a public visitor once the session ends', async () => {
        const routes = new Map([['/api/auth/me', json(ZED)], ['/api/platforms', REQUIRED], ['/api/auth/refresh', EXPIRED]]);
        stubFetchRoutes(routes);
        renderWithQueries(<CurrentUserProvider><Who/></CurrentUserProvider>);
        await screen.findByText('Zed');

        await api.GET('/api/platforms');

        await expect(screen.findByText('Visitor')).resolves.toBeInTheDocument();
    });

    it('tells what the signed-in user may do', async () => {
        stubFetchRoutes(new Map([['/api/auth/me', json(ZED)]]));
        renderWithQueries(<CurrentUserProvider><Abilities/></CurrentUserProvider>);

        await expect(screen.findByText('adds: true, keeps categories: false')).resolves.toBeInTheDocument();
    });

    it('tells that a public visitor may do nothing', async () => {
        stubFetchRoutes(new Map([['/api/auth/me', noContent()]]));
        renderWithQueries(<CurrentUserProvider><Who/><Abilities/></CurrentUserProvider>);

        await screen.findByText('Visitor');

        expect(screen.getByText('adds: false, keeps categories: false')).toBeInTheDocument();
    });

    it('reads again what the page shows once someone signs in', async () => {
        const routes = new Map([['/api/auth/me', noContent()], ['/api/platforms', json([])], ['/api/auth/login', json({accessToken: 'access-jwt'})]]);
        const fetchMock = stubFetchRoutes(routes);
        renderWithQueries(<CurrentUserProvider><Who/><Shown/></CurrentUserProvider>);
        await screen.findByText('Visitor');
        await screen.findByText('Shown');

        expect(calls(fetchMock, '/api/platforms')).toBe(1);

        routes.set('/api/auth/me', json(ZED));
        await startSession((client) => client.POST('/api/auth/login', {body: {login: 'Zed', password: 'secret', remember: false}}));
        await screen.findByText('Zed');

        await vi.waitFor(() => {
            expect(calls(fetchMock, '/api/platforms')).toBe(2);
        });
    });

    it('reads again what the page shows once the user signs out', async () => {
        const fetchMock = stubFetchRoutes(new Map([['/api/auth/me', json(ZED)], ['/api/platforms', json([])], ['/api/auth/logout', noContent()]]));
        renderWithQueries(<CurrentUserProvider><Who/><Shown/></CurrentUserProvider>);
        await screen.findByText('Zed');
        await screen.findByText('Shown');
        await endSession();
        await screen.findByText('Visitor');

        await vi.waitFor(() => {
            expect(calls(fetchMock, '/api/platforms')).toBe(2);
        });
    });

    it('reads what the page shows once, after it is known who signed in', async () => {
        const fetchMock = stubFetchRoutes(new Map([['/api/auth/me', json(ZED)], ['/api/platforms', json([])]]));
        renderWithQueries(<CurrentUserProvider><Who/><Shown/></CurrentUserProvider>);
        await screen.findByText('Zed');
        await screen.findByText('Shown');

        expect(fetchMock.mock.calls.map(([input]) => input)).toStrictEqual(['/api/auth/me', '/api/platforms']);
    });
});
