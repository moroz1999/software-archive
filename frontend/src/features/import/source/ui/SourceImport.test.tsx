import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen, within} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {MemoryRouter} from 'react-router';
import {json, renderWithQueries, stubFetchRoutes} from '@/shared/lib/test';
import {SourceImport} from './SourceImport';

const SECTIONS = '/api/imports/vektor/sections';
const START = '/api/imports/vektor';
const LAST_RUN = '/api/imports/last-run';
const NO_RUN = json({run: null});
const SOFTWARE = '00000000-0000-0000-0001-000000000001';
const G_DOS = 'https://vektor-06c.ru/programmnoe-obespechenie/zagruzchiki/g-dos/';
const BASIS_START = '/api/imports/basis';
const MICRODOS = '00000000-0000-0000-0001-000000000003';
const CPM = '00000000-0000-0000-0001-000000000004';
const DEMO = 'https://vektor-06c.ru/programmnoe-obespechenie/utility-dlya-pc/512-demo/';
const ZONE_X = 'https://caglrc.cc/scalar/ware/30/';
const GRAFOV = '00000000-0000-0000-0004-000000000001';
const IVAN = '00000000-0000-0000-0003-000000000001';
const CREW = '00000000-0000-0000-0005-000000000001';
const RUN = {
    id: '00000000-0000-0000-0000-00000000000a',
    archive: 'vektor-06c.ru',
    section: 'zagruzchiki',
    state: 'running',
    startedAt: '2026-11-14T12:00:00Z',
    endedAt: null,
    added: 2,
    known: 3,
    skipped: 0,
    failed: 1,
    addedPrograms: [
        {externalId: '599', softwareId: SOFTWARE, title: 'Disk Loader', exists: true},
        {externalId: '595', softwareId: '00000000-0000-0000-0001-000000000002', title: 'Masterpiece Boot', exists: false},
    ],
    failedPrograms: [{externalId: '598', url: G_DOS, reason: 'Timed out'}],
    knownPrograms: [
        {externalId: '754', softwareId: MICRODOS, title: 'МикроДОС', exists: true, supplements: ['description', 'categories']},
        {externalId: '753', softwareId: CPM, title: 'CP/M', exists: true, supplements: []},
    ],
    skippedPrograms: [
        {externalId: '524', url: DEMO, title: '512 demo', reason: 'section', detail: 'utility-dlya-pc'},
        {externalId: '30', url: ZONE_X, title: 'Zone X', reason: 'category', detail: 'pc'},
    ],
    authors: [
        {kind: 'label', id: GRAFOV, name: 'Графов А.', created: true, exists: true},
        {kind: 'author', id: IVAN, name: 'IVAN', created: true, exists: true},
        {kind: 'group', id: CREW, name: 'Crew', created: false, exists: true},
    ],
    places: [{kind: 'city', name: 'Ковров'}, {kind: 'country', name: 'Атлантида'}],
    progress: {position: 37, total: 512, url: G_DOS},
};

function renderImport() {
    renderWithQueries(<MemoryRouter><SourceImport/></MemoryRouter>);
}

function accepted(): () => Response {
    return () => new Response(null, {status: 202});
}

