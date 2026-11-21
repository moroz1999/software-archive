import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen, within} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {MemoryRouter} from 'react-router';
import {subscribeSystemErrors, type SystemErrorReport} from '@/shared/api';
import {breadcrumbSteps, json, renderWithQueries, stubFetchRoutes} from '@/shared/lib/test';
import {toReleaseId, toSoftwareId} from '@/entities/software';
import {ReleaseView} from './ReleaseView';

const ID = '00000000-0000-0000-0001-000000000001';
const RELEASE = '00000000-0000-0000-0007-000000000001';
const ELITE = {
    id: ID,
    title: 'Elite',
    description: null,
    releaseDate: null,
    platform: {code: 'vector', name: 'Vector'},
    hardware: [],
    categories: [],
    credits: [],
    sources: [], preservation: 'unknown',
    added: {by: 'Admin', at: '2026-01-01T00:00:00Z'},
    canChange: false,
};
const AY_MOD = {
    id: RELEASE,
    title: 'Elite AY',
    version: null,
    type: 'mod',
    releaseDate: null,
    description: null,
    hardware: [],
    screenshots: [],
    archive: null,
    added: {by: 'Zork', at: '2026-03-04T05:06:07Z'},
    canChange: false,
};

function renderView(release: object) {
    stubFetchRoutes(new Map([
        [`/api/software/${ID}`, json(ELITE)],
        [`/api/software/${ID}/releases`, json([release])],
        [`/api/software/${ID}/releases/${RELEASE}/screenshots`, json([])],
    ]));
    renderWithQueries(<MemoryRouter><ReleaseView softwareId={toSoftwareId(ID)} releaseId={toReleaseId(RELEASE)}/></MemoryRouter>);
}

