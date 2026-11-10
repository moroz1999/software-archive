import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {toReleaseId, toSoftwareId, toSoftwarePlatformCode, toSoftwareTitle} from '@/entities/software';
import {json, noContent, renderWithQueries, stubFetch} from '@/shared/lib/test';
import {DeleteReleaseButton} from './DeleteReleaseButton';

const SOFTWARE = {
    id: toSoftwareId('00000000-0000-0000-0001-000000000001'),
    title: toSoftwareTitle('Elite'),
    description: null,
    releaseDate: null,
    platform: {code: toSoftwarePlatformCode('vector'), name: 'Vector'},
    hardware: [],
};
const RELEASE = {
    id: toReleaseId('00000000-0000-0000-0007-000000000002'),
    title: 'Elite AY',
    version: '1.1',
    type: 'mod' as const,
    releaseDate: null,
    description: null,
    hardware: [],
};

describe(DeleteReleaseButton, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('deletes the release after confirmation', async () => {
        const fetchMock = stubFetch(noContent());
        const onDeleted = vi.fn<() => void>();
        renderWithQueries(<DeleteReleaseButton software={SOFTWARE} release={RELEASE} onDeleted={onDeleted}/>);

        await userEvent.click(screen.getByRole('button', {name: 'Delete'}));

        expect(screen.getByRole('alertdialog', {name: 'Delete release'})).toHaveTextContent('Delete the release “Elite AY” 1.1 from the archive?');

        await userEvent.click(screen.getByRole('button', {name: 'Delete for good'}));

        await vi.waitFor(() => {
            expect(onDeleted).toHaveBeenCalledExactlyOnceWith();
        });

        expect(fetchMock).toHaveBeenCalledExactlyOnceWith(`/api/software/${SOFTWARE.id}/releases/${RELEASE.id}`, expect.objectContaining({method: 'DELETE'}));
    });

    it('names a release without a title of its own by its software', async () => {
        renderWithQueries(<DeleteReleaseButton software={SOFTWARE} release={{...RELEASE, title: null, version: null}} onDeleted={vi.fn<() => void>()}/>);

        await userEvent.click(screen.getByRole('button', {name: 'Delete'}));

        expect(screen.getByRole('alertdialog', {name: 'Delete release'})).toHaveTextContent('Delete the release “Elite” from the archive?');
    });

    it('deletes nothing when cancelled', async () => {
        const fetchMock = stubFetch(noContent());
        renderWithQueries(<DeleteReleaseButton software={SOFTWARE} release={RELEASE} onDeleted={vi.fn<() => void>()}/>);

        await userEvent.click(screen.getByRole('button', {name: 'Delete'}));
        await userEvent.click(screen.getByRole('button', {name: 'Cancel'}));

        expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument();
        expect(fetchMock).not.toHaveBeenCalled();
    });

    it('shows a refusal inside the confirmation', async () => {
        stubFetch(json({status: 422, code: 'release.refused'}, 422));
        renderWithQueries(<DeleteReleaseButton software={SOFTWARE} release={RELEASE} onDeleted={vi.fn<() => void>()}/>);

        await userEvent.click(screen.getByRole('button', {name: 'Delete'}));
        await userEvent.click(screen.getByRole('button', {name: 'Delete for good'}));

        await expect(screen.findByText('The request was refused.')).resolves.toBeInTheDocument();
    });
});
