import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen, within} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {MemoryRouter} from 'react-router';
import {json, renderWithQueries, stubFetchRoutes} from '@/shared/lib/test';
import {VektorImport} from './VektorImport';

const SECTIONS = '/api/imports/vektor/sections';
const START = '/api/imports/vektor';
const LAST_RUN = '/api/imports/last-run';
const NO_RUN = json({run: null});
const SOFTWARE = '00000000-0000-0000-0001-000000000001';
const G_DOS = 'https://vektor-06c.ru/programmnoe-obespechenie/zagruzchiki/g-dos/';
const RUN = {
    id: '00000000-0000-0000-0000-00000000000a',
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
};

function renderImport() {
    renderWithQueries(<MemoryRouter><VektorImport/></MemoryRouter>);
}

function accepted(): () => Response {
    return () => new Response(null, {status: 202});
}

describe(VektorImport, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
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

        await expect(report.findByText('zagruzchiki')).resolves.toBeInTheDocument();
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
});
