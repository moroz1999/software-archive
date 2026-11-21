import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen, within} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {type LabelOwner, toLabelId} from '@/entities/labels';
import {subscribeSystemErrors, type SystemErrorReport} from '@/shared/api';
import {json, renderWithQueries, stubFetch} from '@/shared/lib/test';
import {MakeAuthorDialog} from './MakeAuthorDialog';

const ID = toLabelId('00000000-0000-0000-0004-000000000001');
const GHOST = {id: ID, name: 'Ghost', owner: null};
const JANE = '00000000-0000-0000-0003-000000000001';
const MADE = '00000000-0000-0000-0003-000000000002';

async function makeAuthor() {
    await userEvent.click(within(screen.getByRole('dialog', {name: 'Make author'})).getByRole('button', {name: 'Make author'}));
}

describe(MakeAuthorDialog, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('replaces the label with a person and hands over the new person', async () => {
        const fetchMock = stubFetch(json({id: MADE}, 201));
        const onMade = vi.fn<(owner: LabelOwner) => void>();
        renderWithQueries(<MakeAuthorDialog open onClose={vi.fn<() => void>()} label={GHOST} onMade={onMade}/>);

        expect(screen.getByRole('dialog', {name: 'Make author'})).toHaveTextContent('Make a person known as “Ghost”?');

        await userEvent.click(within(screen.getByRole('dialog', {name: 'Make author'})).getByRole('button', {name: 'Make author'}));

        await vi.waitFor(() => {
            expect(onMade).toHaveBeenCalledExactlyOnceWith({type: 'author', id: MADE});
        });

        expect(fetchMock.mock.calls[0]?.[0]).toBe(`/api/labels/${ID}/author`);
        expect(fetchMock.mock.calls[0]?.[1]).toMatchObject({method: 'POST'});
    });

    it('says the label is taken away from whoever has it', () => {
        stubFetch(json({id: MADE}, 201));
        renderWithQueries(<MakeAuthorDialog
            open
            onClose={vi.fn<() => void>()}
            label={{...GHOST, owner: {type: 'author', id: JANE, name: 'Jane Doe', note: 'JD'}}}
            onMade={vi.fn<(owner: LabelOwner) => void>()}
        />);

        expect(screen.getByRole('dialog', {name: 'Make author'})).toHaveTextContent('It will no longer be a label of Jane Doe.');
    });

    it.each([
        [422, 'label.display-one', 'This label is the display name of its owner: choose another display name for them first.'],
        [422, 'author.nameless', 'This label is the only name of its owner: it cannot be taken away from them.'],
        [403, 'access.denied', 'Only whoever added this, or an administrator, may change it.'],
    ])('shows the refusal %i %s inside the dialog, not in the error dialog', async (status, code, text) => {
        const reports: SystemErrorReport[] = [];
        const unsubscribe = subscribeSystemErrors((report) => {
            reports.push(report);
        });
        stubFetch(json({status, code}, status));
        renderWithQueries(<MakeAuthorDialog open onClose={vi.fn<() => void>()} label={GHOST} onMade={vi.fn<(owner: LabelOwner) => void>()}/>);

        await makeAuthor();

        await expect(screen.findByRole('alert')).resolves.toHaveTextContent(text);
        expect(reports).toHaveLength(0);

        unsubscribe();
    });

    it('hands a system error to the error dialog', async () => {
        const reports: SystemErrorReport[] = [];
        const unsubscribe = subscribeSystemErrors((report) => {
            reports.push(report);
        });
        stubFetch(json({}, 500));
        renderWithQueries(<MakeAuthorDialog open onClose={vi.fn<() => void>()} label={GHOST} onMade={vi.fn<(owner: LabelOwner) => void>()}/>);

        await makeAuthor();

        await vi.waitFor(() => {
            expect(reports).toHaveLength(1);
        });

        unsubscribe();
    });

    it('asks to be closed by Cancel', async () => {
        const onClose = vi.fn<() => void>();
        renderWithQueries(<MakeAuthorDialog label={GHOST} onMade={vi.fn<(owner: LabelOwner) => void>()} open onClose={onClose}/>);

        await userEvent.click(screen.getByRole('button', {name: 'Cancel'}));

        expect(onClose).toHaveBeenCalledExactlyOnceWith();
    });
});
