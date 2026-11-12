import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {toAuthorId} from '@/entities/authors';
import {subscribeSystemErrors, type SystemErrorReport} from '@/shared/api';
import {json, noContent, renderWithQueries, stubFetch} from '@/shared/lib/test';
import {DeleteAuthorButton} from './DeleteAuthorButton';

const AUTHOR = {id: toAuthorId('00000000-0000-0000-0003-000000000001'), name: 'Zed'};

describe(DeleteAuthorButton, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('deletes the author after confirmation, saying their labels stay', async () => {
        const fetchMock = stubFetch(noContent());
        const onDeleted = vi.fn<() => void>();
        renderWithQueries(<DeleteAuthorButton author={AUTHOR} onDeleted={onDeleted}/>);

        await userEvent.click(screen.getByRole('button', {name: 'Delete'}));

        expect(screen.getByRole('alertdialog', {name: 'Delete author'})).toHaveTextContent(
            'Delete “Zed” from the archive? Their labels stay in the archive as labels of nobody, with their credits.',
        );

        await userEvent.click(screen.getByRole('button', {name: 'Delete for good'}));

        await vi.waitFor(() => {
            expect(onDeleted).toHaveBeenCalledExactlyOnceWith();
        });

        expect(fetchMock.mock.calls[0]?.[0]).toBe(`/api/authors/${AUTHOR.id}`);
        expect(fetchMock.mock.calls[0]?.[1]).toMatchObject({method: 'DELETE'});
    });

    it('deletes nothing when cancelled', async () => {
        const fetchMock = stubFetch(noContent());
        renderWithQueries(<DeleteAuthorButton author={AUTHOR} onDeleted={vi.fn<() => void>()}/>);

        await userEvent.click(screen.getByRole('button', {name: 'Delete'}));
        await userEvent.click(screen.getByRole('button', {name: 'Cancel'}));

        expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument();
        expect(fetchMock).not.toHaveBeenCalled();
    });

    it('shows a refusal inside the confirmation, not in the error dialog', async () => {
        const reports: SystemErrorReport[] = [];
        const unsubscribe = subscribeSystemErrors((report) => {
            reports.push(report);
        });
        stubFetch(json({status: 409, code: 'author.changed'}, 409));
        renderWithQueries(<DeleteAuthorButton author={AUTHOR} onDeleted={vi.fn<() => void>()}/>);

        await userEvent.click(screen.getByRole('button', {name: 'Delete'}));
        await userEvent.click(screen.getByRole('button', {name: 'Delete for good'}));

        await expect(screen.findByRole('alert')).resolves.toHaveTextContent('This item was changed in the meantime. Reload and try again.');
        expect(reports).toHaveLength(0);

        unsubscribe();
    });
});
