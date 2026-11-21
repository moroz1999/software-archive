import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {toLabelId} from '@/entities/labels';
import {subscribeSystemErrors, type SystemErrorReport} from '@/shared/api';
import {json, noContent, renderWithQueries, stubFetchAround} from '@/shared/lib/test';
import {AttachLabelDialog, type LabelOwner} from './AttachLabelDialog';

const LABEL = {id: toLabelId('00000000-0000-0000-0004-000000000001'), name: 'Ghost'};
const JANE = '00000000-0000-0000-0003-000000000001';
const CREW = '00000000-0000-0000-0005-000000000001';
const LOOKUP = new Map([
    ['/api/entity-lookup?types=author%2Cgroup&query=jane', json({items: [{id: JANE, type: 'author', title: 'Jane Doe'}]})],
    ['/api/entity-lookup?types=author%2Cgroup&query=crew', json({items: [{id: CREW, type: 'group', title: 'Crew'}]})],
]);

async function chooseJane() {
    await userEvent.type(screen.getByRole('combobox', {name: 'Person or group'}), 'jane');
    await userEvent.click(await screen.findByRole('option', {name: 'Jane Doe'}));
}

async function chooseJaneAndAttach() {
    await chooseJane();
    await userEvent.click(screen.getByRole('button', {name: 'Save'}));
}

describe(AttachLabelDialog, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
        vi.restoreAllMocks();
    });

    it('shows the owners found above its window, in the top layer', async () => {
        stubFetchAround(LOOKUP, noContent());
        const showPopover = vi.spyOn(HTMLElement.prototype, 'showPopover');
        renderWithQueries(<AttachLabelDialog open onClose={vi.fn<() => void>()} label={LABEL} onAttached={vi.fn<(owner: LabelOwner) => void>()}/>);

            await userEvent.type(screen.getByRole('combobox', {name: 'Person or group'}), 'jane');
        const option = await screen.findByRole('option', {name: 'Jane Doe'});

        expect(showPopover.mock.contexts.at(-1)).toContainElement(option);
    });

    it('attaches the label to the person or group chosen and hands over the owner', async () => {
        const fetchMock = stubFetchAround(LOOKUP, noContent());
        const onAttached = vi.fn<(owner: LabelOwner) => void>();
        renderWithQueries(<AttachLabelDialog open onClose={vi.fn<() => void>()} label={LABEL} onAttached={onAttached}/>);

        await chooseJaneAndAttach();

        await vi.waitFor(() => {
            expect(onAttached).toHaveBeenCalledExactlyOnceWith({type: 'author', id: JANE});
        });

        expect(fetchMock.mock.calls[0]?.[0]).toBe(`/api/labels/${LABEL.id}/owner`);
        expect(fetchMock.mock.calls[0]?.[1]).toMatchObject({method: 'POST', body: JSON.stringify({type: 'author', id: JANE, display: false})});
    });

    it('makes the label the display name of its owner only when told to', async () => {
        const fetchMock = stubFetchAround(LOOKUP, noContent());
        renderWithQueries(<AttachLabelDialog open onClose={vi.fn<() => void>()} label={LABEL} onAttached={vi.fn<(owner: LabelOwner) => void>()}/>);

        await chooseJane();

        expect(screen.getByRole('checkbox', {name: 'Display name'})).not.toBeChecked();

        await userEvent.click(screen.getByRole('checkbox', {name: 'Display name'}));
        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        await vi.waitFor(() => {
            expect(fetchMock.mock.calls[0]?.[1]).toMatchObject({body: JSON.stringify({type: 'author', id: JANE, display: true})});
        });
    });

    it('offers no display name for a group, which has none, and attaches the label to it', async () => {
        const fetchMock = stubFetchAround(LOOKUP, noContent());
        renderWithQueries(<AttachLabelDialog open onClose={vi.fn<() => void>()} label={LABEL} onAttached={vi.fn<(owner: LabelOwner) => void>()}/>);

            await userEvent.type(screen.getByRole('combobox', {name: 'Person or group'}), 'crew');
        await userEvent.click(await screen.findByRole('option', {name: 'Crew'}));

        expect(screen.queryByRole('checkbox', {name: 'Display name'})).not.toBeInTheDocument();

        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        await vi.waitFor(() => {
            expect(fetchMock.mock.calls[0]?.[1]).toMatchObject({body: JSON.stringify({type: 'group', id: CREW, display: false})});
        });
    });

    it('asks whose the label is and lets nothing be attached before an owner is chosen', () => {
        stubFetchAround(LOOKUP, noContent());
        renderWithQueries(<AttachLabelDialog open onClose={vi.fn<() => void>()} label={LABEL} onAttached={vi.fn<(owner: LabelOwner) => void>()}/>);

        expect(screen.getByRole('dialog', {name: 'Say whose label'})).toHaveTextContent('Whose label is “Ghost”?');
        expect(screen.getByRole('button', {name: 'Save'})).toBeDisabled();
    });

    it.each([
        [409, 'label.owned', 'Someone has said whose label this is in the meantime. Reload the page.'],
        [422, 'label.owner-unknown', 'That person or group is no longer in the archive.'],
        [422, 'label.display-one', 'They have no labels yet: this one can only be their display name.'],
        [403, 'access.denied', 'Only whoever added this, or an administrator, may change it.'],
    ])('shows the refusal %i %s inside the dialog, not in the error dialog', async (status, code, text) => {
        const reports: SystemErrorReport[] = [];
        const unsubscribe = subscribeSystemErrors((report) => {
            reports.push(report);
        });
        stubFetchAround(LOOKUP, json({status, code}, status));
        renderWithQueries(<AttachLabelDialog open onClose={vi.fn<() => void>()} label={LABEL} onAttached={vi.fn<(owner: LabelOwner) => void>()}/>);

        await chooseJaneAndAttach();

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
        renderWithQueries(<AttachLabelDialog open onClose={vi.fn<() => void>()} label={LABEL} onAttached={vi.fn<(owner: LabelOwner) => void>()}/>);

        await chooseJaneAndAttach();

        await vi.waitFor(() => {
            expect(reports).toHaveLength(1);
        });

        unsubscribe();
    });

    it('asks to be closed by Cancel', async () => {
        const onClose = vi.fn<() => void>();
        renderWithQueries(<AttachLabelDialog label={LABEL} onAttached={vi.fn<(owner: LabelOwner) => void>()} open onClose={onClose}/>);

        await userEvent.click(screen.getByRole('button', {name: 'Cancel'}));

        expect(onClose).toHaveBeenCalledExactlyOnceWith();
    });
});
