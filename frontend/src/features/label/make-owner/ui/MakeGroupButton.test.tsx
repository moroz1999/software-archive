import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen, within} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import i18n from 'i18next';
import {type LabelOwner, toLabelId} from '@/entities/labels';
import {subscribeSystemErrors, type SystemErrorReport} from '@/shared/api';
import {json, renderWithQueries, stubFetchAround} from '@/shared/lib/test';
import {MakeGroupButton} from './MakeGroupButton';

const ID = toLabelId('00000000-0000-0000-0004-000000000001');
const GHOST = {id: ID, name: 'Ghost', owner: null};
const CREW = '00000000-0000-0000-0005-000000000001';
const MADE = '00000000-0000-0000-0005-000000000002';
const TYPES = new Map([['/api/group-types', json(['demogroup', 'label'])]]);

async function makeDemogroup() {
    await userEvent.click(screen.getByRole('button', {name: 'Make group'}));
    await userEvent.click(await screen.findByRole('checkbox', {name: 'Demo group'}));
    await userEvent.click(within(screen.getByRole('dialog', {name: 'Make group'})).getByRole('button', {name: 'Make group'}));
}

describe(MakeGroupButton, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('makes a group under the name of the label with the types chosen and hands over the new group', async () => {
        const fetchMock = stubFetchAround(TYPES, json({id: MADE}, 201));
        const onMade = vi.fn<(owner: LabelOwner) => void>();
        renderWithQueries(<MakeGroupButton label={GHOST} onMade={onMade}/>);

        await makeDemogroup();

        await vi.waitFor(() => {
            expect(onMade).toHaveBeenCalledExactlyOnceWith({type: 'group', id: MADE});
        });

        const call = fetchMock.mock.calls.find(([url]) => url === `/api/labels/${ID}/group`);

        expect(call?.[1]).toMatchObject({method: 'POST', body: JSON.stringify({types: ['demogroup']})});
    });

    it('asks for the types of the group named after the label and makes nothing before one is chosen', async () => {
        stubFetchAround(TYPES, json({id: MADE}, 201));
        renderWithQueries(<MakeGroupButton label={GHOST} onMade={vi.fn<(owner: LabelOwner) => void>()}/>);

        await userEvent.click(screen.getByRole('button', {name: 'Make group'}));

        expect(screen.getByRole('dialog', {name: 'Make group'})).toHaveTextContent('Make a group named “Ghost”?');
        await expect(screen.findByRole('checkbox', {name: 'Label'})).resolves.not.toBeChecked();
        expect(within(screen.getByRole('dialog', {name: 'Make group'})).getByRole('button', {name: 'Make group'})).toBeDisabled();
    });

    it('shows group types in the UI language and sends their stable codes', async () => {
        await i18n.changeLanguage('ru');
        const fetchMock = stubFetchAround(TYPES, json({id: MADE}, 201));
        renderWithQueries(<MakeGroupButton label={GHOST} onMade={vi.fn<(owner: LabelOwner) => void>()}/>);

        await userEvent.click(screen.getByRole('button', {name: 'Сделать группу'}));
        await userEvent.click(await screen.findByRole('checkbox', {name: 'Демо-группа'}));
        await userEvent.click(within(screen.getByRole('dialog', {name: 'Сделать группу'})).getByRole('button', {name: 'Сделать группу'}));

        const call = fetchMock.mock.calls.find(([url]) => url === `/api/labels/${ID}/group`);

        expect(call?.[1]).toMatchObject({method: 'POST', body: JSON.stringify({types: ['demogroup']})});
    });

    it('says the label is taken away from whoever has it', async () => {
        stubFetchAround(TYPES, json({id: MADE}, 201));
        renderWithQueries(<MakeGroupButton
            label={{...GHOST, owner: {type: 'group', id: CREW, name: 'Crew', note: null}}}
            onMade={vi.fn<(owner: LabelOwner) => void>()}
        />);

        await userEvent.click(screen.getByRole('button', {name: 'Make group'}));

        expect(screen.getByRole('dialog', {name: 'Make group'})).toHaveTextContent('It will no longer be a label of Crew.');
    });

    it.each([
        [422, 'group.type-unknown', 'A type chosen is no longer in the archive. Reload the page.'],
        [422, 'label.display-one', 'This label is the display name of its owner: choose another display name for them first.'],
        [422, 'author.nameless', 'This label is the only name of its owner: it cannot be taken away from them.'],
        [403, 'access.denied', 'Only whoever added this, or an administrator, may change it.'],
    ])('shows the refusal %i %s inside the dialog, not in the error dialog', async (status, code, text) => {
        const reports: SystemErrorReport[] = [];
        const unsubscribe = subscribeSystemErrors((report) => {
            reports.push(report);
        });
        stubFetchAround(TYPES, json({status, code}, status));
        renderWithQueries(<MakeGroupButton label={GHOST} onMade={vi.fn<(owner: LabelOwner) => void>()}/>);

        await makeDemogroup();

        await expect(screen.findByRole('alert')).resolves.toHaveTextContent(text);
        expect(reports).toHaveLength(0);

        unsubscribe();
    });

    it('hands a system error to the error dialog', async () => {
        const reports: SystemErrorReport[] = [];
        const unsubscribe = subscribeSystemErrors((report) => {
            reports.push(report);
        });
        stubFetchAround(TYPES, json({}, 500));
        renderWithQueries(<MakeGroupButton label={GHOST} onMade={vi.fn<(owner: LabelOwner) => void>()}/>);

        await makeDemogroup();

        await vi.waitFor(() => {
            expect(reports).toHaveLength(1);
        });

        unsubscribe();
    });
});
