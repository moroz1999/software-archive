import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {toLabelId} from '@/entities/labels';
import {subscribeSystemErrors, type SystemErrorReport} from '@/shared/api';
import {json, noContent, renderWithQueries, stubFetchAround} from '@/shared/lib/test';
import {AttachLabelButton, type LabelOwner} from './AttachLabelButton';

const LABEL = {id: toLabelId('00000000-0000-0000-0004-000000000001'), name: 'Ghost'};
const JANE = '00000000-0000-0000-0003-000000000001';
const LOOKUP = new Map([['/api/entity-lookup?types=author%2Cgroup&query=jane', json({items: [{id: JANE, type: 'author', title: 'Jane Doe'}]})]]);

async function chooseJane() {
    await userEvent.click(screen.getByRole('button', {name: 'Attach to owner'}));
    await userEvent.type(screen.getByRole('combobox', {name: 'Person or group'}), 'jane');
    await userEvent.click(await screen.findByRole('option', {name: 'Jane Doe'}));
}

async function chooseJaneAndAttach() {
    await chooseJane();
    await userEvent.click(screen.getByRole('button', {name: 'Attach'}));
}

describe(AttachLabelButton, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('attaches the label to the person or group chosen and hands over the owner', async () => {
        const fetchMock = stubFetchAround(LOOKUP, noContent());
        const onAttached = vi.fn<(owner: LabelOwner) => void>();
        renderWithQueries(<AttachLabelButton label={LABEL} onAttached={onAttached}/>);

        await chooseJaneAndAttach();

        await vi.waitFor(() => {
            expect(onAttached).toHaveBeenCalledExactlyOnceWith({type: 'author', id: JANE});
        });

        expect(fetchMock.mock.calls[0]?.[0]).toBe(`/api/labels/${LABEL.id}/owner`);
        expect(fetchMock.mock.calls[0]?.[1]).toMatchObject({method: 'POST', body: JSON.stringify({type: 'author', id: JANE, display: false})});
    });

    it('makes the label the display name of its owner only when told to', async () => {
        const fetchMock = stubFetchAround(LOOKUP, noContent());
        renderWithQueries(<AttachLabelButton label={LABEL} onAttached={vi.fn<(owner: LabelOwner) => void>()}/>);

        await chooseJane();

        expect(screen.getByRole('checkbox', {name: 'Display name'})).not.toBeChecked();

        await userEvent.click(screen.getByRole('checkbox', {name: 'Display name'}));
        await userEvent.click(screen.getByRole('button', {name: 'Attach'}));

        await vi.waitFor(() => {
            expect(fetchMock.mock.calls[0]?.[1]).toMatchObject({body: JSON.stringify({type: 'author', id: JANE, display: true})});
        });
    });

    it('asks whose the label is and lets nothing be attached before an owner is chosen', async () => {
        stubFetchAround(LOOKUP, noContent());
        renderWithQueries(<AttachLabelButton label={LABEL} onAttached={vi.fn<(owner: LabelOwner) => void>()}/>);

        await userEvent.click(screen.getByRole('button', {name: 'Attach to owner'}));

        expect(screen.getByRole('dialog', {name: 'Attach to owner'})).toHaveTextContent('Whose label is “Ghost”?');
        expect(screen.getByRole('button', {name: 'Attach'})).toBeDisabled();
    });

    it.each([
        [409, 'label.owned', 'The label has got an owner in the meantime. Reload the page.'],
        [422, 'label.owner-unknown', 'That person or group is no longer in the archive.'],
        [422, 'label.display-one', 'They have no labels yet: this one can only be their display name.'],
        [403, 'access.denied', 'Only whoever added this, or an administrator, may change it.'],
    ])('shows the refusal %i %s inside the dialog, not in the error dialog', async (status, code, text) => {
        const reports: SystemErrorReport[] = [];
        const unsubscribe = subscribeSystemErrors((report) => {
            reports.push(report);
        });
        stubFetchAround(LOOKUP, json({status, code}, status));
        renderWithQueries(<AttachLabelButton label={LABEL} onAttached={vi.fn<(owner: LabelOwner) => void>()}/>);

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
        renderWithQueries(<AttachLabelButton label={LABEL} onAttached={vi.fn<(owner: LabelOwner) => void>()}/>);

        await chooseJaneAndAttach();

        await vi.waitFor(() => {
            expect(reports).toHaveLength(1);
        });

        unsubscribe();
    });
});
