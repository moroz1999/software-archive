import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import {json, renderWithQueries, stubFetch} from '@/shared/lib/test';
import {toSoftwareId} from '../model/software';
import {SoftwareReleasesProvider} from '../model/SoftwareReleasesProvider';
import {SoftwareReleases} from './SoftwareReleases';

vi.unmock('@/shared/lib/loading/skeleton-timing');

const ID = toSoftwareId('00000000-0000-0000-0001-000000000001');

describe(SoftwareReleases, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('keeps its skeleton for half a second once its call starts, even when the releases arrive at once', async () => {
        const fetchMock = stubFetch(json([]));
        renderWithQueries(<SoftwareReleasesProvider id={ID} enabled><SoftwareReleases/></SoftwareReleasesProvider>);
        await vi.waitFor(() => {
            expect(fetchMock).toHaveBeenCalledWith(`/api/software/${ID}/releases`, expect.anything());
        });

        await new Promise((resolve) => {
            setTimeout(resolve, 200);
        });

        expect(screen.getByTestId('software-releases-skeleton')).toBeInTheDocument();
        await expect(screen.findByText('No releases known yet.')).resolves.toBeInTheDocument();
    });
});
