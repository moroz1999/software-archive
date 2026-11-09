import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen, within} from '@testing-library/react';
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
};
const ORIGINAL = {...AY_MOD, id: '00000000-0000-0000-0007-000000000002', title: 'Elite', version: null, type: 'original', releaseDate: null, description: null};

function renderReleases(enabled = true) {
    renderWithQueries(<SoftwareReleasesProvider id={ID} enabled={enabled}><SoftwareReleases/></SoftwareReleasesProvider>);
}

describe(SoftwareReleases, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('shows every release with its version, type, day, description and hardware', async () => {
        stubFetch(json([AY_MOD, ORIGINAL]));
        renderReleases();

        const release = await screen.findByRole('article', {name: 'Elite AY'});

        expect(release).toHaveTextContent('1.1');
        expect(release).toHaveTextContent('Mod');
        expect(release).toHaveTextContent('1 March 1990');
        expect(release).toHaveTextContent('With AY music');
        expect(within(within(release).getByRole('group', {name: 'Sound'})).getByText('AY-3-8910')).toBeInTheDocument();
    });

    it('leaves out what is unknown about a release', async () => {
        stubFetch(json([ORIGINAL]));
        renderReleases();

        const release = await screen.findByRole('article', {name: 'Elite'});

        expect(release).toHaveTextContent('Original');
        expect(release).not.toHaveTextContent('Version');
    });

    it('says so when the software has no releases', async () => {
        stubFetch(json([]));
        renderReleases();

        await expect(screen.findByText('No releases known yet.')).resolves.toBeInTheDocument();
    });

    it('asks for nothing until the block is in view', () => {
        const fetchMock = stubFetch(json([AY_MOD]));
        renderReleases(false);

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
