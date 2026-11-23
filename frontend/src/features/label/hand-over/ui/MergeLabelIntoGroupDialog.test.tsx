import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {toLabelId} from '@/entities/labels';
import {toGroupId, type GroupId} from '@/entities/groups';
import {subscribeSystemErrors, type SystemErrorReport} from '@/shared/api';
import {json, noContent, renderWithQueries, stubFetchAround} from '@/shared/lib/test';
import {MergeLabelIntoGroupDialog} from './MergeLabelIntoGroupDialog';

const LABEL = {id: toLabelId('00000000-0000-0000-0004-000000000001'), name: 'Ghost'};
const GHOSTS = '00000000-0000-0000-0005-000000000001';
const LOOKUP = new Map([
    ['/api/entity-lookup?types=group&query=ghosts', json({items: [{id: GHOSTS, type: 'group', title: 'Ghosts'}]})],
]);

function openDialog(onMerged = vi.fn<(group: GroupId) => void>(), onClose = vi.fn<() => void>()) {
    renderWithQueries(<MergeLabelIntoGroupDialog open label={LABEL} onClose={onClose} onMerged={onMerged}/>);
}

async function chooseGhostsAndMerge() {
    await userEvent.type(screen.getByRole('combobox', {name: 'Group'}), 'ghosts');
    await userEvent.click(await screen.findByRole('option', {name: 'Ghosts'}));
    await userEvent.click(screen.getByRole('button', {name: 'Merge and delete'}));
}

describe(MergeLabelIntoGroupDialog, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('tells what merging does and lets nothing be merged before a group is chosen', () => {
        stubFetchAround(LOOKUP, noContent());
        openDialog();

        expect(screen.getByRole('dialog', {name: 'Merge into group and delete'})).toHaveTextContent(
            'Its credits, memberships, locations and sources go to the group chosen, and “Ghost” is deleted.',
        );
        expect(screen.getByRole('button', {name: 'Merge and delete'})).toBeDisabled();
    });

    it('merges the label into the group found among groups only and hands over the group', async () => {
        const fetchMock = stubFetchAround(LOOKUP, noContent());
        const onMerged = vi.fn<(group: GroupId) => void>();
        openDialog(onMerged);

        await chooseGhostsAndMerge();

        await vi.waitFor(() => {
            expect(onMerged).toHaveBeenCalledExactlyOnceWith(toGroupId(GHOSTS));
        });

        expect(fetchMock.mock.calls[0]?.[0]).toBe(`/api/labels/${LABEL.id}/merge-into-group`);
        expect(fetchMock.mock.calls[0]?.[1]).toMatchObject({method: 'POST', body: JSON.stringify({group: GHOSTS})});
    });

    it.each([
        [422, 'label.owner-unknown', 'That group is no longer in the archive.'],
        [422, 'label.of-person', 'This label is a person’s: only a label of nobody or of a group is given to a group.'],
        [403, 'access.denied', 'Only an administrator gives a label to a group.'],
    ])('shows the refusal %i %s inside the dialog, not in the error dialog', async (status, code, text) => {
        const reports: SystemErrorReport[] = [];
        const unsubscribe = subscribeSystemErrors((report) => {
            reports.push(report);
        });
        stubFetchAround(LOOKUP, json({status, code}, status));
        openDialog();

        await chooseGhostsAndMerge();

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

        await chooseGhostsAndMerge();

        await vi.waitFor(() => {
            expect(reports).toHaveLength(1);
        });

        unsubscribe();
    });

    it('asks to be closed by Cancel', async () => {
        const onClose = vi.fn<() => void>();
        openDialog(vi.fn<(group: GroupId) => void>(), onClose);

        await userEvent.click(screen.getByRole('button', {name: 'Cancel'}));

        expect(onClose).toHaveBeenCalledExactlyOnceWith();
    });
});
