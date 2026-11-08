import {afterEach, describe, expect, it, vi} from 'vitest';
import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {toReleaseDate, toSoftwareId, toSoftwareTitle} from '../../../entities/software';
import {json, noContent, stubFetch} from '../../../shared/lib/test';
import {DeleteSoftwareButton} from './DeleteSoftwareButton';

const SOFTWARE = {
    id: toSoftwareId('00000000-0000-0000-0001-000000000001'),
    title: toSoftwareTitle('Elite'),
    description: null,
    releaseDate: toReleaseDate('1985-09-20'),
};

describe(DeleteSoftwareButton, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('deletes the software after confirmation', async () => {
        const fetchMock = stubFetch(noContent());
        const onDeleted = vi.fn<() => void>();
        render(<DeleteSoftwareButton software={SOFTWARE} onDeleted={onDeleted}/>);

        await userEvent.click(screen.getByRole('button', {name: 'Delete'}));

        expect(screen.getByRole('alertdialog', {name: 'Delete software'})).toHaveTextContent('Delete “Elite” from the archive?');

        await userEvent.click(screen.getByRole('button', {name: 'Delete for good'}));

        await vi.waitFor(() => {
            expect(onDeleted).toHaveBeenCalledExactlyOnceWith();
        });

        expect(fetchMock.mock.calls[0]?.[1]).toMatchObject({method: 'DELETE'});
    });

    it('deletes nothing when cancelled', async () => {
        const fetchMock = stubFetch(noContent());
        render(<DeleteSoftwareButton software={SOFTWARE} onDeleted={vi.fn<() => void>()}/>);

        await userEvent.click(screen.getByRole('button', {name: 'Delete'}));
        await userEvent.click(screen.getByRole('button', {name: 'Cancel'}));

        expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument();
        expect(fetchMock).not.toHaveBeenCalled();
    });

    it('says so in the dialog when the software is already gone', async () => {
        stubFetch(json({status: 404, code: 'software.not-found'}, 404));
        const onDeleted = vi.fn<() => void>();
        render(<DeleteSoftwareButton software={SOFTWARE} onDeleted={onDeleted}/>);

        await userEvent.click(screen.getByRole('button', {name: 'Delete'}));
        await userEvent.click(screen.getByRole('button', {name: 'Delete for good'}));

        await expect(screen.findByRole('alert')).resolves.toHaveTextContent('This software has already been deleted.');
        expect(onDeleted).not.toHaveBeenCalled();
    });
});
