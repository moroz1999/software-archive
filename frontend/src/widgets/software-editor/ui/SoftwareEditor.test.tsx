import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen, within} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {MemoryRouter} from 'react-router';
import {breadcrumbSteps, json, noContent, renderWithQueries, stubFetchAround, stubFetchRoutes} from '@/shared/lib/test';
import {SoftwareEditor} from './SoftwareEditor';

const ID = '00000000-0000-0000-0001-000000000001';
const ELITE = {id: ID, title: 'Elite', description: 'Space trading', releaseDate: '1985-09-20', platform: {code: 'vector', name: 'Vector'}, hardware: [],
    categories: [{
        id: '00000000-0000-0000-0006-000000000002',
        title: 'Action',
        branch: [{id: '00000000-0000-0000-0006-000000000001', title: 'Games'}, {id: '00000000-0000-0000-0006-000000000002', title: 'Action'}],
    }], credits: [], sources: [], preservation: 'unknown', instructions: null, compilationItems: [], compilations: [], origins: [], added: {by: 'Zed', at: '2026-01-01T00:00:00Z'}, canChange: true};
const GAMES = '00000000-0000-0000-0006-000000000001';
const ACTION = '00000000-0000-0000-0006-000000000002';
const TREE = [{id: GAMES, title: 'Games', children: [{id: ACTION, title: 'Action', children: []}]}];
/** What the editor loads besides the software itself. */
const SHOT = {id: '00000000-0000-0000-0055-000000000001', url: '/api/screenshots/00000000-0000-0000-0055-000000000001'};
const AROUND = new Map([
    [`/api/software/${ID}/screenshots`, json([SHOT])],
    [`/api/software/${ID}/maps`, json([SHOT])],
    [`/api/software/${ID}/covers`, json([])],
    [`/api/software/${ID}/adverts`, json([])],
    ['/api/categories', json(TREE)],
    ['/api/platforms', json([{code: 'vector', name: 'Vector'}])],
    ['/api/hardware?platform=vector', json([])],
]);

