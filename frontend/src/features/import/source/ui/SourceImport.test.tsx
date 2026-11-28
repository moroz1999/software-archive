import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen, waitFor, within} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {MemoryRouter} from 'react-router';
import {subscribeSystemErrors, type SystemErrorReport} from '@/shared/api';
import {json, renderWithQueries, stubFetchRoutes} from '@/shared/lib/test';
import {SourceImport} from './SourceImport';

const SECTIONS = '/api/imports/vektor/sections';
const START = '/api/imports/vektor';
const LAST_RUN = '/api/imports/last-run';
const NO_RUN = json({run: null});
const SOFTWARE = '00000000-0000-0000-0001-000000000001';
const G_DOS = 'https://vektor-06c.ru/programmnoe-obespechenie/zagruzchiki/g-dos/';
const BASIS_START = '/api/imports/basis';
const BASIS_CATEGORIES = '/api/imports/basis/categories';
const MICRODOS = '00000000-0000-0000-0001-000000000003';
const CPM = '00000000-0000-0000-0001-000000000004';
const DEMO = 'https://vektor-06c.ru/programmnoe-obespechenie/utility-dlya-pc/512-demo/';
const ZONE_X = 'https://caglrc.cc/scalar/ware/30/';
const LIST_PAGE = 'https://vektor-06c.ru/programmnoe-obespechenie/zagruzchiki/';
const GRAFOV = '00000000-0000-0000-0004-000000000001';
const IVAN = '00000000-0000-0000-0003-000000000001';
const CREW = '00000000-0000-0000-0005-000000000001';
const AT = '2026-11-14T12:01:00Z';
const BK_CATALOG_START = '/api/imports/bk-catalog';
const DIGGER = 'https://kalininskiy.github.io/bk-catalog/#game-555893';
const BK_TURBO = 'https://kalininskiy.github.io/bk-catalog/#software-2026321';
const LOG = [
    {seq: 1, at: AT, kind: 'list', entry: {url: LIST_PAGE, number: 1, pages: 24, programs: 10}},
    {seq: 2, at: AT, kind: 'download', entry: {externalId: '599', title: 'Disk Loader', file: 'archive', name: 'disk.zip', size: 2048}},
    {seq: 3, at: AT, kind: 'download', entry: {externalId: '599', title: 'Disk Loader', file: 'screenshot', name: 'disk.png', size: 5120}},
    {seq: 4, at: AT, kind: 'added', entry: {externalId: '599', softwareId: SOFTWARE, title: 'Disk Loader', exists: true}},
    {seq: 5, at: AT, kind: 'author', entry: {kind: 'label', id: GRAFOV, name: 'Графов А.', created: true, exists: true}},
    {seq: 6, at: AT, kind: 'author', entry: {kind: 'author', id: IVAN, name: 'IVAN', created: true, exists: true}},
    {seq: 7, at: AT, kind: 'place', entry: {kind: 'city', name: 'Ковров'}},
    {seq: 8, at: AT, kind: 'added', entry: {externalId: '595', softwareId: '00000000-0000-0000-0001-000000000002', title: 'Masterpiece Boot', exists: false}},
    {seq: 9, at: AT, kind: 'known', entry: {externalId: '754', softwareId: MICRODOS, title: 'МикроДОС', exists: true, supplements: ['description', 'categories']}},
    {seq: 10, at: AT, kind: 'author', entry: {kind: 'group', id: CREW, name: 'Crew', created: false, exists: true}},
    {seq: 11, at: AT, kind: 'known', entry: {externalId: '753', softwareId: CPM, title: 'CP/M', exists: true, supplements: []}},
    {seq: 12, at: AT, kind: 'skipped', entry: {externalId: '524', url: DEMO, title: '512 demo', reason: 'section', detail: 'utility-dlya-pc'}},
    {seq: 13, at: AT, kind: 'skipped', entry: {externalId: '30', url: ZONE_X, title: 'Zone X', reason: 'category', detail: 'pc'}},
    {seq: 14, at: AT, kind: 'failed', entry: {externalId: '598', url: G_DOS, reason: 'Timed out'}},
    {seq: 15, at: null, kind: 'place', entry: {kind: 'country', name: 'Атлантида'}},
];
const unsubscribers: (() => void)[] = [];
const RUN = {
    id: '00000000-0000-0000-0000-00000000000a',
    archive: 'vektor-06c.ru',
    section: 'zagruzchiki',
    state: 'running',
    startedAt: '2026-11-14T12:00:00Z',
    endedAt: null,
    added: 2,
    known: 2,
    skipped: 2,
    failed: 1,
    warnings: 0,
    progress: {position: 37, total: 512, url: G_DOS},
    log: LOG,
};

