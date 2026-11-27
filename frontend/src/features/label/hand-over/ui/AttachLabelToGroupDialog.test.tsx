import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {subscribeSystemErrors, type SystemErrorReport} from '@/shared/api';
import {json, noContent, renderWithQueries, stubFetchAround} from '@/shared/lib/test';
import {AttachLabelToGroupDialog} from './AttachLabelToGroupDialog';

const LABEL = {id: '00000000-0000-0000-0004-000000000001', name: 'Ghost'};
const GHOSTS = '00000000-0000-0000-0005-000000000001';
const LOOKUP = new Map([
    ['/api/entity-lookup?types=group&query=ghosts', json({items: [{id: GHOSTS, type: 'group', title: 'Ghosts'}]})],
]);

function openDialog(onAttached = vi.fn<(group: string) => void>(), onClose = vi.fn<() => void>()) {
    renderWithQueries(<AttachLabelToGroupDialog open label={LABEL} onClose={onClose} onAttached={onAttached}/>);
}

async function chooseGhostsAndAttach() {
    await userEvent.type(screen.getByRole('combobox', {name: 'Group'}), 'ghosts');
    await userEvent.click(await screen.findByRole('option', {name: 'Ghosts'}));
    await userEvent.click(screen.getByRole('button', {name: 'Make a label'}));
}

describe(AttachLabelToGroupDialog, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('tells what attaching does and lets nothing be attached before a group is chosen', () => {
        stubFetchAround(LOOKUP, noContent());
        openDialog();

        expect(screen.getByRole('dialog', {name: 'Make a label of a group'})).toHaveTextContent(
            '“Ghost” becomes another name of the group chosen; its credits and memberships stay under this name.',
        );
        expect(screen.getByRole('button', {name: 'Make a label'})).toBeDisabled();
    });

    it('makes the label a label of the group found among groups only and hands over the group', async () => {
        const fetchMock = stubFetchAround(LOOKUP, noContent());
        const onAttached = vi.fn<(group: string) => void>();
        openDialog(onAttached);

        await chooseGhostsAndAttach();

        await vi.waitFor(() => {
            expect(onAttached).toHaveBeenCalledExactlyOnceWith(GHOSTS);
        });

        expect(fetchMock.mock.calls[0]?.[0]).toBe(`/api/labels/${LABEL.id}/attach-to-group`);
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

        await chooseGhostsAndAttach();

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

        await chooseGhostsAndAttach();

        await vi.waitFor(() => {
            expect(reports).toHaveLength(1);
        });

        unsubscribe();
    });

    it('asks to be closed by Cancel', async () => {
        const onClose = vi.fn<() => void>();
        openDialog(vi.fn<(group: string) => void>(), onClose);

        await userEvent.click(screen.getByRole('button', {name: 'Cancel'}));

        expect(onClose).toHaveBeenCalledExactlyOnceWith();
    });
});
