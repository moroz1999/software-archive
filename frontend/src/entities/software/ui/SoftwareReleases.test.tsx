import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen, within} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {subscribeSystemErrors, type SystemErrorReport} from '@/shared/api';
import {json, renderWithQueries, stubFetch} from '@/shared/lib/test';
import {toSoftwareId} from '../model/software';
import {SoftwareReleasesProvider} from '../model/SoftwareReleasesProvider';
import {SoftwareReleases} from './SoftwareReleases';

const ID = toSoftwareId('00000000-0000-0000-0001-000000000001');
const AY_MOD = {
    id: '00000000-0000-0000-0007-000000000001',
    title: 'Elite AY',
    version: '1.1',
    type: 'mod',
    releaseDate: '1990-03-01',
    description: 'With AY music',
    hardware: [{code: 'vector06c', name: 'Vector-06C', group: 'computers'}, {code: 'ay8910', name: 'AY-3-8910', group: 'sound'}],
    added: {by: 'Zed', at: '2026-01-01T00:00:00Z'},
    canChange: true,
};
const FIX = {...AY_MOD, id: '00000000-0000-0000-0007-000000000002', title: 'Elite Fix', version: null, type: 'bugfix', releaseDate: '1988-05-01', description: null};
const ORIGINAL = {...AY_MOD, id: '00000000-0000-0000-0007-000000000003', title: 'Elite', version: null, type: 'original', releaseDate: null, description: null};
const UNKNOWN = {...ORIGINAL, id: '00000000-0000-0000-0007-000000000004', type: 'unknown'};
const ARCHIVE = {fileName: 'elite-ay.zip', size: 1536, url: '/api/software/00000000-0000-0000-0001-000000000001/releases/00000000-0000-0000-0007-000000000001/archive'};
const SHOTS = [
    {id: '00000000-0000-0000-0055-000000000001', url: '/api/screenshots/00000000-0000-0000-0055-000000000001'},
    {id: '00000000-0000-0000-0055-000000000002', url: '/api/screenshots/00000000-0000-0000-0055-000000000002'},
];

/** The screen of a phone, or of anything wider. */
function stubScreen(phone: boolean) {
    vi.stubGlobal('matchMedia', vi.fn<(query: string) => unknown>(() => ({
        matches: phone,
        addEventListener: vi.fn<() => void>(),
        removeEventListener: vi.fn<() => void>(),
    })));
}

function renderReleases({enabled = true, phone = false} = {}) {
    stubScreen(phone);
    renderWithQueries(
        <SoftwareReleasesProvider id={ID} enabled={enabled}>
            <SoftwareReleases actions={(release) => <span>Edit {release.title}</span>}/>
        </SoftwareReleasesProvider>,
    );
}

/** The titles of the rows of the table, top to bottom. */
function rowTitles(): readonly (string | null)[] {
    return within(screen.getByRole('table', {name: 'Releases'})).getAllByRole('row').slice(1)
        .map((row) => within(row).getByText(/^Elite/).textContent);
}