describe(SourceImport, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('offers vektor-06c.ru and the card index Basis as the sources, vektor-06c.ru first', async () => {
        stubFetchRoutes(new Map([[LAST_RUN, NO_RUN], [SECTIONS, json(['zagruzchiki'])]]));
        renderImport();

        const source = await screen.findByRole('combobox', {name: 'Source'});

        expect(source).toHaveDisplayValue('vektor-06c.ru');
        expect(within(source).getAllByRole('option').map((option) => option.textContent)).toStrictEqual(['vektor-06c.ru', 'caglrc.cc (Basis)']);
    });

    it('starts the import of the whole card index of Basis, which has no sections', async () => {
        const fetchMock = stubFetchRoutes(new Map([[LAST_RUN, NO_RUN], [SECTIONS, json(['zagruzchiki'])], [BASIS_START, accepted()]]));
        renderImport();

        await screen.findByRole('option', {name: 'zagruzchiki'});
        await userEvent.selectOptions(screen.getByRole('combobox', {name: 'Source'}), 'caglrc.cc (Basis)');

        expect(screen.queryByRole('combobox', {name: 'Section'})).not.toBeInTheDocument();

        await userEvent.click(screen.getByRole('button', {name: 'Start'}));

        await expect(screen.findByRole('status')).resolves.toHaveTextContent('The import has started.');
        expect(fetchMock).toHaveBeenCalledWith(BASIS_START, expect.objectContaining({method: 'POST'}));
    });

    it('names the archive the last import read', async () => {
        stubFetchRoutes(new Map([[LAST_RUN, json({run: {...RUN, archive: 'caglrc.cc', section: null}})], [SECTIONS, json(['zagruzchiki'])]]));
        renderImport();

        const report = within(await screen.findByRole('region', {name: 'Last import'}));

        await expect(report.findByText('caglrc.cc (Basis)')).resolves.toBeInTheDocument();
        expect(report.queryByText('Whole site')).not.toBeInTheDocument();
    });

    it('offers the whole site and every section to import', async () => {
        stubFetchRoutes(new Map([[LAST_RUN, NO_RUN], [SECTIONS, json(['arkadnye-igry', 'zagruzchiki'])]]));
        renderImport();

        const section = await screen.findByRole('combobox', {name: 'Section'});

        await expect(within(section).findByRole('option', {name: 'zagruzchiki'})).resolves.toBeInTheDocument();
        expect(within(section).getAllByRole('option').map((option) => option.textContent)).toStrictEqual(['Whole site', 'arkadnye-igry', 'zagruzchiki']);
    });

    it('starts the import of the chosen section and says it goes on in the background', async () => {
        const fetchMock = stubFetchRoutes(new Map([[LAST_RUN, NO_RUN], [SECTIONS, json(['arkadnye-igry', 'zagruzchiki'])], [`${START}?section=zagruzchiki`, accepted()]]));
        renderImport();

        await userEvent.selectOptions(await screen.findByRole('combobox', {name: 'Section'}), await screen.findByRole('option', {name: 'zagruzchiki'}));
        await userEvent.click(screen.getByRole('button', {name: 'Start'}));

        await expect(screen.findByRole('status')).resolves.toHaveTextContent('The import has started.');
        expect(fetchMock).toHaveBeenCalledWith(`${START}?section=zagruzchiki`, expect.objectContaining({method: 'POST'}));
    });

    it('starts the import of the whole site when no section is chosen', async () => {
        const fetchMock = stubFetchRoutes(new Map([[LAST_RUN, NO_RUN], [SECTIONS, json(['zagruzchiki'])], [START, accepted()]]));
        renderImport();

        await screen.findByRole('option', {name: 'zagruzchiki'});
        await userEvent.click(screen.getByRole('button', {name: 'Start'}));

        await screen.findByRole('status');

        expect(fetchMock).toHaveBeenCalledWith(START, expect.objectContaining({method: 'POST'}));
    });

    it('says so when an import is already running', async () => {
        stubFetchRoutes(new Map([[LAST_RUN, NO_RUN], [SECTIONS, json(['zagruzchiki'])], [START, json({status: 409, code: 'import.running'}, 409)]]));
        renderImport();

        await screen.findByRole('option', {name: 'zagruzchiki'});
        await userEvent.click(screen.getByRole('button', {name: 'Start'}));

        await expect(screen.findByRole('alert')).resolves.toHaveTextContent('An import is already running.');
        expect(screen.queryByRole('status')).not.toBeInTheDocument();
    });

    it('says when nothing has been imported yet', async () => {
        stubFetchRoutes(new Map([[LAST_RUN, NO_RUN], [SECTIONS, json(['zagruzchiki'])]]));
        renderImport();

        const report = await screen.findByRole('region', {name: 'Last import'});

        await expect(within(report).findByText('Nothing has been imported yet.')).resolves.toBeInTheDocument();
    });

    it('reports what the last import did', async () => {
        stubFetchRoutes(new Map([[LAST_RUN, json({run: RUN})], [SECTIONS, json(['zagruzchiki'])]]));
        renderImport();

        const report = within(await screen.findByRole('region', {name: 'Last import'}));

        await expect(report.findByText('vektor-06c.ru · zagruzchiki')).resolves.toBeInTheDocument();
        expect(report.getByText('Running')).toBeInTheDocument();
        expect(report.getByText(/Started 14\.11\.2026/u)).toBeInTheDocument();
        expect(report.getByText('Added 2 · already there 3 · left out 0 · failed 1')).toBeInTheDocument();
    });

    it('leads to the software it added and to the pages of the programs that failed', async () => {
        stubFetchRoutes(new Map([[LAST_RUN, json({run: RUN})], [SECTIONS, json(['zagruzchiki'])]]));
        renderImport();

        const added = within(await screen.findByRole('region', {name: 'Added'}));
        const failed = within(screen.getByRole('region', {name: 'Failed'}));

        expect(added.getByRole('link', {name: 'Disk Loader'})).toHaveAttribute('href', `/software/${SOFTWARE}`);
        expect(added.getByText('Masterpiece Boot (deleted since)')).toBeInTheDocument();
        expect(failed.getByRole('link', {name: G_DOS})).toHaveAttribute('href', G_DOS);
        expect(failed.getByText('Timed out')).toBeInTheDocument();
    });

    it('shows the report of the import it has just started', async () => {
        let started = false;
        stubFetchRoutes(new Map([
            [LAST_RUN, () => (started ? json({run: RUN}) : NO_RUN)()],
            [SECTIONS, json(['zagruzchiki'])],
            [START, () => {
                started = true;
                return accepted()();
            }],
        ]));
        renderImport();

        await screen.findByText('Nothing has been imported yet.');
        await userEvent.click(screen.getByRole('button', {name: 'Start'}));

        await expect(within(screen.getByRole('region', {name: 'Last import'})).findByText('Running')).resolves.toBeInTheDocument();
    });

    it('says which program the running import goes to now, leading to its page', async () => {
        stubFetchRoutes(new Map([[LAST_RUN, json({run: RUN})], [SECTIONS, json(['zagruzchiki'])]]));
        renderImport();

        const report = within(await screen.findByRole('region', {name: 'Last import'}));

        await expect(report.findByRole('link', {name: 'Now: program 37 of 512'})).resolves.toHaveAttribute('href', G_DOS);
    });

    it('says the running import reads the list before the first program, and counts none it was not told of', async () => {
        stubFetchRoutes(new Map([[LAST_RUN, json({run: {...RUN, progress: {position: 0, total: null, url: null}}})], [SECTIONS, json(['zagruzchiki'])]]));
        renderImport();

        const report = within(await screen.findByRole('region', {name: 'Last import'}));

        await expect(report.findByText('Now: reading the list of programs')).resolves.toBeInTheDocument();
    });

    it('counts no programs the source did not tell the number of', async () => {
        stubFetchRoutes(new Map([[LAST_RUN, json({run: {...RUN, progress: {position: 37, total: null, url: G_DOS}}})], [SECTIONS, json(['zagruzchiki'])]]));
        renderImport();

        const report = within(await screen.findByRole('region', {name: 'Last import'}));

        await expect(report.findByRole('link', {name: 'Now: program 37'})).resolves.toBeInTheDocument();
    });

    it('tells nothing of a current program once the import ended', async () => {
        stubFetchRoutes(new Map([[LAST_RUN, json({run: {...RUN, state: 'done', endedAt: '2026-11-14T13:00:00Z'}})], [SECTIONS, json(['zagruzchiki'])]]));
        renderImport();

        const report = within(await screen.findByRole('region', {name: 'Last import'}));
        await report.findByText('Done');

        expect(report.queryByText(/^Now:/u)).not.toBeInTheDocument();
    });

    it('lists the programs it found in the archive with what it added to each', async () => {
        stubFetchRoutes(new Map([[LAST_RUN, json({run: RUN})], [SECTIONS, json(['zagruzchiki'])]]));
        renderImport();

        const supplemented = within(await screen.findByRole('region', {name: 'Supplemented'}));
        const unchanged = within(screen.getByRole('region', {name: 'Already there, nothing added'}));

        expect(supplemented.getByRole('link', {name: 'МикроДОС'})).toHaveAttribute('href', `/software/${MICRODOS}`);
        expect(supplemented.getByText('description, categories')).toBeInTheDocument();
        expect(unchanged.getByRole('link', {name: 'CP/M'})).toHaveAttribute('href', `/software/${CPM}`);
        expect(supplemented.queryByRole('link', {name: 'CP/M'})).not.toBeInTheDocument();
    });

    it('lists the programs it left out with why', async () => {
        stubFetchRoutes(new Map([[LAST_RUN, json({run: RUN})], [SECTIONS, json(['zagruzchiki'])]]));
        renderImport();

        const skipped = within(await screen.findByRole('region', {name: 'Left out'}));

        expect(skipped.getByRole('link', {name: '512 demo'})).toHaveAttribute('href', DEMO);
        expect(skipped.getByText('The section utility-dlya-pc is not imported')).toBeInTheDocument();
        expect(skipped.getByRole('link', {name: 'Zone X'})).toHaveAttribute('href', ZONE_X);
        expect(skipped.getByText('The category pc is not imported')).toBeInTheDocument();
    });

    it('lists the authors it created and those it found in the archive, each leading to its page', async () => {
        stubFetchRoutes(new Map([[LAST_RUN, json({run: RUN})], [SECTIONS, json(['zagruzchiki'])]]));
        renderImport();

        const created = within(await screen.findByRole('region', {name: 'Authors created'}));
        const found = within(screen.getByRole('region', {name: 'Authors found in the archive'}));

        expect(created.getByRole('link', {name: 'Графов А.'})).toHaveAttribute('href', `/labels/${GRAFOV}`);
        expect(created.getByRole('link', {name: 'IVAN'})).toHaveAttribute('href', `/authors/${IVAN}`);
        expect(created.getByText('Label')).toBeInTheDocument();
        expect(found.getByRole('link', {name: 'Crew'})).toHaveAttribute('href', `/groups/${CREW}`);
        expect(found.getByText('Group')).toBeInTheDocument();
    });

    it('lists the cities and countries it added', async () => {
        stubFetchRoutes(new Map([[LAST_RUN, json({run: RUN})], [SECTIONS, json(['zagruzchiki'])]]));
        renderImport();

        const places = within(await screen.findByRole('region', {name: 'Places added'}));

        expect(places.getAllByRole('listitem').map((item) => item.textContent)).toStrictEqual(['City Ковров', 'Country Атлантида']);
    });
});