describe(SoftwareEditor, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('shows a skeleton, then the form with the software and a delete button', async () => {
        stubFetchAround(AROUND, json(ELITE));
        renderWithQueries(<MemoryRouter><SoftwareEditor id={ID} onSaved={vi.fn<(id: string) => void>()} onDeleted={vi.fn<() => void>()}/></MemoryRouter>);

        expect(screen.getByTestId('software-editor-skeleton')).toBeInTheDocument();
        await expect(screen.findByRole('textbox', {name: 'Title'})).resolves.toHaveValue('Elite');
        expect(screen.getByRole('button', {name: 'Delete'})).toBeInTheDocument();
    });

    it('shows where imports took the software from, as text, with its ids there', async () => {
        stubFetchAround(AROUND, json({...ELITE, origins: [{origin: 'vektor-06c.ru', externalId: '7', url: 'https://vektor-06c.ru/elite/'}]}));
        renderWithQueries(<MemoryRouter><SoftwareEditor id={ID} onSaved={vi.fn<(id: string) => void>()} onDeleted={vi.fn<() => void>()}/></MemoryRouter>);

        await screen.findByRole('textbox', {name: 'Title'});

        expect(within(screen.getByRole('region', {name: 'Sources'})).getByRole('link', {name: '7'})).toHaveAttribute('href', 'https://vektor-06c.ru/elite/');
    });

    it('leads back from the editing to the software and on through the catalog', async () => {
        stubFetchAround(AROUND, json(ELITE));
        renderWithQueries(<MemoryRouter><SoftwareEditor id={ID} onSaved={vi.fn<(id: string) => void>()} onDeleted={vi.fn<() => void>()}/></MemoryRouter>);

        await screen.findByRole('textbox', {name: 'Title'});

        expect(breadcrumbSteps()).toStrictEqual([
            ['Software', '/software'],
            ['Games', `/software/category/${GAMES}`],
            ['Action', `/software/category/${ACTION}`],
            ['Elite', `/software/${ID}`],
            ['Editing', null],
        ]);
    });

    it('offers deleting next to the page title and leads back to the software from the form', async () => {
        stubFetchAround(AROUND, json(ELITE));
        renderWithQueries(<MemoryRouter><SoftwareEditor id={ID} onSaved={vi.fn<(id: string) => void>()} onDeleted={vi.fn<() => void>()}/></MemoryRouter>);

        const heading = await screen.findByRole('heading', {level: 1, name: 'Edit software'});
        await screen.findByRole('textbox', {name: 'Title'});

        // The title's line comes before the form: its Delete is the page's first button.
        expect(screen.getAllByRole('button').at(0)).toHaveAccessibleName('Delete');
        expect(heading).toBeInTheDocument();
        expect(screen.getByRole('link', {name: 'Cancel'})).toHaveAttribute('href', `/software/${ID}`);
    });

    it('offers the screenshots of the software to change below the form', async () => {
        stubFetchAround(AROUND, json(ELITE));
        renderWithQueries(<MemoryRouter><SoftwareEditor id={ID} onSaved={vi.fn<(id: string) => void>()} onDeleted={vi.fn<() => void>()}/></MemoryRouter>);

        const screenshots = await screen.findByRole('list', {name: 'Screenshots'});

        expect(within(screenshots).getByRole('img', {name: 'Screenshot 1'})).toHaveAttribute('src', SHOT.url);
        expect(screen.getByRole('button', {name: 'Add screenshots'})).toBeInTheDocument();
    });

    it('offers the maps, the covers and the adverts of the software to change after its screenshots', async () => {
        stubFetchAround(AROUND, json(ELITE));
        renderWithQueries(<MemoryRouter><SoftwareEditor id={ID} onSaved={vi.fn<(id: string) => void>()} onDeleted={vi.fn<() => void>()}/></MemoryRouter>);

        const maps = await screen.findByRole('list', {name: 'Maps'});

        expect(within(maps).getByRole('img', {name: 'Map 1'})).toHaveAttribute('src', SHOT.url);
        expect(screen.getByRole('button', {name: 'Add maps'})).toBeInTheDocument();
        await expect(screen.findByRole('button', {name: 'Add covers'})).resolves.toBeInTheDocument();
        await expect(screen.findByRole('button', {name: 'Add adverts'})).resolves.toBeInTheDocument();
    });

    it('reports the software saved', async () => {
        stubFetchAround(AROUND, json(ELITE), noContent());
        const onSaved = vi.fn<(id: string) => void>();
        renderWithQueries(<MemoryRouter><SoftwareEditor id={ID} onSaved={onSaved} onDeleted={vi.fn<() => void>()}/></MemoryRouter>);

        await screen.findByRole('textbox', {name: 'Title'});
        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        await vi.waitFor(() => {
            expect(onSaved).toHaveBeenCalledExactlyOnceWith(ID);
        });
    });

    it('reports the software deleted', async () => {
        stubFetchAround(AROUND, json(ELITE), noContent());
        const onDeleted = vi.fn<() => void>();
        renderWithQueries(<MemoryRouter><SoftwareEditor id={ID} onSaved={vi.fn<(id: string) => void>()} onDeleted={onDeleted}/></MemoryRouter>);

        await screen.findByRole('textbox', {name: 'Title'});
        await userEvent.click(screen.getByRole('button', {name: 'Delete'}));
        await userEvent.click(screen.getByRole('button', {name: 'Delete for good'}));

        await vi.waitFor(() => {
            expect(onDeleted).toHaveBeenCalledExactlyOnceWith();
        });
    });

    it('shows the categories of the software checked and saves the categories chosen', async () => {
        const fetchMock = stubFetchRoutes(new Map([...AROUND, [`/api/software/${ID}`, json(ELITE)]]));
        renderWithQueries(<MemoryRouter><SoftwareEditor id={ID} onSaved={vi.fn<(id: string) => void>()} onDeleted={vi.fn<() => void>()}/></MemoryRouter>);

        await expect(screen.findByRole('checkbox', {name: 'Action'})).resolves.toBeChecked();

        await userEvent.click(screen.getByRole('checkbox', {name: 'Games'}));
        await userEvent.click(screen.getByRole('button', {name: 'Remove Action'}));
        fetchMock.mockClear();
        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        await vi.waitFor(() => {
            expect(fetchMock.mock.calls.find((call) => call[1]?.method === 'PUT')?.[1]?.body).toContain(`"categories":["${GAMES}"]`);
        });
    });
});
