import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {toLabelId} from '@/entities/labels';
import {toAuthorId, type AuthorId} from '@/entities/authors';
import {subscribeSystemErrors, type SystemErrorReport} from '@/shared/api';
import {json, noContent, renderWithQueries, stubFetchAround} from '@/shared/lib/test';
import {MergeLabelIntoAuthorDialog} from './MergeLabelIntoAuthorDialog';

const LABEL = {id: toLabelId('00000000-0000-0000-0004-000000000001'), name: 'Ghost'};
const JANE = '00000000-0000-0000-0003-000000000001';
const LOOKUP = new Map([
    ['/api/entity-lookup?types=author&query=jane', json({items: [{id: JANE, type: 'author', title: 'Jane Doe'}]})],
]);

function openDialog(onMerged = vi.fn<(author: AuthorId) => void>(), onClose = vi.fn<() => void>()) {
    renderWithQueries(<MergeLabelIntoAuthorDialog open label={LABEL} onClose={onClose} onMerged={onMerged}/>);
}

async function chooseJaneAndMerge() {
    await userEvent.type(screen.getByRole('combobox', {name: 'Author'}), 'jane');
    await userEvent.click(await screen.findByRole('option', {name: 'Jane Doe'}));
    await userEvent.click(screen.getByRole('button', {name: 'Merge and delete'}));
}

describe(MergeLabelIntoAuthorDialog, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('tells what merging does and lets nothing be merged before a person is chosen', () => {
        stubFetchAround(LOOKUP, noContent());
        openDialog();

        expect(screen.getByRole('dialog', {name: 'Merge into author and delete'})).toHaveTextContent(
            'Its credits, memberships, locations and sources go to the person chosen, and “Ghost” is deleted.',
        );
        expect(screen.getByRole('button', {name: 'Merge and delete'})).toBeDisabled();
    });

    it('merges the label into the person found among people only and hands over the person', async () => {
        const fetchMock = stubFetchAround(LOOKUP, noContent());
        const onMerged = vi.fn<(author: AuthorId) => void>();
        openDialog(onMerged);

        await chooseJaneAndMerge();

        await vi.waitFor(() => {
            expect(onMerged).toHaveBeenCalledExactlyOnceWith(toAuthorId(JANE));
        });

        expect(fetchMock.mock.calls[0]?.[0]).toBe(`/api/labels/${LABEL.id}/merge-into-author`);
        expect(fetchMock.mock.calls[0]?.[1]).toMatchObject({method: 'POST', body: JSON.stringify({author: JANE})});
    });

    it.each([
        [422, 'label.owner-unknown', 'That person is no longer in the archive.'],
        [422, 'label.of-person', 'This label is a person’s: only a label of nobody or of a group is merged.'],
        [403, 'access.denied', 'Only an administrator merges a label.'],
    ])('shows the refusal %i %s inside the dialog, not in the error dialog', async (status, code, text) => {
        const reports: SystemErrorReport[] = [];
        const unsubscribe = subscribeSystemErrors((report) => {
            reports.push(report);
        });
        stubFetchAround(LOOKUP, json({status, code}, status));
        openDialog();

        await chooseJaneAndMerge();

        await expect(screen.findByRole('alert')).resolves.toHaveTextContent(text);
        expect(reports).toHaveLength(0);

        unsubscribe();
    });

    it('hands a system error to the error dialog', async () => {
        const reports: SystemErrorReport[] = [];
        const unsubscribe = subscribeSystemErrors((report) => {
            reports.push(report);
        });
        stubFetchAround(LOOKUP, json({}, 500));
        openDialog();

        await chooseJaneAndMerge();

        await vi.waitFor(() => {
            expect(reports).toHaveLength(1);
        });

        unsubscribe();
    });

    it('asks to be closed by Cancel', async () => {
        const onClose = vi.fn<() => void>();
        openDialog(vi.fn<(author: AuthorId) => void>(), onClose);

        await userEvent.click(screen.getByRole('button', {name: 'Cancel'}));

        expect(onClose).toHaveBeenCalledExactlyOnceWith();
    });
});
