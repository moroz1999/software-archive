import {afterEach, describe, expect, it, vi} from 'vitest';
import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {type QueryClient} from '@tanstack/react-query';
import {createQueryClient} from '@/shared/api';
import {json, noContent, pageResponse, stubFetchRoutes} from '@/shared/lib/test';
import {App} from './App';
import {type AppRouter, createAppRouter} from './router';

const SOFTWARE_ID = '00000000-0000-0000-0001-000000000001';

describe('UI localization', () => {
    const routers: AppRouter[] = [];
    const queryClients: QueryClient[] = [];

    function renderApp() {
        const queryClient = createQueryClient();
        const router = createAppRouter(queryClient);
        queryClients.push(queryClient);
        routers.push(router);
        return render(<App router={router} queryClient={queryClient}/>);
    }

    function stubHome(status = 200) {
        return stubFetchRoutes(new Map([
            ['/api/platforms', json([])],
            ['/api/auth/me', noContent()],
            ['/api/software/latest?size=12', status === 200 ? json(pageResponse([])) : json({status}, status)],
        ]));
    }

    afterEach(() => {
        routers.forEach((router) => {
            router.dispose();
        });
        routers.length = 0;
        queryClients.forEach((queryClient) => {
            queryClient.clear();
        });
        queryClients.length = 0;
        localStorage.clear();
        document.documentElement.lang = 'en';
        window.history.replaceState(null, '', '/');
        vi.restoreAllMocks();
        vi.unstubAllGlobals();
    });

    it('uses a supported browser language for the UI', async () => {
        vi.spyOn(window.navigator, 'language', 'get').mockReturnValue('ru-RU');
        stubHome();

        renderApp();

        await expect(screen.findByRole('heading', {level: 1, name: 'Недавно добавленное'})).resolves.toBeInTheDocument();
        expect(screen.getByRole('navigation', {name: 'Главное меню'})).toBeInTheDocument();
        expect(document.documentElement).toHaveAttribute('lang', 'ru');
    });

    it('switches the UI language and remembers the choice', async () => {
        stubHome();
        renderApp();

        await screen.findByRole('heading', {level: 1, name: 'Recently added'});
        await userEvent.selectOptions(screen.getByRole('combobox', {name: 'Language'}), 'ru');

        await expect(screen.findByRole('heading', {level: 1, name: 'Недавно добавленное'})).resolves.toBeInTheDocument();
        expect(localStorage.getItem('archive.language')).toBe('ru');
        expect(document.documentElement).toHaveAttribute('lang', 'ru');
    });

    it('uses the remembered language on the next page load', async () => {
        localStorage.setItem('archive.language', 'ru');
        stubHome();

        renderApp();

        await expect(screen.findByRole('heading', {level: 1, name: 'Недавно добавленное'})).resolves.toBeInTheDocument();
        expect(screen.getByRole('combobox', {name: 'Язык'})).toHaveValue('ru');
    });

    it('loads the archive content in the UI language and again when the language is switched', async () => {
        const authorId = '00000000-0000-0000-0003-000000000001';
        window.history.replaceState(null, '', `/authors/${authorId}`);
        const author = (country: string, city: string) => json({
            id: authorId,
            name: 'Zed',
            realName: null,
            notes: null,
            labels: [],
            locations: [{countryId: '00000000-0000-0000-0002-000000000006', country, cityId: '00000000-0000-0000-0002-000000000601', city, primary: true}],
            groups: [],
            origins: [], added: {by: 'Zed', at: '2026-01-01T00:00:00Z'},
            canChange: false,
        });
        vi.stubGlobal('fetch', vi.fn<typeof fetch>((input, init) => {
            const url = typeof input === 'string' ? input : input instanceof URL ? input.href : input.url;
            const russian = new Headers(init?.headers).get('Accept-Language') === 'ru';
            const routes = new Map([
                ['/api/platforms', json([])],
                ['/api/auth/me', noContent()],
                [`/api/authors/${authorId}`, russian ? author('Финляндия', 'Хельсинки') : author('Finland', 'Helsinki')],
                [`/api/authors/${authorId}/software`, json([])],
            ]);
            const response = routes.get(url);
            return response === undefined ? Promise.reject(new TypeError(`Unexpected fetch: ${url}`)) : Promise.resolve(response());
        }));

        renderApp();

        await expect(screen.findByText('Helsinki, Finland')).resolves.toBeInTheDocument();

        await userEvent.selectOptions(screen.getByRole('combobox', {name: 'Language'}), 'ru');

        await expect(screen.findByText('Хельсинки, Финляндия')).resolves.toBeInTheDocument();
    });

    it('asks for the archive content in the remembered language from the first call', async () => {
        localStorage.setItem('archive.language', 'ru');
        const fetchMock = stubHome();

        renderApp();

        await screen.findByRole('heading', {level: 1, name: 'Недавно добавленное'});
        const asked = fetchMock.mock.calls.map(([, init]) => new Headers(init?.headers).get('Accept-Language'));

        expect(asked).not.toHaveLength(0);
        expect(new Set(asked)).toStrictEqual(new Set(['ru']));
    });

    it('localizes system errors', async () => {
        localStorage.setItem('archive.language', 'ru');
        stubHome(500);

        renderApp();

        await expect(screen.findByRole('alertdialog', {name: 'Что-то пошло не так'})).resolves.toBeInTheDocument();
        expect(screen.getByText('Сервер не смог выполнить запрос. Попробуйте ещё раз.')).toBeInTheDocument();
        expect(screen.getByRole('button', {name: 'Повторить'})).toBeInTheDocument();
    });

    it('labels the release date in the UI language and writes it as dd.mm.yyyy in any', async () => {
        localStorage.setItem('archive.language', 'ru');
        window.history.replaceState(null, '', `/software/${SOFTWARE_ID}`);
        stubFetchRoutes(new Map([
            ['/api/platforms', json([])],
            ['/api/auth/me', noContent()],
            [`/api/software/${SOFTWARE_ID}`, json({
                id: SOFTWARE_ID,
                title: 'Elite',
                description: null,
                releaseDate: '1985-03-02',
                platform: {code: 'vector', name: 'Vector'},
                hardware: [],
                categories: [],
                credits: [],
                sources: [], preservation: 'unknown',
                origins: [], added: {by: 'Zed', at: '2026-01-01T00:00:00Z'},
                canChange: true,
            })],
            [`/api/software/${SOFTWARE_ID}/screenshots`, json([])],
            [`/api/software/${SOFTWARE_ID}/releases`, json([])],
        ]));

        renderApp();

        await screen.findByRole('heading', {level: 1, name: 'Elite'});

        expect(screen.getAllByText('Дата выпуска').length).toBeGreaterThan(0);
        expect(screen.getByText('02.03.1985')).toBeInTheDocument();
    });
});
