import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {json, noContent, renderWithQueries, stubFetch} from '@/shared/lib/test';
import {ReparseReleaseFilesButton} from './ReparseReleaseFilesButton';

const SOFTWARE = '00000000-0000-0000-0001-000000000001';
const RELEASE = '00000000-0000-0000-0007-000000000002';

describe(ReparseReleaseFilesButton, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('asks to parse the file of the release again', async () => {
        const fetchMock = stubFetch(noContent());
        renderWithQueries(<ReparseReleaseFilesButton softwareId={SOFTWARE} releaseId={RELEASE}/>);

        await userEvent.click(screen.getByRole('button', {name: 'Parse again'}));

        await vi.waitFor(() => {
            expect(fetchMock).toHaveBeenCalledExactlyOnceWith(
                `/api/software/${SOFTWARE}/releases/${RELEASE}/files/reparse`,
                expect.objectContaining({method: 'POST'}),
            );
        });
    });

    it('is busy while the file is being parsed', async () => {
        vi.stubGlobal('fetch', vi.fn<typeof fetch>(() => new Promise<Response>(() => undefined)));
        renderWithQueries(<ReparseReleaseFilesButton softwareId={SOFTWARE} releaseId={RELEASE}/>);

        await userEvent.click(screen.getByRole('button', {name: 'Parse again'}));

        expect(screen.getByRole('button', {name: 'Parse again'})).toHaveAttribute('aria-busy', 'true');
    });

    it('tells why the file may not be parsed again', async () => {
        stubFetch(json({status: 403, code: 'user.change-forbidden'}, 403));
        renderWithQueries(<ReparseReleaseFilesButton softwareId={SOFTWARE} releaseId={RELEASE}/>);

        await userEvent.click(screen.getByRole('button', {name: 'Parse again'}));

        await expect(screen.findByText('Only whoever added this, or an administrator, may change it.')).resolves.toBeInTheDocument();
    });
});
