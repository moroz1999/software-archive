import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import {MemoryRouter} from 'react-router';
import {json, renderWithQueries, stubFetchRoutes} from '@/shared/lib/test';
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
    sources: [],
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

    it('tells only when the release was added once the account of whoever added it is gone', async () => {
        renderView({...AY_MOD, added: {by: null, at: '2026-03-04T05:06:07Z'}});

        await expect(screen.findByText('Added on 4 March 2026')).resolves.toBeInTheDocument();
    });
});