describe(ReleaseView, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('tells who added the release and when', async () => {
        renderView(AY_MOD);

        await expect(screen.findByText('Added by Zork on 4 March 2026')).resolves.toBeInTheDocument();
    });

    it('names the browser tab after the release and the platform of its software', async () => {
        renderView(AY_MOD);

        await expect(screen.findByRole('heading', {level: 1, name: 'Elite AY'})).resolves.toBeInTheDocument();
        expect(document.title).toBe('Elite AY (Vector) — Ретротека');
    });

    it('leads back from the release to its software and on through the catalog', async () => {
        renderView(AY_MOD);

        await screen.findByRole('heading', {level: 1, name: 'Elite AY'});

        expect(breadcrumbSteps()).toStrictEqual([['Software', '/software'], ['Elite', `/software/${ID}`], ['Elite AY', null]]);
    });

    it('tells only when the release was added once the account of whoever added it is gone', async () => {
        renderView({...AY_MOD, added: {by: null, at: '2026-03-04T05:06:07Z'}});

        await expect(screen.findByText('Added on 4 March 2026')).resolves.toBeInTheDocument();
    });

    it('shows the parsed release file as an indented tree and marks executables', async () => {
        stubFetchRoutes(new Map([
            [`/api/software/${ID}`, json(ELITE)],
            [`/api/software/${ID}/releases`, json([{
                ...AY_MOD,
                archive: {fileName: 'bundle.zip', size: 42, url: `/api/software/${ID}/releases/${RELEASE}/archive`},
            }])],
            [`/api/software/${ID}/releases/${RELEASE}/files`, json({root: {
                id: '00000000-0000-0000-00f2-000000000001',
                name: 'bundle.zip',
                directory: false,
                executable: false,
                unreadable: false,
                children: [{
                    id: '00000000-0000-0000-00f2-000000000002',
                    name: 'games',
                    directory: true,
                    executable: false,
                    unreadable: false,
                    children: [{id: '00000000-0000-0000-00f2-000000000003', name: 'ELITE.ROM', directory: false, executable: true, unreadable: false, children: []}],
                }],
            }})],
            [`/api/software/${ID}/releases/${RELEASE}/screenshots`, json([])],
        ]));
        renderWithQueries(<MemoryRouter><ReleaseView softwareId={toSoftwareId(ID)} releaseId={toReleaseId(RELEASE)}/></MemoryRouter>);

        const tree = await screen.findByRole('list', {name: 'File structure'});

        expect(within(tree).getByText('bundle.zip')).toBeInTheDocument();
        expect(within(tree).getByText('games')).toBeInTheDocument();
        expect(within(tree).queryByText('ELITE.ROM')).not.toBeInTheDocument();

        await userEvent.click(within(tree).getByRole('button', {name: 'Expand games'}));

        expect(within(tree).getByText('ELITE.ROM')).toBeInTheDocument();
        expect(within(tree).getByText('Executable')).toBeInTheDocument();
    });

    it('tells that a release file could not be read instead of showing a tree', async () => {
        stubFetchRoutes(new Map([
            [`/api/software/${ID}`, json(ELITE)],
            [`/api/software/${ID}/releases`, json([{
                ...AY_MOD,
                archive: {fileName: 'broken.zip', size: 42, url: `/api/software/${ID}/releases/${RELEASE}/archive`},
            }])],
            [`/api/software/${ID}/releases/${RELEASE}/files`, json({root: {
                id: '00000000-0000-0000-00f2-000000000001',
                name: 'broken.zip',
                directory: false,
                executable: false,
                unreadable: true,
                children: [],
            }})],
            [`/api/software/${ID}/releases/${RELEASE}/screenshots`, json([])],
        ]));
        renderWithQueries(<MemoryRouter><ReleaseView softwareId={toSoftwareId(ID)} releaseId={toReleaseId(RELEASE)}/></MemoryRouter>);

        await expect(screen.findByText('The file could not be read')).resolves.toBeInTheDocument();
        expect(screen.getByText('File structure')).toBeInTheDocument();
        expect(screen.queryByRole('list', {name: 'File structure'})).not.toBeInTheDocument();
    });

    it('tells that a release file waits for its structure to be parsed', async () => {
        stubFetchRoutes(new Map([
            [`/api/software/${ID}`, json(ELITE)],
            [`/api/software/${ID}/releases`, json([{
                ...AY_MOD,
                archive: {fileName: 'bundle.zip', size: 42, url: `/api/software/${ID}/releases/${RELEASE}/archive`},
            }])],
            [`/api/software/${ID}/releases/${RELEASE}/files`, json({root: null})],
            [`/api/software/${ID}/releases/${RELEASE}/screenshots`, json([])],
        ]));
        renderWithQueries(<MemoryRouter><ReleaseView softwareId={toSoftwareId(ID)} releaseId={toReleaseId(RELEASE)}/></MemoryRouter>);

        await expect(screen.findByText('Waiting for the structure to be parsed')).resolves.toBeInTheDocument();
        expect(screen.getByText('File structure')).toBeInTheDocument();
        expect(screen.queryByRole('button', {name: 'Parse again'})).not.toBeInTheDocument();
        expect(screen.queryByRole('list', {name: 'File structure'})).not.toBeInTheDocument();
    });

    it('parses the release file again for whoever may change the release and shows the new tree', async () => {
        let parsed = false;
        stubFetchRoutes(new Map([
            [`/api/software/${ID}`, json(ELITE)],
            [`/api/software/${ID}/releases`, json([{
                ...AY_MOD,
                canChange: true,
                archive: {fileName: 'bundle.zip', size: 42, url: `/api/software/${ID}/releases/${RELEASE}/archive`},
            }])],
            [`/api/software/${ID}/releases/${RELEASE}/files`, () => json({root: parsed
                ? {id: '00000000-0000-0000-00f2-000000000001', name: 'bundle.zip', directory: false, executable: false, unreadable: false, children: []}
                : null})()],
            [`/api/software/${ID}/releases/${RELEASE}/files/reparse`, () => {
                parsed = true;
                return new Response(null, {status: 204});
            }],
            [`/api/software/${ID}/releases/${RELEASE}/screenshots`, json([])],
        ]));
        renderWithQueries(<MemoryRouter><ReleaseView softwareId={toSoftwareId(ID)} releaseId={toReleaseId(RELEASE)}/></MemoryRouter>);

        await userEvent.click(await screen.findByRole('button', {name: 'Parse again'}));

        const tree = await screen.findByRole('list', {name: 'File structure'});

        expect(within(tree).getByText('bundle.zip')).toBeInTheDocument();
    });

    it('hands a release file loading error to the error dialog', async () => {
        const reports: SystemErrorReport[] = [];
        const unsubscribe = subscribeSystemErrors((report) => {
            reports.push(report);
        });
        stubFetchRoutes(new Map([
            [`/api/software/${ID}`, json(ELITE)],
            [`/api/software/${ID}/releases`, json([{
                ...AY_MOD,
                archive: {fileName: 'bundle.zip', size: 42, url: `/api/software/${ID}/releases/${RELEASE}/archive`},
            }])],
            [`/api/software/${ID}/releases/${RELEASE}/files`, json({status: 500}, 500)],
            [`/api/software/${ID}/releases/${RELEASE}/screenshots`, json([])],
        ]));

        try {
            renderWithQueries(<MemoryRouter><ReleaseView softwareId={toSoftwareId(ID)} releaseId={toReleaseId(RELEASE)}/></MemoryRouter>);

            await vi.waitFor(() => {
                expect(reports).toHaveLength(1);
            });

            expect(reports[0]?.error.kind).toBe('server');
            expect(screen.queryByRole('list', {name: 'File structure'})).not.toBeInTheDocument();
        } finally {
            unsubscribe();
        }
    });
});
