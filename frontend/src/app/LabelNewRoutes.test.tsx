import {afterEach, describe, expect, it, vi} from 'vitest';
import {render, screen, within} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {createQueryClient} from '@/shared/api';
import {json, stubGlobalFetch} from '@/shared/lib/test';
import {App} from './App';
import {type AppRouter, createAppRouter} from './router';

const MEMBER = {id: '00000000-0000-0000-0009-000000000001', name: 'Zed', email: 'zed@example.com', groups: ['member'], abilities: ['add-entries']};

const ID = '00000000-0000-0000-0004-000000000001';
const FINLAND = '00000000-0000-0000-0010-000000000001';
const HELSINKI = '00000000-0000-0000-0011-000000000001';

const ZORK = {
    id: ID,
    name: 'Zork',
    owner: null,
    groups: [],
    locations: [],
    origins: [],
    added: {by: 'Zed', at: '2026-03-04T05:06:07Z'},
    canChange: false,
    canMakeOwner: true,
    canHandOver: false,
    canEdit: true,
    canChangeOwner: false,
};

type Answer = () => Response;
type Routes = Readonly<Record<string, Answer>>;

function urlOf(input: Parameters<typeof fetch>[0]): string {
    return typeof input === 'string' ? input : input instanceof URL ? input.href : input.url;
}

/** Answers every fetch by its method and URL; anything else fails like a network failure, showing up as a system error. */
function stubApi(routes: Routes) {
    const answers = new Map<string, Answer>([
        ['GET /api/auth/me', json(MEMBER)],
        ['GET /api/platforms', json([])],
        ['GET /api/categories', json([])],
        ['GET /api/countries', json([{id: FINLAND, name: 'Finland'}])],
        [`GET /api/countries/${FINLAND}/cities`, json([{id: HELSINKI, name: 'Helsinki'}])],
        [`GET /api/labels/${ID}`, json(ZORK)],
        [`GET /api/labels/${ID}/software`, json([])],
        ...Object.entries(routes),
    ]);
    const fetchMock = vi.fn<typeof fetch>((input, init) => {
        const url = urlOf(input);
        const answer = answers.get(`${init?.method ?? 'GET'} ${url}`);
        return answer === undefined ? Promise.reject(new TypeError(`Unexpected fetch: ${url}`)) : Promise.resolve(answer());
    });
    stubGlobalFetch(fetchMock);
    return fetchMock;
}

function postedBody(fetchMock: ReturnType<typeof stubApi>): unknown {
    const call = fetchMock.mock.calls.find(([url, init]) => url === '/api/labels' && init?.method === 'POST');
    const body = call?.[1]?.body;
    return typeof body === 'string' ? JSON.parse(body) : undefined;
}

describe('label adding route', () => {
    let router: AppRouter | undefined;

    afterEach(() => {
        router?.dispose();
        router = undefined;
        window.history.replaceState(null, '', '/');
        vi.unstubAllGlobals();
    });

    async function openForm(routes: Routes = {}) {
        const fetchMock = stubApi(routes);
        window.history.replaceState(null, '', '/labels/new');
        const queryClient = createQueryClient();
        router = createAppRouter(queryClient);
        render(<App router={router} queryClient={queryClient}/>);
        await screen.findByRole('heading', {level: 1, name: 'Add label'});
        return fetchMock;
    }

    it('opens an empty form with no say of whose the label is', async () => {
        await openForm();

        expect(screen.getByRole('textbox', {name: 'Name'})).toHaveValue('');
        expect(screen.queryByRole('group', {name: 'Whose label'})).not.toBeInTheDocument();
        expect(screen.queryByRole('region', {name: 'Sources'})).not.toBeInTheDocument();
    });

    it('adds a label of nobody with its locations and opens its page', async () => {
        const fetchMock = await openForm({'POST /api/labels': json({id: ID}, 201)});

        await userEvent.type(screen.getByRole('textbox', {name: 'Name'}), 'Zork');
        await userEvent.click(screen.getByRole('button', {name: 'Add location'}));
        await userEvent.selectOptions(screen.getByRole('combobox', {name: 'Country 1'}), FINLAND);
        await userEvent.selectOptions(await screen.findByRole('combobox', {name: 'City 1'}), HELSINKI);
        await userEvent.click(screen.getByRole('button', {name: 'Add label'}));

        await expect(screen.findByRole('heading', {level: 1, name: 'Zork'})).resolves.toBeInTheDocument();
        expect(window.location.pathname).toBe(`/labels/${ID}`);
        expect(postedBody(fetchMock)).toStrictEqual({
            name: 'Zork',
            locations: [{country: FINLAND, city: HELSINKI, primary: true}],
        });
    });

    it('returns to the author catalog on Cancel', async () => {
        await openForm({'GET /api/authors?page=0': json({content: [], page: {size: 100, number: 0, totalElements: 0, totalPages: 0}})});

        await userEvent.click(screen.getByRole('link', {name: 'Cancel'}));

        await vi.waitFor(() => {
            expect(window.location.pathname).toBe('/authors');
        });
    });

    it('checks the name and the country of each location before sending', async () => {
        const fetchMock = await openForm();

        await userEvent.click(screen.getByRole('button', {name: 'Add location'}));
        await userEvent.click(screen.getByRole('button', {name: 'Add label'}));

        expect(screen.getByText('Enter a name.')).toBeInTheDocument();
        expect(screen.getByText('Choose a country.')).toBeInTheDocument();
        expect(postedBody(fetchMock)).toBeUndefined();
    });

    it('shows a refusal about the locations under them', async () => {
        await openForm({
            'POST /api/labels': json({status: 422, code: 'location.unknown', errors: [{field: 'locations', code: 'location.unknown'}]}, 422),
        });

        await userEvent.type(screen.getByRole('textbox', {name: 'Name'}), 'Zork');
        await userEvent.click(screen.getByRole('button', {name: 'Add location'}));
        await userEvent.selectOptions(screen.getByRole('combobox', {name: 'Country 1'}), FINLAND);
        await userEvent.click(screen.getByRole('button', {name: 'Add label'}));
        const locations = screen.getByRole('region', {name: 'Geography'});

        await expect(within(locations).findByText(/is not in the archive/u)).resolves.toBeInTheDocument();
    });

    it('hands a failure of saving to the error dialog, keeping what was typed', async () => {
        await openForm({'POST /api/labels': json({status: 500}, 500)});

        await userEvent.type(screen.getByRole('textbox', {name: 'Name'}), 'Zork');
        await userEvent.click(screen.getByRole('button', {name: 'Add label'}));

        await expect(screen.findByRole('alertdialog', {name: 'Something went wrong'})).resolves.toBeInTheDocument();
        expect(screen.getByRole('textbox', {name: 'Name'})).toHaveValue('Zork');
    });
});
