import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {toCategoryId} from '@/entities/categories';
import {subscribeSystemErrors, type SystemErrorReport} from '@/shared/api';
import {json, noContent, renderWithQueries, stubFetch} from '@/shared/lib/test';
import {DeleteCategoryButton} from './DeleteCategoryButton';

const CATEGORY = {id: toCategoryId('00000000-0000-0000-0006-000000000002'), title: 'Action'};

describe(DeleteCategoryButton, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('deletes the category after confirmation, saying its subcategories go with it and its software stays', async () => {
        const fetchMock = stubFetch(noContent());
        const onDeleted = vi.fn<() => void>();
        renderWithQueries(<DeleteCategoryButton category={CATEGORY} onDeleted={onDeleted}/>);

        await userEvent.click(screen.getByRole('button', {name: 'Delete'}));

        expect(screen.getByRole('alertdialog', {name: 'Delete category'})).toHaveTextContent(
            'Delete “Action” with all its subcategories? Their software stays in the archive and only leaves them.',
        );

        await userEvent.click(screen.getByRole('button', {name: 'Delete for good'}));

        await vi.waitFor(() => {
            expect(onDeleted).toHaveBeenCalledExactlyOnceWith();
        });

        expect(fetchMock.mock.calls[0]?.[0]).toBe(`/api/categories/${CATEGORY.id}`);
        expect(fetchMock.mock.calls[0]?.[1]).toMatchObject({method: 'DELETE'});
    });

    it('deletes nothing when cancelled', async () => {
        const fetchMock = stubFetch(noContent());
        renderWithQueries(<DeleteCategoryButton category={CATEGORY} onDeleted={vi.fn<() => void>()}/>);

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
        stubFetch(json({status: 409, code: 'category.changed'}, 409));
        renderWithQueries(<DeleteCategoryButton category={CATEGORY} onDeleted={vi.fn<() => void>()}/>);

        await userEvent.click(screen.getByRole('button', {name: 'Delete'}));
        await userEvent.click(screen.getByRole('button', {name: 'Delete for good'}));

        await expect(screen.findByRole('alert')).resolves.toHaveTextContent('This item was changed in the meantime. Reload and try again.');
        expect(reports).toHaveLength(0);

        unsubscribe();
    });
});