describe(SoftwareReleases, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('shows the releases as a table of title, year, version, type and hardware', async () => {
        stubFetch(json([AY_MOD, ORIGINAL]));
        renderReleases();

        const table = await screen.findByRole('table', {name: 'Releases'});

        expect(within(table).getAllByRole('columnheader').map((header) => header.textContent).slice(0, 6)).toStrictEqual(['Title', 'Release date', 'Version', 'Type', 'Hardware', 'Archive']);
        expect(within(table).getAllByRole('row')[1]).toHaveTextContent(/Elite AY.*With AY music.*01\.03\.1990.*1\.1.*Mod.*Vector-06C.*AY-3-8910.*Edit Elite AY/);
    });

    it('keeps the order the releases come in until asked to sort', async () => {
        stubFetch(json([AY_MOD, FIX, ORIGINAL]));
        renderReleases();
        await screen.findByRole('table', {name: 'Releases'});

        expect(rowTitles()).toStrictEqual(['Elite AY', 'Elite Fix', 'Elite']);
    });

    it('leaves out a type nobody knows', async () => {
        stubFetch(json([UNKNOWN]));
        renderReleases();

        const table = await screen.findByRole('table', {name: 'Releases'});

        expect(table).not.toHaveTextContent('Unknown');
    });

    it('sorts by title and turns the order round on the second click', async () => {
        stubFetch(json([AY_MOD, FIX, ORIGINAL]));
        renderReleases();

        await userEvent.click(await screen.findByRole('button', {name: 'Title'}));

        expect(rowTitles()).toStrictEqual(['Elite', 'Elite AY', 'Elite Fix']);
        expect(screen.getByRole('columnheader', {name: 'Title'})).toHaveAttribute('aria-sort', 'ascending');

        await userEvent.click(screen.getByRole('button', {name: 'Title'}));

        expect(rowTitles()).toStrictEqual(['Elite Fix', 'Elite AY', 'Elite']);
    });

    it('sorts by year, the newest first and the unknown last', async () => {
        stubFetch(json([ORIGINAL, FIX, AY_MOD]));
        renderReleases();

        await userEvent.click(await screen.findByRole('button', {name: 'Release date'}));

        expect(rowTitles()).toStrictEqual(['Elite AY', 'Elite Fix', 'Elite']);
        expect(screen.getByRole('columnheader', {name: 'Release date'})).toHaveAttribute('aria-sort', 'descending');
    });

    it('shows the releases of one type', async () => {
        stubFetch(json([AY_MOD, FIX, ORIGINAL]));
        renderReleases();

        const types = await screen.findByRole('group', {name: 'Type'});

        expect(within(types).getAllByRole('button').map((button) => button.textContent)).toStrictEqual(['All', 'Mod', 'Bugfix', 'Original']);
        expect(within(types).getByRole('button', {name: 'All'})).toHaveAttribute('aria-pressed', 'true');

        await userEvent.click(within(types).getByRole('button', {name: 'Mod'}));

        expect(rowTitles()).toStrictEqual(['Elite AY']);
    });

    it('offers no type to choose when all releases are of one type', async () => {
        stubFetch(json([AY_MOD, {...FIX, type: 'mod'}]));
        renderReleases();
        await screen.findByRole('table', {name: 'Releases'});

        expect(screen.queryByRole('group', {name: 'Type'})).not.toBeInTheDocument();
    });

    it('shows the releases as cards when asked', async () => {
        stubFetch(json([AY_MOD, ORIGINAL]));
        renderReleases();

        await userEvent.click(await screen.findByRole('button', {name: 'Cards'}));

        const card = screen.getByRole('article', {name: 'Elite AY'}).textContent;

        expect(screen.queryByRole('table')).not.toBeInTheDocument();
        expect(['Mod', '01.03.1990', '1.1', 'With AY music', 'Vector-06C', 'AY-3-8910', 'Edit Elite AY'].filter((fact) => !card.includes(fact))).toStrictEqual([]);
        expect(screen.getByRole('article', {name: 'Elite'})).toBeInTheDocument();
    });

    it('offers the archive of a release for download in its row of the table', async () => {
        stubFetch(json([{...AY_MOD, archive: ARCHIVE}, ORIGINAL]));
        renderReleases();

        const table = await screen.findByRole('table', {name: 'Releases'});
        const row = within(table).getByRole('row', {name: /Elite AY/u});

        expect(within(row).getByRole('link', {name: 'Download Elite AY'})).toHaveAttribute('href', ARCHIVE.url);
        expect(row).toHaveTextContent('1.5 KB');
        expect(within(within(table).getByRole('row', {name: /^Elite(?! AY)/u})).queryByRole('link', {name: /^Download/u})).not.toBeInTheDocument();
    });

    it('offers the archive of a release for download on its card', async () => {
        stubFetch(json([{...AY_MOD, archive: ARCHIVE}, ORIGINAL]));
        renderReleases({phone: true});

        const card = await screen.findByRole('article', {name: 'Elite AY'});

        expect(within(card).getByRole('link', {name: 'Download Elite AY'})).toHaveAttribute('href', ARCHIVE.url);
        expect(card).toHaveTextContent('1.5 KB');
        expect(within(screen.getByRole('article', {name: 'Elite'})).queryByRole('link', {name: /^Download/u})).not.toBeInTheDocument();
    });

    it('shows the first screenshot of a release in its row of the table', async () => {
        stubFetch(json([{...AY_MOD, screenshots: SHOTS}, ORIGINAL]));
        renderReleases();

        const table = await screen.findByRole('table', {name: 'Releases'});

        expect(within(within(table).getByRole('row', {name: /Elite AY/u})).getByRole('img', {name: 'Elite AY'})).toHaveAttribute('src', SHOTS[0]?.url);
        expect(within(within(table).getByRole('row', {name: /^Elite(?! AY)/u})).queryByRole('img')).not.toBeInTheDocument();
    });

    it('shows the screenshots of a release on its card', async () => {
        stubFetch(json([{...AY_MOD, screenshots: SHOTS}, ORIGINAL]));
        renderReleases({phone: true});

        const card = await screen.findByRole('article', {name: 'Elite AY'});

        expect(within(card).getByRole('img', {name: 'Elite AY'})).toHaveAttribute('src', SHOTS[0]?.url);
        expect(within(screen.getByRole('article', {name: 'Elite'})).queryByRole('img')).not.toBeInTheDocument();
    });

    it('shows cards on a phone, with nothing to switch', async () => {
        stubFetch(json([AY_MOD]));
        renderReleases({phone: true});

        await expect(screen.findByRole('article', {name: 'Elite AY'})).resolves.toBeInTheDocument();
        expect(screen.queryByRole('table')).not.toBeInTheDocument();
        expect(screen.queryByRole('group', {name: 'View'})).not.toBeInTheDocument();
    });

    it('holds the place of the table while loading: the same columns under the same bar', () => {
        stubFetch(() => new Response(new ReadableStream()));
        renderReleases();

        const skeleton = screen.getByTestId('software-releases-skeleton');

        expect(within(skeleton).getAllByRole('columnheader', {hidden: true}).map((header) => header.textContent)).toStrictEqual(['Title', 'Release date', 'Version', 'Type', 'Hardware', 'Archive', '']);
        expect(within(skeleton).getAllByRole('row', {hidden: true})).toHaveLength(4);
    });

    it('holds the place of cards while loading on a phone', () => {
        stubFetch(() => new Response(new ReadableStream()));
        renderReleases({phone: true});

        expect(within(screen.getByTestId('software-releases-skeleton')).queryByRole('table', {hidden: true})).not.toBeInTheDocument();
    });

    it('says so when the software has no releases', async () => {
        stubFetch(json([]));
        renderReleases();

        await expect(screen.findByText('No releases known yet.')).resolves.toBeInTheDocument();
    });

    it('asks for nothing until the block is in view', () => {
        const fetchMock = stubFetch(json([AY_MOD]));
        renderReleases({enabled: false});

        expect(fetchMock).not.toHaveBeenCalled();
    });

    it('hands a system error to the error dialog', async () => {
        const reports: SystemErrorReport[] = [];
        const unsubscribe = subscribeSystemErrors((report) => {
            reports.push(report);
        });
        stubFetch(json({status: 500}, 500));
        renderReleases();

        await vi.waitFor(() => {
            expect(reports).toHaveLength(1);
        });

        unsubscribe();
    });
});