/** The lines of the log of the last import, as a visitor reads them. */
async function logLines(): Promise<string[]> {
    const log = await screen.findByRole('list', {name: 'Log'});
    return within(log).getAllByRole('listitem').map((item) => item.textContent);
}

function renderImport() {
    renderWithQueries(<MemoryRouter><SourceImport/></MemoryRouter>);
}

function accepted(): () => Response {
    return () => new Response(null, {status: 202});
}

describe(SourceImport, () => {
    afterEach(() => {
        unsubscribers.splice(0).forEach((unsubscribe) => {
            unsubscribe();
        });
        vi.unstubAllGlobals();
    });

    it('offers vektor-06c.ru and the card index Basis as the sources, vektor-06c.ru first', async () => {
        stubFetchRoutes(new Map([[LAST_RUN, NO_RUN], [SECTIONS, json(['zagruzchiki'])]]));
        renderImport();

        const source = await screen.findByRole('combobox', {name: 'Source'});

        expect(source).toHaveDisplayValue('vektor-06c.ru');
        expect(within(source).getAllByRole('option').map((option) => option.textContent)).toStrictEqual(['vektor-06c.ru', 'caglrc.cc (Basis)', 'bk-catalog']);
    });

    it('offers the whole card index and every category of Basis', async () => {
        stubFetchRoutes(new Map([[LAST_RUN, NO_RUN], [SECTIONS, json(['zagruzchiki'])], [BASIS_CATEGORIES, json(['arcade', 'demo'])]]));
        renderImport();

        await screen.findByRole('option', {name: 'zagruzchiki'});
        await userEvent.selectOptions(screen.getByRole('combobox', {name: 'Source'}), 'caglrc.cc (Basis)');

        const category = await screen.findByRole('combobox', {name: 'Category'});

        expect(within(category).getAllByRole('option').map((option) => option.textContent)).toStrictEqual(['Whole card index', 'arcade', 'demo']);
    });

    it('starts the import of the chosen category of Basis', async () => {
        const fetchMock = stubFetchRoutes(new Map([
            [LAST_RUN, NO_RUN],
            [SECTIONS, json(['zagruzchiki'])],
            [BASIS_CATEGORIES, json(['arcade', 'demo'])],
            [`${BASIS_START}?category=arcade`, accepted()],
        ]));
        renderImport();

        await screen.findByRole('option', {name: 'zagruzchiki'});
        await userEvent.selectOptions(screen.getByRole('combobox', {name: 'Source'}), 'caglrc.cc (Basis)');
        await userEvent.selectOptions(await screen.findByRole('combobox', {name: 'Category'}), 'arcade');

        await userEvent.click(screen.getByRole('button', {name: 'Start'}));

        await expect(screen.findByRole('status')).resolves.toHaveTextContent('The import has started.');
        expect(fetchMock).toHaveBeenCalledWith(`${BASIS_START}?category=arcade`, expect.objectContaining({method: 'POST'}));
    });

    it('starts the import of the whole card index of Basis when no category is chosen', async () => {
        const fetchMock = stubFetchRoutes(new Map([
            [LAST_RUN, NO_RUN],
            [SECTIONS, json(['zagruzchiki'])],
            [BASIS_CATEGORIES, json(['arcade'])],
            [BASIS_START, accepted()],
        ]));
        renderImport();

        await screen.findByRole('option', {name: 'zagruzchiki'});
        await userEvent.selectOptions(screen.getByRole('combobox', {name: 'Source'}), 'caglrc.cc (Basis)');
        await screen.findByRole('combobox', {name: 'Category'});

        await userEvent.click(screen.getByRole('button', {name: 'Start'}));

        await expect(screen.findByRole('status')).resolves.toHaveTextContent('The import has started.');
        expect(fetchMock).toHaveBeenCalledWith(BASIS_START, expect.objectContaining({method: 'POST'}));
    });

    it('hands a failure to load the Basis categories to the system error dialog', async () => {
        const reports: SystemErrorReport[] = [];
        unsubscribers.push(subscribeSystemErrors((report) => {
            reports.push(report);
        }));
        stubFetchRoutes(new Map([
            [LAST_RUN, NO_RUN],
            [SECTIONS, json(['zagruzchiki'])],
            [BASIS_CATEGORIES, json({status: 500}, 500)],
        ]));
        renderImport();

        await screen.findByRole('option', {name: 'zagruzchiki'});
        await userEvent.selectOptions(screen.getByRole('combobox', {name: 'Source'}), 'caglrc.cc (Basis)');

        await waitFor(() => {
            expect(reports).toHaveLength(1);
        });

        expect(reports[0]?.error.kind).toBe('server');
    });

    it('offers the whole of bk-catalog and each of its sections by name, without asking the archive', async () => {
        stubFetchRoutes(new Map([[LAST_RUN, NO_RUN], [SECTIONS, json(['zagruzchiki'])]]));
        renderImport();

        await screen.findByRole('option', {name: 'zagruzchiki'});
        await userEvent.selectOptions(screen.getByRole('combobox', {name: 'Source'}), 'bk-catalog');

        const section = screen.getByRole('combobox', {name: 'Section'});

        expect(within(section).getAllByRole('option').map((option) => option.textContent)).toStrictEqual(['Whole catalog', 'Games', 'Programs', 'Demoscene']);
        expect(section).toBeEnabled();
    });

    it('starts the import of the chosen section of bk-catalog', async () => {
        const fetchMock = stubFetchRoutes(new Map([
            [LAST_RUN, NO_RUN],
            [SECTIONS, json(['zagruzchiki'])],
            [`${BK_CATALOG_START}?section=demoscene`, accepted()],
        ]));
        renderImport();

        await screen.findByRole('option', {name: 'zagruzchiki'});
        await userEvent.selectOptions(screen.getByRole('combobox', {name: 'Source'}), 'bk-catalog');
        await userEvent.selectOptions(screen.getByRole('combobox', {name: 'Section'}), 'Demoscene');
        await userEvent.click(screen.getByRole('button', {name: 'Start'}));

        await expect(screen.findByRole('status')).resolves.toHaveTextContent('The import has started.');
        expect(fetchMock).toHaveBeenCalledWith(`${BK_CATALOG_START}?section=demoscene`, expect.objectContaining({method: 'POST'}));
    });

    it('starts the import of the whole of bk-catalog when no section is chosen', async () => {
        const fetchMock = stubFetchRoutes(new Map([[LAST_RUN, NO_RUN], [SECTIONS, json(['zagruzchiki'])], [BK_CATALOG_START, accepted()]]));
        renderImport();

        await screen.findByRole('option', {name: 'zagruzchiki'});
        await userEvent.selectOptions(screen.getByRole('combobox', {name: 'Source'}), 'bk-catalog');
        await userEvent.click(screen.getByRole('button', {name: 'Start'}));

        await screen.findByRole('status');

        expect(fetchMock).toHaveBeenCalledWith(BK_CATALOG_START, expect.objectContaining({method: 'POST'}));
    });

    it('names the section of bk-catalog the last import read, or the whole catalog', async () => {
        stubFetchRoutes(new Map([[LAST_RUN, json({run: {...RUN, archive: 'bk-catalog', section: 'games'}})], [SECTIONS, json(['zagruzchiki'])]]));
        renderImport();

        const report = within(await screen.findByRole('region', {name: 'Last import'}));

        await expect(report.findByText('bk-catalog · Games')).resolves.toBeInTheDocument();
    });

    it('names the whole of bk-catalog when the last import read it all', async () => {
        stubFetchRoutes(new Map([[LAST_RUN, json({run: {...RUN, archive: 'bk-catalog', section: null}})], [SECTIONS, json(['zagruzchiki'])]]));
        renderImport();

        const report = within(await screen.findByRole('region', {name: 'Last import'}));

        await expect(report.findByText('bk-catalog · Whole catalog')).resolves.toBeInTheDocument();
    });

    it('counts the warnings of the last import apart, so that they are not missed among its lines', async () => {
        stubFetchRoutes(new Map([[LAST_RUN, json({run: {...RUN, warnings: 3}})], [SECTIONS, json(['zagruzchiki'])]]));
        renderImport();

        const report = within(await screen.findByRole('region', {name: 'Last import'}));

        await expect(report.findByText('Added 2 · already there 2 · left out 2 · failed 1')).resolves.toBeInTheDocument();
        expect(report.getByText('Warnings: 3')).toBeInTheDocument();
    });

    it('tells no warnings of an import that had none', async () => {
        stubFetchRoutes(new Map([[LAST_RUN, json({run: RUN})], [SECTIONS, json(['zagruzchiki'])]]));
        renderImport();

        const report = within(await screen.findByRole('region', {name: 'Last import'}));
        await report.findByText('Running');

        expect(report.queryByText(/^Warnings/u)).not.toBeInTheDocument();
    });

    it('marks every warning in the log, telling what could not be taken and leading to the entry in the source', async () => {
        const warning = (seq: number, kind: string, value: string) => ({
            seq, at: AT, kind: 'warning', entry: {externalId: 'game-555893', url: DIGGER, title: 'Digger', kind, value},
        });
        stubFetchRoutes(new Map([
            [LAST_RUN, json({run: {...RUN, archive: 'bk-catalog', section: null, warnings: 7, log: [
                warning(1, 'platform', 'БК0012'),
                warning(2, 'genre', 'Симулятор'),
                warning(3, 'date', 'весна 1991'),
                warning(4, 'lineage', 'Конверсия'),
                warning(5, 'lineage-source-too-long', 'Elite…'),
                warning(6, 'file-missing', 'lost.zip'),
                warning(7, 'screenshot-missing', 'lost.png'),
            ]}})],
            [SECTIONS, json(['zagruzchiki'])],
        ]));
        renderImport();

        const lines = await logLines();

        expect(lines.map((line) => line.replace(/^\d{2}:\d{2}:\d{2}/u, ''))).toStrictEqual([
            'WarningDigger: the platform БК0012 is unknown, the program is taken without it',
            'WarningDigger: the genre Симулятор is unknown, the program is taken without it',
            'WarningDigger: the date весна 1991 cannot be read, the program is taken without it',
            'WarningDigger: the lineage Конверсия is unknown, the program is taken without it',
            'WarningDigger: the original is longer than 255 characters, the program is taken without it',
            'WarningDigger: the file lost.zip is not in the source, its release is taken without it',
            'WarningDigger: the screenshot lost.png is not in the source',
        ]);
        expect(within(screen.getByRole('list', {name: 'Log'})).getAllByRole('link', {name: 'Digger'}).at(0)).toHaveAttribute('href', DIGGER);
    });

    it('tells a program left out by its platform and the lineage added to a known one', async () => {
        stubFetchRoutes(new Map([
            [LAST_RUN, json({run: {...RUN, archive: 'bk-catalog', section: null, log: [
                {seq: 1, at: AT, kind: 'skipped', entry: {externalId: 'software-2026321', url: BK_TURBO, title: 'BK Turbo 8', reason: 'platform', detail: 'Windows'}},
                {seq: 2, at: AT, kind: 'known', entry: {externalId: 'game-555893', softwareId: MICRODOS, title: 'Digger', exists: true, supplements: ['lineage']}},
            ]}})],
            [SECTIONS, json(['zagruzchiki'])],
        ]));
        renderImport();

        const lines = await logLines();

        expect(lines.map((line) => line.replace(/^\d{2}:\d{2}:\d{2}/u, ''))).toStrictEqual([
            'BK Turbo 8: left out, the platform Windows is not imported',
            'Digger: already there, added lineage',
        ]);
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
        expect(report.getByText('Added 2 · already there 2 · left out 2 · failed 1')).toBeInTheDocument();
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

    it('tells every step of the import line by line in the order it went', async () => {
        stubFetchRoutes(new Map([[LAST_RUN, json({run: RUN})], [SECTIONS, json(['zagruzchiki'])]]));
        renderImport();

        const lines = await logLines();

        expect(lines.map((line) => line.replace(/^\d{2}:\d{2}:\d{2}/u, ''))).toStrictEqual([
            'Read the list: page 1 of 24, 10 programs',
            'Disk Loader: downloaded the archive disk.zip (2 KB)',
            'Disk Loader: downloaded the screenshot disk.png (5 KB)',
            'Disk Loader: added',
            'Created the label Графов А.',
            'Created the person IVAN',
            'Added the city Ковров',
            'Masterpiece Boot (deleted since): added',
            'МикроДОС: already there, added description, categories',
            'Found the group Crew in the archive',
            'CP/M: already there, nothing to add',
            '512 demo: left out, the section utility-dlya-pc is not imported',
            'Zone X: left out, the category pc is not imported',
            `${G_DOS}: failed: Timed out`,
            'Added the country Атлантида',
        ]);
    });

    it('tells when each step was done, and no time for a line carried over from before the log', async () => {
        stubFetchRoutes(new Map([[LAST_RUN, json({run: RUN})], [SECTIONS, json(['zagruzchiki'])]]));
        renderImport();

        const lines = await logLines();

        expect(lines[0]).toMatch(/^\d{2}:\d{2}:\d{2}Read the list/u);
        expect(lines.at(-1)).toBe('Added the country Атлантида');
    });

    it('leads from the log to the software and the pages of the source', async () => {
        stubFetchRoutes(new Map([[LAST_RUN, json({run: RUN})], [SECTIONS, json(['zagruzchiki'])]]));
        renderImport();

        const log = within(await screen.findByRole('list', {name: 'Log'}));

        expect(log.getByRole('link', {name: 'page 1 of 24'})).toHaveAttribute('href', LIST_PAGE);
        expect(log.getAllByRole('link', {name: 'Disk Loader'}).at(-1)).toHaveAttribute('href', `/software/${SOFTWARE}`);
        expect(log.queryByRole('link', {name: 'Masterpiece Boot'})).not.toBeInTheDocument();
        expect(log.getByRole('link', {name: 'МикроДОС'})).toHaveAttribute('href', `/software/${MICRODOS}`);
        expect(log.getByRole('link', {name: '512 demo'})).toHaveAttribute('href', DEMO);
    });

    it('leads from the log to the pages of the programs that failed and to the authors', async () => {
        stubFetchRoutes(new Map([[LAST_RUN, json({run: RUN})], [SECTIONS, json(['zagruzchiki'])]]));
        renderImport();

        const log = within(await screen.findByRole('list', {name: 'Log'}));

        expect(log.getByRole('link', {name: G_DOS})).toHaveAttribute('href', G_DOS);
        expect(log.getByRole('link', {name: 'Графов А.'})).toHaveAttribute('href', `/labels/${GRAFOV}`);
        expect(log.getByRole('link', {name: 'IVAN'})).toHaveAttribute('href', `/authors/${IVAN}`);
        expect(log.getByRole('link', {name: 'Crew'})).toHaveAttribute('href', `/groups/${CREW}`);
    });

    it('reads only the new lines of the log while the import goes on and adds them after those it has', async () => {
        vi.useFakeTimers({shouldAdvanceTime: true});
        const next = {seq: 16, at: AT, kind: 'list', entry: {url: `${LIST_PAGE}?PAGEN_1=2`, number: 2, pages: 24, programs: 10}};
        const fetchMock = stubFetchRoutes(new Map([
            [LAST_RUN, json({run: RUN})],
            [`${LAST_RUN}?after=15`, json({run: {...RUN, log: [next]}})],
            [SECTIONS, json(['zagruzchiki'])],
        ]));
        renderImport();
        await logLines();

        await vi.advanceTimersByTimeAsync(5000);

        await waitFor(async () => {
            await expect(logLines()).resolves.toHaveLength(LOG.length + 1);
        });

        expect((await logLines()).at(-1)).toMatch(/Read the list: page 2 of 24, 10 programs$/u);
        expect(fetchMock).toHaveBeenCalledWith(`${LAST_RUN}?after=15`, expect.anything());

        vi.useRealTimers();
    });
});
