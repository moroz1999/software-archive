import {afterEach, describe, expect, it, vi} from 'vitest';
import {render, screen, within} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {createQueryClient} from '@/shared/api';
import {breadcrumbSteps, json, noContent} from '@/shared/lib/test';
import {App} from './App';
import {type AppRouter, createAppRouter} from './router';

const MEMBER = {id: '00000000-0000-0000-0009-000000000001', name: 'Zed', email: 'zed@example.com', groups: ['member'], abilities: ['add-entries']};
const ADMIN = {...MEMBER, groups: ['admin', 'member'], abilities: ['add-entries', 'keep-categories']};

const ID = '00000000-0000-0000-0004-000000000001';
const JANE = '00000000-0000-0000-0003-000000000001';
const BOB = '00000000-0000-0000-0003-000000000002';
const FINLAND = '00000000-0000-0000-0010-000000000001';
const LABEL_URL = `/api/labels/${ID}`;

const GHOST = {
    id: ID,
    name: 'Ghost',
    owner: {type: 'author', id: JANE, name: 'Jane Doe', note: 'JD'},
    groups: [],
    locations: [{countryId: FINLAND, country: 'Finland', cityId: null, city: null, primary: true}],
    origins: [
        {origin: 'vektor-06c.ru', externalId: 'Ghost', url: null},
        {origin: 'zx.example', externalId: '42', url: 'https://zx.example/42'},
    ],
    added: {by: 'Zed', at: '2026-01-01T00:00:00Z'},
    canChange: false,
    canMakeOwner: true,
    canHandOver: false,
    canEdit: true,
    canChangeOwner: true,
};

type Answer = () => Response;
type Routes = Readonly<Record<string, Answer>>;

function urlOf(input: Parameters<typeof fetch>[0]): string {
    return typeof input === 'string' ? input : input instanceof URL ? input.href : input.url;
}

/** Answers every fetch by its method and URL; anything else fails like a network failure, showing up as a system error. */
function stubApi(user: typeof MEMBER, routes: Routes) {
    const answers = new Map<string, Answer>([
        ['GET /api/auth/me', json(user)],
        ['GET /api/platforms', json([])],
        ['GET /api/categories', json([])],
        ['GET /api/countries', json([{id: FINLAND, name: 'Finland'}])],
        [`GET /api/countries/${FINLAND}/cities`, json([])],
        [`GET ${LABEL_URL}/software`, json([])],
        ...Object.entries(routes),
    ]);
    const fetchMock = vi.fn<typeof fetch>((input, init) => {
        const url = urlOf(input);
        const answer = answers.get(`${init?.method ?? 'GET'} ${url}`);
        return answer === undefined ? Promise.reject(new TypeError(`Unexpected fetch: ${url}`)) : Promise.resolve(answer());
    });
    vi.stubGlobal('fetch', fetchMock);
    return fetchMock;
}

function bodyOf(fetchMock: ReturnType<typeof stubApi>, method: string): unknown {
    const call = fetchMock.mock.calls.find(([url, init]) => url === LABEL_URL && init?.method === method);
    const body = call?.[1]?.body;
    return typeof body === 'string' ? JSON.parse(body) : undefined;
}

