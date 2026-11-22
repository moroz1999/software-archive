import {afterEach, describe, expect, it, vi} from 'vitest';
import {render, screen, within} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {createQueryClient} from '@/shared/api';
import {json, noContent} from '@/shared/lib/test';
import {App} from './App';
import {type AppRouter, createAppRouter} from './router';

const MEMBER = {id: '00000000-0000-0000-0009-000000000001', name: 'Zed', email: 'zed@example.com', groups: ['member'], abilities: ['add-entries']};
const ADMIN = {...MEMBER, groups: ['admin', 'member'], abilities: ['add-entries', 'keep-categories']};

const FINLAND = '00000000-0000-0000-0010-000000000001';
const SWEDEN = '00000000-0000-0000-0010-000000000002';
const HELSINKI = '00000000-0000-0000-0010-000000000011';
const KOVROV = '00000000-0000-0000-0010-000000000021';
const ABAKAN = '00000000-0000-0000-0010-000000000022';
const NEW_ID = '00000000-0000-0000-0010-000000000099';
const SUOMI = '00000000-0000-0000-0010-000000000003';
const HELSINGFORS = '00000000-0000-0000-0010-000000000012';
const STOCKHOLM = '00000000-0000-0000-0010-000000000031';
const CITIES_OF_NO_COUNTRY = 'GET /api/geography/cities-of-no-country';

const GEOGRAPHY = [
    {id: FINLAND, name: 'Finland', places: 3, cities: [{id: HELSINKI, name: 'Helsinki', places: 1}]},
    {id: SWEDEN, name: 'Sweden', places: 0, cities: []},
];
const COUNTRIES = [{id: FINLAND, name: 'Finland'}, {id: SWEDEN, name: 'Sweden'}];
const FINLAND_DETAILS = {id: FINLAND, names: {ru: 'Финляндия', en: 'Finland'}, latitude: 64, longitude: 26};
const HELSINKI_DETAILS = {id: HELSINKI, names: {ru: 'Хельсинки', en: 'Helsinki'}, countryId: FINLAND, latitude: null, longitude: null};

type Answer = () => Response;
type Routes = Readonly<Record<string, Answer>>;

function urlOf(input: Parameters<typeof fetch>[0]): string {
    return typeof input === 'string' ? input : input instanceof URL ? input.href : input.url;
}

