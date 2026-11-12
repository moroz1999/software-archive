import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import {MemoryRouter} from 'react-router';
import {PlatformSelectionProvider} from '@/entities/platforms';
import {json, pageResponse, renderWithQueries, stubFetch} from '@/shared/lib/test';
import {LatestSoftwareProvider} from '../model/LatestSoftwareProvider';
import {LatestSoftware} from './LatestSoftware';

vi.unmock('@/shared/lib/loading/skeleton-timing');

const ELITE = {id: '00000000-0000-0000-0001-000000000001', title: 'Elite', releaseDate: '1984-09-20'};

describe(LatestSoftware, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('keeps its skeleton for half a second once shown, even when the software arrives at once', async () => {
        const fetchMock = stubFetch(json(pageResponse([ELITE], {size: 12})));
        renderWithQueries(
            <MemoryRouter><PlatformSelectionProvider><LatestSoftwareProvider><LatestSoftware/></LatestSoftwareProvider></PlatformSelectionProvider></MemoryRouter>,
        );
        await vi.waitFor(() => {
            expect(fetchMock).toHaveBeenCalledWith('/api/software/latest?size=12', expect.anything());
        });

        await new Promise((resolve) => {
            setTimeout(resolve, 200);
        });

        expect(screen.getByTestId('latest-software-skeleton')).toBeInTheDocument();
        await expect(screen.findByRole('article', {name: 'Elite'})).resolves.toBeInTheDocument();
    });
});