describe('label editing routes', () => {
    let router: AppRouter | undefined;

    afterEach(() => {
        router?.dispose();
        router = undefined;
        window.history.replaceState(null, '', '/');
        vi.unstubAllGlobals();
    });

    function openAt(path: string) {
        window.history.replaceState(null, '', path);
        const queryClient = createQueryClient();
        router = createAppRouter(queryClient);
        render(<App router={router} queryClient={queryClient}/>);
    }

    async function openForm(user: typeof MEMBER, label: object, routes: Routes = {}) {
        const fetchMock = stubApi(user, {[`GET ${LABEL_URL}`]: json(label), ...routes});
        openAt(`/labels/${ID}/edit`);
        await screen.findByRole('heading', {level: 1, name: 'Edit label'});
        return fetchMock;
    }

    it('leads from the label page to its editing for whoever may edit it', async () => {
        stubApi(MEMBER, {[`GET ${LABEL_URL}`]: json(GHOST)});
        openAt(`/labels/${ID}`);

        const menu = await screen.findByRole('button', {name: 'Actions'});

        expect(screen.queryByRole('link', {name: 'Edit'})).not.toBeInTheDocument();

        await userEvent.click(menu);

        expect(screen.getByRole('link', {name: 'Edit'})).toHaveAttribute('href', `/labels/${ID}/edit`);
    });

    it('leads back from the editing to the label and the authors', async () => {
        await openForm(MEMBER, GHOST);

        expect(breadcrumbSteps()).toStrictEqual([['Authors', '/authors'], ['Ghost', `/labels/${ID}`], ['Editing', null]]);
    });

    it('offers no editing to whoever may not edit the label', async () => {
        stubApi(MEMBER, {[`GET ${LABEL_URL}`]: json({...GHOST, canEdit: false})});
        openAt(`/labels/${ID}`);

        await userEvent.click(await screen.findByRole('button', {name: 'Actions'}));

        expect(screen.getByRole('button', {name: 'Make author'})).toBeInTheDocument();
        expect(screen.queryByRole('link', {name: 'Edit'})).not.toBeInTheDocument();
    });

    it('opens the form with the label as it is', async () => {
        await openForm(ADMIN, GHOST);

        expect(screen.getByRole('textbox', {name: 'Name'})).toHaveValue('Ghost');
        expect(screen.getByRole('button', {name: 'Remove Jane Doe'})).toBeInTheDocument();
        expect(screen.getByRole('combobox', {name: 'Country 1'})).toHaveValue(FINLAND);
    });

    it('shows the form skeleton first when the label comes later than the preload window', async () => {
        let answer: (response: Response) => void = () => undefined;
        const later = new Promise<Response>((resolve) => {
            answer = resolve;
        });
        const fetchMock = stubApi(ADMIN, {});
        const routed = fetchMock.getMockImplementation();
        fetchMock.mockImplementation((input, init) => (urlOf(input) === LABEL_URL ? later : routed?.(input, init) ?? Promise.reject(new TypeError('No route'))));
        openAt(`/labels/${ID}/edit`);

        await expect(screen.findByTestId('label-editor-skeleton')).resolves.toBeInTheDocument();

        answer(json(GHOST)());

        await expect(screen.findByRole('textbox', {name: 'Name'})).resolves.toHaveValue('Ghost');
        expect(screen.queryByTestId('label-editor-skeleton')).not.toBeInTheDocument();
    });

    it('shows where an import took the label from, leading to the source page when there is one', async () => {
        await openForm(ADMIN, GHOST);

        const sources = screen.getByRole('region', {name: 'Sources'});

        expect(within(sources).getByText('vektor-06c.ru')).toBeInTheDocument();
        expect(within(sources).getByText('Ghost')).toBeInTheDocument();
        expect(within(sources).getByRole('link', {name: '42'})).toHaveAttribute('href', 'https://zx.example/42');
    });

    it('says a label that was not imported has no sources', async () => {
        await openForm(ADMIN, {...GHOST, origins: []});

        expect(within(screen.getByRole('region', {name: 'Sources'})).getByText('None')).toBeInTheDocument();
    });

    it('saves the name, the owner and the locations and returns to the label', async () => {
        const fetchMock = await openForm(ADMIN, GHOST, {[`PUT ${LABEL_URL}`]: noContent()});

        await userEvent.clear(screen.getByRole('textbox', {name: 'Name'}));
        await userEvent.type(screen.getByRole('textbox', {name: 'Name'}), 'Spook');
        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        await expect(screen.findByRole('heading', {level: 1, name: 'Ghost'})).resolves.toBeInTheDocument();
        expect(window.location.pathname).toBe(`/labels/${ID}`);
        expect(bodyOf(fetchMock, 'PUT')).toStrictEqual({
            name: 'Spook',
            owner: {type: 'author', id: JANE, display: false},
            locations: [{country: FINLAND, city: null, primary: true}],
        });
    });

    it('keeps a location in a city of no known country as it is on saving', async () => {
        const kovrov = '00000000-0000-0000-0002-000000000013';
        const ghost = {...GHOST, locations: [{countryId: null, country: null, cityId: kovrov, city: 'Ковров', primary: true}]};
        const fetchMock = await openForm(ADMIN, ghost, {[`PUT ${LABEL_URL}`]: noContent()});

        expect(screen.getByRole('combobox', {name: 'City 1'})).toHaveDisplayValue('Ковров');

        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        await screen.findByRole('heading', {level: 1, name: 'Ghost'});

        expect(bodyOf(fetchMock, 'PUT')).toMatchObject({locations: [{country: null, city: kovrov, primary: true}]});
    });

    it('leaves the label to nobody once the owner is taken away', async () => {
        const fetchMock = await openForm(ADMIN, GHOST, {[`PUT ${LABEL_URL}`]: noContent()});

        await userEvent.click(screen.getByRole('button', {name: 'Remove Jane Doe'}));
        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        await vi.waitFor(() => {
            expect(bodyOf(fetchMock, 'PUT')).toMatchObject({owner: null});
        });
    });

    it('offers the display name once another person is chosen', async () => {
        const fetchMock = await openForm(ADMIN, GHOST, {
            [`PUT ${LABEL_URL}`]: noContent(),
            'GET /api/entity-lookup?types=author%2Cgroup&query=bob': json({items: [{id: BOB, type: 'author', title: 'Bob'}]}),
        });

        expect(screen.queryByRole('checkbox', {name: 'Display name'})).not.toBeInTheDocument();

        await userEvent.click(screen.getByRole('button', {name: 'Remove Jane Doe'}));
        await userEvent.type(screen.getByRole('combobox', {name: 'Person or group'}), 'bob');
        await userEvent.click(await screen.findByRole('option', {name: 'Bob'}));
        await userEvent.click(screen.getByRole('checkbox', {name: 'Display name'}));
        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        await vi.waitFor(() => {
            expect(bodyOf(fetchMock, 'PUT')).toMatchObject({owner: {type: 'author', id: BOB, display: true}});
        });
    });

    it('shows whose the label is without changing it to whoever is not an administrator', async () => {
        const fetchMock = await openForm(MEMBER, {...GHOST, canChangeOwner: false}, {[`PUT ${LABEL_URL}`]: noContent()});

        expect(screen.queryByRole('combobox', {name: 'Person or group'})).not.toBeInTheDocument();
        expect(screen.getByText('Jane Doe')).toBeInTheDocument();

        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        await vi.waitFor(() => {
            expect(bodyOf(fetchMock, 'PUT')).toMatchObject({owner: {type: 'author', id: JANE, display: false}});
        });
    });

    it('says a label of nobody is not known yet whose to whoever is not an administrator', async () => {
        await openForm(MEMBER, {...GHOST, owner: null, canChangeOwner: false});

        expect(screen.getByText('Not known yet whose label this is')).toBeInTheDocument();
    });

    it('checks the name before sending', async () => {
        const fetchMock = await openForm(ADMIN, GHOST);

        await userEvent.clear(screen.getByRole('textbox', {name: 'Name'}));
        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        expect(screen.getByText('Enter a name.')).toBeInTheDocument();
        expect(bodyOf(fetchMock, 'PUT')).toBeUndefined();
    });

    it('shows a refusal about the owner under whose the label is', async () => {
        await openForm(ADMIN, GHOST, {
            [`PUT ${LABEL_URL}`]: json({status: 422, code: 'label.display-one', errors: [{field: 'owner', code: 'label.display-one'}]}, 422),
        });

        await userEvent.click(screen.getByRole('button', {name: 'Remove Jane Doe'}));
        await userEvent.click(screen.getByRole('button', {name: 'Save'}));
        const owner = screen.getByRole('group', {name: 'Whose label'});

        await expect(within(owner).findByText('This label is the display name of its owner: choose another display name for them first.')).resolves.toBeInTheDocument();
    });

    it('shows a refusal of the whole form at its top', async () => {
        await openForm(MEMBER, {...GHOST, canChangeOwner: false}, {[`PUT ${LABEL_URL}`]: json({status: 403, code: 'access.denied'}, 403)});

        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        await expect(screen.findByText('Only whoever added this, or an administrator, may change it.')).resolves.toBeInTheDocument();
    });

    it('hands a failure of saving to the error dialog, keeping what was typed', async () => {
        await openForm(ADMIN, GHOST, {[`PUT ${LABEL_URL}`]: json({status: 500}, 500)});

        await userEvent.type(screen.getByRole('textbox', {name: 'Name'}), '!');
        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        await expect(screen.findByRole('alertdialog', {name: 'Something went wrong'})).resolves.toBeInTheDocument();
        expect(screen.getByRole('textbox', {name: 'Name'})).toHaveValue('Ghost!');
    });

    it('deletes the label after confirmation, saying its credits go with it, and opens the author catalog', async () => {
        const fetchMock = await openForm(ADMIN, GHOST, {[`DELETE ${LABEL_URL}`]: noContent()});

        await userEvent.click(screen.getByRole('button', {name: 'Delete'}));

        expect(screen.getByRole('alertdialog', {name: 'Delete label'})).toHaveTextContent(
            'Delete “Ghost” from the archive? Its credits and memberships go with it.',
        );

        await userEvent.click(screen.getByRole('button', {name: 'Delete for good'}));

        await vi.waitFor(() => {
            expect(window.location.pathname).toBe('/authors');
        });

        expect(fetchMock.mock.calls.some(([url, init]) => url === LABEL_URL && init?.method === 'DELETE')).toBe(true);
    });

    it('shows a refusal of deleting inside the confirmation', async () => {
        await openForm(ADMIN, GHOST, {
            [`DELETE ${LABEL_URL}`]: json({status: 422, code: 'author.nameless'}, 422),
        });

        await userEvent.click(screen.getByRole('button', {name: 'Delete'}));
        await userEvent.click(screen.getByRole('button', {name: 'Delete for good'}));

        await expect(within(screen.getByRole('alertdialog', {name: 'Delete label'})).findByRole('alert'))
            .resolves.toHaveTextContent('This label is the only name of its owner: it cannot be taken away from them.');
    });

    it('shows the not found page for a label not in the archive', async () => {
        stubApi(ADMIN, {[`GET ${LABEL_URL}`]: json({status: 404, code: 'label.not-found'}, 404)});
        openAt(`/labels/${ID}/edit`);

        await expect(screen.findByRole('heading', {level: 1, name: 'Not found'})).resolves.toBeInTheDocument();
    });

    it('shows the not found page for an id that cannot be one, asking nothing', async () => {
        const fetchMock = stubApi(ADMIN, {});
        openAt('/labels/nonsense/edit');

        await expect(screen.findByRole('heading', {level: 1, name: 'Not found'})).resolves.toBeInTheDocument();
        expect(fetchMock.mock.calls.some(([input]) => urlOf(input).startsWith('/api/labels/'))).toBe(false);
    });
});