/** Answers every fetch by its method and URL; anything else fails like a network failure, showing up as a system error. */
function stubApi(user: typeof MEMBER, routes: Routes = {}) {
    const answers = new Map<string, Answer>([
        ['GET /api/auth/me', json(user)],
        ['GET /api/platforms', json([])],
        ['GET /api/geography', json(GEOGRAPHY)],
        ['GET /api/geography/cities-of-no-country', json([])],
        ['GET /api/geography/duplicates', json({countries: [], cities: []})],
        ['GET /api/countries', json(COUNTRIES)],
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

function bodyOf(fetchMock: ReturnType<typeof stubApi>, method: string, url: string): unknown {
    const call = fetchMock.mock.calls.find(([input, init]) => urlOf(input) === url && init?.method === method);
    const body = call?.[1]?.body;
    return typeof body === 'string' ? JSON.parse(body) : undefined;
}

function refusal(status: number, code: string, field?: string): Answer {
    return json({status, code, detail: code, ...(field === undefined ? {} : {errors: [{field, code}]})}, status);
}

describe('geography routes', () => {
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

    async function backAtTheTree() {
        await expect(screen.findByRole('heading', {level: 1, name: 'Geography'})).resolves.toBeInTheDocument();
        expect(window.location.pathname).toBe('/management/geography');
    }

    it('shows the countries with how many places are in them, their cities under them, each leading to its editing', async () => {
        stubApi(ADMIN);
        openAt('/management/geography');

        await expect(screen.findByRole('link', {name: 'Finland'})).resolves.toHaveAttribute('href', `/management/geography/countries/${FINLAND}/edit`);
        expect(screen.getByText('3 places')).toBeInTheDocument();
        expect(screen.getByText('0 places')).toBeInTheDocument();

        await userEvent.click(screen.getByRole('button', {name: 'Expand Finland'}));

        expect(screen.getByRole('link', {name: 'Helsinki'})).toHaveAttribute('href', `/management/geography/cities/${HELSINKI}/edit`);
        expect(screen.getByText('1 place')).toBeInTheDocument();
    });

    it('is a section of the management', async () => {
        stubApi(ADMIN);
        openAt('/management/geography');

        await expect(screen.findByRole('heading', {level: 1, name: 'Geography'})).resolves.toBeInTheDocument();
        expect(within(screen.getByRole('navigation', {name: 'Management'})).getByRole('link', {name: 'Geography'})).toHaveAttribute('aria-current', 'page');
    });

    it('leads to adding a country and a city', async () => {
        stubApi(ADMIN);
        openAt('/management/geography');

        await expect(screen.findByRole('link', {name: 'Add country'})).resolves.toHaveAttribute('href', '/management/geography/countries/new');
        expect(screen.getByRole('link', {name: 'Add city'})).toHaveAttribute('href', '/management/geography/cities/new');
    });

    it('hides the geography from a member', async () => {
        stubApi(MEMBER);
        openAt('/management/geography');

        await expect(screen.findByRole('heading', {level: 1, name: 'Not found'})).resolves.toBeInTheDocument();
    });

    it('adds a country with its names and coordinates and returns to the tree', async () => {
        const fetchMock = stubApi(ADMIN, {'POST /api/countries': json({id: NEW_ID}, 201)});
        openAt('/management/geography/countries/new');

        await userEvent.type(await screen.findByRole('textbox', {name: 'Name in Russian'}), 'Норвегия');
        await userEvent.type(screen.getByRole('textbox', {name: 'Name in English'}), 'Norway');
        await userEvent.type(screen.getByRole('textbox', {name: 'Latitude'}), '60.5');
        await userEvent.type(screen.getByRole('textbox', {name: 'Longitude'}), '8.5');
        await userEvent.click(screen.getByRole('button', {name: 'Add country'}));

        await backAtTheTree();

        expect(bodyOf(fetchMock, 'POST', '/api/countries')).toStrictEqual({names: {ru: 'Норвегия', en: 'Norway'}, latitude: 60.5, longitude: 8.5});
    });

    it('asks for the Russian name and for both coordinates on the globe before sending', async () => {
        const fetchMock = stubApi(ADMIN);
        openAt('/management/geography/countries/new');

        await userEvent.type(await screen.findByRole('textbox', {name: 'Latitude'}), '91');
        await userEvent.click(screen.getByRole('button', {name: 'Add country'}));

        expect(screen.getByRole('textbox', {name: 'Name in Russian'})).toHaveAccessibleDescription('Enter the name in Russian.');
        expect(screen.getByRole('textbox', {name: 'Latitude'})).toHaveAccessibleDescription('Latitude is a number from −90 to 90.');
        expect(screen.getByRole('textbox', {name: 'Longitude'})).toHaveAccessibleDescription('Give both coordinates or none.');
        expect(fetchMock.mock.calls.some(([, init]) => init?.method === 'POST')).toBe(false);
    });

    it('saves a country as edited', async () => {
        const fetchMock = stubApi(ADMIN, {
            [`GET /api/countries/${FINLAND}`]: json(FINLAND_DETAILS),
            [`PUT /api/countries/${FINLAND}`]: noContent(),
        });
        openAt(`/management/geography/countries/${FINLAND}/edit`);

        const russian = await screen.findByRole('textbox', {name: 'Name in Russian'});

        expect(russian).toHaveValue('Финляндия');
        expect(screen.getByRole('textbox', {name: 'Latitude'})).toHaveValue('64');

        await userEvent.clear(russian);
        await userEvent.type(russian, 'Суоми');
        await userEvent.clear(screen.getByRole('textbox', {name: 'Latitude'}));
        await userEvent.clear(screen.getByRole('textbox', {name: 'Longitude'}));
        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        await backAtTheTree();

        expect(bodyOf(fetchMock, 'PUT', `/api/countries/${FINLAND}`)).toStrictEqual({names: {ru: 'Суоми', en: 'Finland'}, latitude: null, longitude: null});
    });

    it('says inside the confirmation why a country with cities or places stays', async () => {
        stubApi(ADMIN, {
            [`GET /api/countries/${FINLAND}`]: json(FINLAND_DETAILS),
            [`DELETE /api/countries/${FINLAND}`]: refusal(422, 'geo.country-in-use'),
        });
        openAt(`/management/geography/countries/${FINLAND}/edit`);

        await userEvent.click(await screen.findByRole('button', {name: 'Delete'}));

        expect(screen.getByRole('alertdialog')).toHaveTextContent('Delete “Finland” from the archive?');

        await userEvent.click(screen.getByRole('button', {name: 'Delete for good'}));

        await expect(within(screen.getByRole('alertdialog')).findByText('The country has cities or places in it: it cannot be deleted.')).resolves.toBeInTheDocument();
    });

    it('adds a city in the country chosen', async () => {
        const fetchMock = stubApi(ADMIN, {'POST /api/cities': json({id: NEW_ID}, 201)});
        openAt('/management/geography/cities/new');

        await userEvent.type(await screen.findByRole('textbox', {name: 'Name in Russian'}), 'Турку');
        await userEvent.selectOptions(screen.getByRole('combobox', {name: 'Country'}), await screen.findByRole('option', {name: 'Finland'}));
        await userEvent.click(screen.getByRole('button', {name: 'Add city'}));

        await backAtTheTree();

        expect(bodyOf(fetchMock, 'POST', '/api/cities')).toStrictEqual({names: {ru: 'Турку'}, countryId: FINLAND, latitude: null, longitude: null});
    });

    it('moves a city to another country, and says under the country when that one is gone', async () => {
        const fetchMock = stubApi(ADMIN, {
            [`GET /api/cities/${HELSINKI}`]: json(HELSINKI_DETAILS),
            [`PUT /api/cities/${HELSINKI}`]: refusal(422, 'geo.country-unknown', 'countryId'),
        });
        openAt(`/management/geography/cities/${HELSINKI}/edit`);

        const country = await screen.findByRole('combobox', {name: 'Country'});

        expect(country).toHaveValue(FINLAND);

        await userEvent.selectOptions(country, screen.getByRole('option', {name: 'Sweden'}));
        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        await expect(screen.findByText('The country is not in the archive any more. Reload and try again.')).resolves.toBeInTheDocument();
        expect(bodyOf(fetchMock, 'PUT', `/api/cities/${HELSINKI}`)).toStrictEqual({names: {ru: 'Хельсинки', en: 'Helsinki'}, countryId: SWEDEN, latitude: null, longitude: null});
    });

    it('deletes a city and returns to the tree', async () => {
        const fetchMock = stubApi(ADMIN, {
            [`GET /api/cities/${HELSINKI}`]: json(HELSINKI_DETAILS),
            [`DELETE /api/cities/${HELSINKI}`]: noContent(),
        });
        openAt(`/management/geography/cities/${HELSINKI}/edit`);

        await userEvent.click(await screen.findByRole('button', {name: 'Delete'}));
        await userEvent.click(screen.getByRole('button', {name: 'Delete for good'}));

        await backAtTheTree();

        expect(fetchMock.mock.calls.some(([input, init]) => urlOf(input) === `/api/cities/${HELSINKI}` && init?.method === 'DELETE')).toBe(true);
    });

    it('says inside the confirmation why a city places are in stays', async () => {
        stubApi(ADMIN, {
            [`GET /api/cities/${HELSINKI}`]: json(HELSINKI_DETAILS),
            [`DELETE /api/cities/${HELSINKI}`]: refusal(422, 'geo.city-in-use'),
        });
        openAt(`/management/geography/cities/${HELSINKI}/edit`);

        await userEvent.click(await screen.findByRole('button', {name: 'Delete'}));
        await userEvent.click(screen.getByRole('button', {name: 'Delete for good'}));

        await expect(within(screen.getByRole('alertdialog')).findByText('People, groups or labels are located in the city: it cannot be deleted.')).resolves.toBeInTheDocument();
    });

    it('shows the cities of no country over the tree, each with how many places are in it and leading to its editing', async () => {
        stubApi(ADMIN, {[CITIES_OF_NO_COUNTRY]: json([{id: KOVROV, name: 'Kovrov', places: 2}])});
        openAt('/management/geography');

        const block = within(await screen.findByRole('region', {name: 'Cities of no country'}));

        expect(block.getByRole('link', {name: 'Kovrov'})).toHaveAttribute('href', `/management/geography/cities/${KOVROV}/edit`);
        expect(block.getByRole('cell', {name: '2'})).toBeInTheDocument();
        expect(screen.getByRole('region', {name: 'Cities of no country'}).compareDocumentPosition(screen.getByRole('list', {name: 'Geography'}))).toBe(Node.DOCUMENT_POSITION_FOLLOWING);
    });

    it('lines the cities of no country up in a table: the city, its places, its country to choose and what else can be done', async () => {
        stubApi(ADMIN, {[CITIES_OF_NO_COUNTRY]: json([{id: KOVROV, name: 'Kovrov', places: 2}, {id: ABAKAN, name: 'Abakan', places: 0}])});
        openAt('/management/geography');

        const table = await screen.findByRole('table', {name: 'Cities of no country'});

        expect(within(table).getAllByRole('columnheader').map((header) => header.textContent)).toStrictEqual(['City', 'Places', 'Country', '']);

        const rows = within(table).getAllByRole('row').slice(1);

        expect(rows.map((row) => within(row).getAllByRole('cell').length)).toStrictEqual([4, 4]);
        expect(within(rows[1] ?? table).getByRole('combobox', {name: 'Country for Abakan'})).toBeInTheDocument();
        expect(within(rows[1] ?? table).getByRole('button', {name: 'Make Abakan a country'})).toBeInTheDocument();
    });

    it('makes a city of no country a country once asked, and the city leaves the list', async () => {
        let made = false;
        const fetchMock = stubApi(ADMIN, {
            [CITIES_OF_NO_COUNTRY]: () => json(made ? [] : [{id: KOVROV, name: 'Kovrov', places: 2}])(),
            [`POST /api/cities/${KOVROV}/to-country`]: () => {
                made = true;
                return json({id: NEW_ID}, 201)();
            },
        });
        openAt('/management/geography');

        await userEvent.click(await screen.findByRole('button', {name: 'Make Kovrov a country'}));

        expect(screen.getByRole('alertdialog')).toHaveTextContent('Make “Kovrov” a country? Its places move to the new country, and the city is deleted.');

        await userEvent.click(within(screen.getByRole('alertdialog')).getByRole('button', {name: 'Make a country'}));

        await expect(screen.findByText('Every city is in a country.')).resolves.toBeInTheDocument();
        expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument();
        expect(fetchMock.mock.calls.some(([input, init]) => urlOf(input) === `/api/cities/${KOVROV}/to-country` && init?.method === 'POST')).toBe(true);
    });

    it.each([
        ['geo.city-has-country', 'The city is in a country already. Reload and try again.'],
        ['geo.city-in-use-by-label', 'An old record of a label names the city: it cannot become a country.'],
    ])('says inside the confirmation why a city is not made a country (%s)', async (code, message) => {
        stubApi(ADMIN, {
            [CITIES_OF_NO_COUNTRY]: json([{id: KOVROV, name: 'Kovrov', places: 2}]),
            [`POST /api/cities/${KOVROV}/to-country`]: refusal(422, code),
        });
        openAt('/management/geography');

        await userEvent.click(await screen.findByRole('button', {name: 'Make Kovrov a country'}));
        await userEvent.click(within(screen.getByRole('alertdialog')).getByRole('button', {name: 'Make a country'}));

        await expect(within(screen.getByRole('alertdialog')).findByText(message)).resolves.toBeInTheDocument();
    });

    it('says when every city is in a country', async () => {
        stubApi(ADMIN);
        openAt('/management/geography');

        const block = within(await screen.findByRole('region', {name: 'Cities of no country'}));

        expect(block.getByText('Every city is in a country.')).toBeInTheDocument();
    });

    it('puts a city of no country in the country chosen, and the city leaves the list', async () => {
        let put = false;
        const fetchMock = stubApi(ADMIN, {
            [CITIES_OF_NO_COUNTRY]: () => json(put ? [] : [{id: KOVROV, name: 'Kovrov', places: 2}])(),
            [`PUT /api/cities/${KOVROV}/country`]: () => {
                put = true;
                return noContent()();
            },
        });
        openAt('/management/geography');

        const country = await screen.findByRole('combobox', {name: 'Country for Kovrov'});

        expect(screen.getByRole('button', {name: 'Put Kovrov in the country'})).toBeDisabled();

        await userEvent.selectOptions(country, await screen.findByRole('option', {name: 'Finland'}));
        await userEvent.click(screen.getByRole('button', {name: 'Put Kovrov in the country'}));

        await expect(screen.findByText('Every city is in a country.')).resolves.toBeInTheDocument();
        expect(bodyOf(fetchMock, 'PUT', `/api/cities/${KOVROV}/country`)).toStrictEqual({countryId: FINLAND});
    });

    it('says next to the city when its country is gone', async () => {
        stubApi(ADMIN, {
            [CITIES_OF_NO_COUNTRY]: json([{id: KOVROV, name: 'Kovrov', places: 2}]),
            [`PUT /api/cities/${KOVROV}/country`]: refusal(422, 'geo.country-unknown', 'countryId'),
        });
        openAt('/management/geography');

        await userEvent.selectOptions(await screen.findByRole('combobox', {name: 'Country for Kovrov'}), await screen.findByRole('option', {name: 'Finland'}));
        await userEvent.click(screen.getByRole('button', {name: 'Put Kovrov in the country'}));

        await expect(screen.findByText('The country is not in the archive any more. Reload and try again.')).resolves.toBeInTheDocument();
        expect(screen.getByRole('link', {name: 'Kovrov'})).toBeInTheDocument();
    });

    it('shows the countries and the cities that may be duplicates, each leading to its editing', async () => {
        stubApi(ADMIN, {'GET /api/geography/duplicates': json({
            countries: [[{id: FINLAND, name: 'Finland', places: 3}, {id: SUOMI, name: 'Suomi', places: 0}]],
            cities: [{country: 'Finland', cities: [
                {id: HELSINKI, name: 'Helsinki', country: 'Finland', places: 1},
                {id: KOVROV, name: 'Helsinki', country: null, places: 0},
            ]}],
        })});
        openAt('/management/geography');

        const block = within(await screen.findByRole('region', {name: 'Possible duplicates'}));

        expect(block.getByRole('link', {name: 'Suomi'})).toHaveAttribute('href', `/management/geography/countries/${SUOMI}/edit`);
        expect(block.getByRole('link', {name: 'Helsinki, Finland'})).toHaveAttribute('href', `/management/geography/cities/${HELSINKI}/edit`);
        expect(block.getByRole('link', {name: 'Helsinki, country not specified'})).toHaveAttribute('href', `/management/geography/cities/${KOVROV}/edit`);
        expect(block.getByText('1 place')).toBeInTheDocument();
    });

    it('says when no duplicates are found', async () => {
        stubApi(ADMIN);
        openAt('/management/geography');

        const block = within(await screen.findByRole('region', {name: 'Possible duplicates'}));

        expect(block.getByText('No possible duplicates.')).toBeInTheDocument();
    });

    it('merges a city into another one chosen in its country and returns to the tree', async () => {
        const fetchMock = stubApi(ADMIN, {
            [`GET /api/cities/${HELSINGFORS}`]: json({...HELSINKI_DETAILS, id: HELSINGFORS, names: {ru: 'Гельсингфорс'}}),
            [`GET /api/countries/${SWEDEN}/cities`]: json([{id: STOCKHOLM, name: 'Stockholm'}]),
            [`POST /api/cities/${HELSINGFORS}/merge`]: noContent(),
        });
        openAt(`/management/geography/cities/${HELSINGFORS}/edit`);

        await userEvent.click(await screen.findByRole('button', {name: 'Merge into another'}));
        const dialog = within(screen.getByRole('dialog', {name: 'Merge city'}));

        expect(dialog.getByText('Its places move to the city chosen, and “Гельсингфорс” is deleted.')).toBeInTheDocument();

        await userEvent.selectOptions(dialog.getByRole('combobox', {name: 'Country'}), dialog.getByRole('option', {name: 'Sweden'}));
        await userEvent.selectOptions(dialog.getByRole('combobox', {name: 'City'}), await dialog.findByRole('option', {name: 'Stockholm'}));
        await userEvent.click(dialog.getByRole('button', {name: 'Merge'}));

        await backAtTheTree();

        expect(bodyOf(fetchMock, 'POST', `/api/cities/${HELSINGFORS}/merge`)).toStrictEqual({into: STOCKHOLM});
    });

    it('offers no city to merge into before one is chosen, and not the city itself', async () => {
        stubApi(ADMIN, {
            [`GET /api/cities/${HELSINKI}`]: json(HELSINKI_DETAILS),
            [`GET /api/countries/${FINLAND}/cities`]: json([{id: HELSINKI, name: 'Helsinki'}, {id: HELSINGFORS, name: 'Helsingfors'}]),
        });
        openAt(`/management/geography/cities/${HELSINKI}/edit`);

        await userEvent.click(await screen.findByRole('button', {name: 'Merge into another'}));
        const dialog = within(screen.getByRole('dialog', {name: 'Merge city'}));

        expect(dialog.getByRole('button', {name: 'Merge'})).toBeDisabled();

        await userEvent.selectOptions(dialog.getByRole('combobox', {name: 'Country'}), dialog.getByRole('option', {name: 'Finland'}));

        await expect(dialog.findByRole('option', {name: 'Helsingfors'})).resolves.toBeInTheDocument();
        expect(dialog.queryByRole('option', {name: 'Helsinki'})).not.toBeInTheDocument();
    });

    it('says inside the dialog why a city is not merged', async () => {
        stubApi(ADMIN, {
            [`GET /api/cities/${HELSINKI}`]: json(HELSINKI_DETAILS),
            [`GET /api/countries/${SWEDEN}/cities`]: json([{id: STOCKHOLM, name: 'Stockholm'}]),
            [`POST /api/cities/${HELSINKI}/merge`]: refusal(422, 'geo.city-unknown', 'into'),
        });
        openAt(`/management/geography/cities/${HELSINKI}/edit`);

        await userEvent.click(await screen.findByRole('button', {name: 'Merge into another'}));
        const dialog = within(screen.getByRole('dialog', {name: 'Merge city'}));
        await userEvent.selectOptions(dialog.getByRole('combobox', {name: 'Country'}), dialog.getByRole('option', {name: 'Sweden'}));
        await userEvent.selectOptions(dialog.getByRole('combobox', {name: 'City'}), await dialog.findByRole('option', {name: 'Stockholm'}));
        await userEvent.click(dialog.getByRole('button', {name: 'Merge'}));

        await expect(dialog.findByText('The city is not in the archive any more. Reload and try again.')).resolves.toBeInTheDocument();
    });

    it('merges a country into another one, not offering itself', async () => {
        const fetchMock = stubApi(ADMIN, {
            [`GET /api/countries/${FINLAND}`]: json(FINLAND_DETAILS),
            [`POST /api/countries/${FINLAND}/merge`]: noContent(),
        });
        openAt(`/management/geography/countries/${FINLAND}/edit`);

        await userEvent.click(await screen.findByRole('button', {name: 'Merge into another'}));
        const dialog = within(screen.getByRole('dialog', {name: 'Merge country'}));

        expect(dialog.queryByRole('option', {name: 'Finland'})).not.toBeInTheDocument();

        await userEvent.selectOptions(dialog.getByRole('combobox', {name: 'Country'}), dialog.getByRole('option', {name: 'Sweden'}));
        await userEvent.click(dialog.getByRole('button', {name: 'Merge'}));

        await backAtTheTree();

        expect(bodyOf(fetchMock, 'POST', `/api/countries/${FINLAND}/merge`)).toStrictEqual({into: SWEDEN});
    });
});
