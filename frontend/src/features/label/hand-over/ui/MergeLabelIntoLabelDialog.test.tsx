import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {toLabelId, type LabelId} from '@/entities/labels';
import {subscribeSystemErrors, type SystemErrorReport} from '@/shared/api';
import {json, noContent, renderWithQueries, stubFetchAround} from '@/shared/lib/test';
import {MergeLabelIntoLabelDialog} from './MergeLabelIntoLabelDialog';

const GHOST = '00000000-0000-0000-0004-000000000001';
const SPOOK = '00000000-0000-0000-0004-000000000002';
const LABEL = {id: toLabelId(GHOST), name: 'Ghost'};
const LOOKUP = new Map([
    ['/api/entity-lookup?types=author-label%2Cgroup-label&query=gh', json({items: [
        {id: GHOST, ownerId: null, type: 'author-label', title: 'Ghost'},
        {id: SPOOK, ownerId: null, type: 'author-label', title: 'Ghosty'},
    ]})],
]);

function openDialog(onMerged = vi.fn<(label: LabelId) => void>(), onClose = vi.fn<() => void>()) {
    renderWithQueries(<MergeLabelIntoLabelDialog open label={LABEL} onClose={onClose} onMerged={onMerged}/>);
}

async function chooseGhostyAndMerge() {
    await userEvent.type(screen.getByRole('combobox', {name: 'Label'}), 'gh');
    await userEvent.click(await screen.findByRole('option', {name: 'Ghosty'}));
    await userEvent.click(screen.getByRole('button', {name: 'Merge and delete'}));
}

describe(MergeLabelIntoLabelDialog, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('tells what merging does and lets nothing be merged before a label is chosen', () => {
        stubFetchAround(LOOKUP, noContent());
        openDialog();

        expect(screen.getByRole('dialog', {name: 'Merge into label and delete'})).toHaveTextContent(
            'Its credits, memberships, locations and sources go to the label chosen, and “Ghost” is deleted.',
        );
        expect(screen.getByRole('button', {name: 'Merge and delete'})).toBeDisabled();
    });

    it('offers every label but the one being merged', async () => {
        stubFetchAround(LOOKUP, noContent());
        openDialog();

        await userEvent.type(screen.getByRole('combobox', {name: 'Label'}), 'gh');

        await expect(screen.findByRole('option', {name: 'Ghosty'})).resolves.toBeInTheDocument();
        expect(screen.queryByRole('option', {name: 'Ghost'})).not.toBeInTheDocument();
    });

    it('merges the label into the label found among labels only and hands over that label', async () => {
        const fetchMock = stubFetchAround(LOOKUP, noContent());
        const onMerged = vi.fn<(label: LabelId) => void>();
        openDialog(onMerged);

        await chooseGhostyAndMerge();

        await vi.waitFor(() => {
            expect(onMerged).toHaveBeenCalledExactlyOnceWith(toLabelId(SPOOK));
        });

        expect(fetchMock.mock.calls[0]?.[0]).toBe(`/api/labels/${GHOST}/merge-into-label`);
        expect(fetchMock.mock.calls[0]?.[1]).toMatchObject({method: 'POST', body: JSON.stringify({label: SPOOK})});
    });

    it.each([
        [422, 'label.owner-unknown', 'That label is no longer in the archive.'],
        [422, 'label.merge-into-itself', 'A label is not merged into itself.'],
        [422, 'label.of-person', 'This label is a person’s: only a label of nobody or of a group is merged.'],
        [403, 'access.denied', 'Only an administrator merges a label.'],
    ])('shows the refusal %i %s inside the dialog, not in the error dialog', async (status, code, text) => {
        const reports: SystemErrorReport[] = [];
        const unsubscribe = subscribeSystemErrors((report) => {
            reports.push(report);
        });
        stubFetchAround(LOOKUP, json({status, code}, status));
        openDialog();

        await chooseGhostyAndMerge();

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

        await chooseGhostyAndMerge();

        await vi.waitFor(() => {
            expect(reports).toHaveLength(1);
        });

        unsubscribe();
    });

    it('asks to be closed by Cancel', async () => {
        const onClose = vi.fn<() => void>();
        openDialog(vi.fn<(label: LabelId) => void>(), onClose);

        await userEvent.click(screen.getByRole('button', {name: 'Cancel'}));

        expect(onClose).toHaveBeenCalledExactlyOnceWith();
    });
});
