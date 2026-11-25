import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen, within} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {MemoryRouter} from 'react-router';
import {breadcrumbSteps, json, noContent, renderWithQueries, stubFetchAround} from '@/shared/lib/test';
import {toGroupId} from '@/entities/groups';
import {GroupEditor} from './GroupEditor';

const ID = '00000000-0000-0000-0005-000000000001';
const FINLAND = '00000000-0000-0000-0002-000000000001';
const HELSINKI = '00000000-0000-0000-0002-000000000011';
const CREW_LABEL = '00000000-0000-0000-0004-000000000001';
const CREW = {
    id: ID,
    name: 'Crew Ltd',
    notes: null,
    types: ['demogroup'],
    labels: [{id: CREW_LABEL, name: 'Crew'}],
    locations: [{countryId: FINLAND, country: 'Finland', cityId: HELSINKI, city: 'Helsinki', primary: true}],
    members: [],
    memberGroups: [],
    groups: [],
    origins: [], added: {by: 'Zed', at: '2026-01-01T00:00:00Z'},
    canChange: true,
};

/** What the editor loads besides the group. */
const AROUND = new Map([
    ['/api/group-types', json(['crack group', 'demogroup'])],
    ['/api/countries', json([{id: FINLAND, name: 'Finland'}])],
    [`/api/countries/${FINLAND}/cities`, json([{id: HELSINKI, name: 'Helsinki'}])],
]);

function renderEditor(onSaved = vi.fn<() => void>(), onDeleted = vi.fn<() => void>()) {
    renderWithQueries(<MemoryRouter><GroupEditor id={toGroupId(ID)} onSaved={onSaved} onDeleted={onDeleted}/></MemoryRouter>);
}

describe(GroupEditor, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('offers every type of the reference book, the types of the group checked', async () => {
        stubFetchAround(AROUND, json(CREW));
        renderEditor();

        await expect(screen.findByRole('checkbox', {name: 'demogroup'})).resolves.toBeChecked();
        expect(screen.getByRole('checkbox', {name: 'crack group'})).not.toBeChecked();
    });

    it('shows where imports took the group from, as text', async () => {
        stubFetchAround(AROUND, json({...CREW, origins: [{origin: 'caglrc.cc', externalId: '17', url: null}]}));
        renderEditor();

        await screen.findByRole('textbox', {name: 'Name'});

        expect(within(screen.getByRole('region', {name: 'Sources'})).getByRole('listitem')).toHaveTextContent('caglrc.cc17');
    });

    it('leads back from the editing to the group and the groups', async () => {
        stubFetchAround(AROUND, json(CREW));
        renderEditor();

        await screen.findByRole('textbox', {name: 'Name'});

        expect(breadcrumbSteps()).toStrictEqual([['Groups', '/groups'], ['Crew Ltd', `/groups/${ID}`], ['Editing', null]]);
    });

    it('shows a skeleton, then the form with the group, its types, labels and locations', async () => {
        stubFetchAround(AROUND, json(CREW));
        renderEditor();

        expect(screen.getByTestId('group-editor-skeleton')).toBeInTheDocument();
        await expect(screen.findByRole('textbox', {name: 'Name'})).resolves.toHaveValue('Crew Ltd');
        await expect(screen.findByRole('checkbox', {name: 'demogroup'})).resolves.toBeChecked();
        expect(screen.getByRole('textbox', {name: 'Label 1'})).toHaveValue('Crew');
        await expect(screen.findByRole('option', {name: 'Helsinki', selected: true})).resolves.toBeInTheDocument();
    });

    it('puts Delete next to the title and returns to the group on Cancel', async () => {
        stubFetchAround(AROUND, json(CREW));
        renderEditor();

        await screen.findByRole('textbox', {name: 'Name'});

        expect(screen.getByRole('heading', {level: 1, name: 'Edit group'})).toBeInTheDocument();
        expect(screen.getByRole('button', {name: 'Delete'})).toBeInTheDocument();
        expect(screen.getByRole('link', {name: 'Cancel'})).toHaveAttribute('href', `/groups/${ID}`);
    });

    it('starts with its main section, the required fields starred', async () => {
        stubFetchAround(AROUND, json(CREW));
        renderEditor();

        await screen.findByRole('textbox', {name: 'Name'});

        const main = screen.getByRole('region', {name: 'Main'});

        expect(within(main).getByRole('textbox', {name: 'Name'})).toBeInTheDocument();
        expect(within(main).getByRole('group', {name: 'Types'})).toBeInTheDocument();
        expect(within(main).getAllByText('*')).toHaveLength(1);
    });

    it('puts the names, the places and the members of the group in sections of their own', async () => {
        stubFetchAround(AROUND, json(CREW));
        renderEditor();

        await screen.findByRole('textbox', {name: 'Name'});

        expect(within(screen.getByRole('region', {name: 'Names'})).getByRole('textbox', {name: 'Label 1'})).toBeInTheDocument();
        expect(screen.queryByRole('radio', {name: 'Display name'})).not.toBeInTheDocument();
        expect(within(screen.getByRole('region', {name: 'Geography'})).getByRole('group', {name: 'Locations'})).toBeInTheDocument();
        expect(screen.getByRole('region', {name: 'Members'})).toBeInTheDocument();
    });

    it('offers the roles of a member and no dates of the membership', async () => {
        const member = {id: CREW_LABEL, type: 'author-label', title: 'Crew', name: 'Crew', personId: null, personName: null, roles: ['coder']};
        stubFetchAround(AROUND, json({...CREW, members: [member]}));
        renderEditor();

        await expect(screen.findByRole('combobox', {name: 'Role for Crew'})).resolves.toBeInTheDocument();
        expect(screen.queryByRole('textbox', {name: 'Start date for Crew'})).not.toBeInTheDocument();
        expect(screen.queryByRole('textbox', {name: 'End date for Crew'})).not.toBeInTheDocument();
    });

    it('saves the whole group with its types, labels and locations', async () => {
        const fetchMock = stubFetchAround(AROUND, json(CREW), noContent());
        const onSaved = vi.fn<() => void>();
        renderEditor(onSaved);

        await screen.findByRole('textbox', {name: 'Name'});
        await userEvent.click(screen.getByRole('button', {name: 'Add label'}));
        await userEvent.type(screen.getByRole('textbox', {name: 'Label 2'}), 'CRW');
        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        await vi.waitFor(() => {
            expect(onSaved).toHaveBeenCalledExactlyOnceWith();
        });

        const put = fetchMock.mock.calls.find(([, init]) => init?.method === 'PUT');

        expect(put?.[1]?.body).toBe(JSON.stringify({
            name: 'Crew Ltd',
            notes: null,
            types: ['demogroup'],
            labels: [{id: CREW_LABEL, name: 'Crew'}, {id: null, name: 'CRW'}],
            locations: [{country: FINLAND, city: HELSINKI, primary: true}],
            members: [],
        }));
    });

    it('says why the labels were refused under the labels', async () => {
        stubFetchAround(AROUND, json(CREW), json({status: 422, code: 'label.not-own', errors: [{field: 'labels', code: 'label.not-own'}]}, 422));
        renderEditor();

        await screen.findByRole('textbox', {name: 'Name'});
        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        await vi.waitFor(() => {
            expect(screen.getByRole('group', {name: 'Labels'})).toHaveAccessibleDescription('One of the labels belongs to someone else. Reload and try again.');
        });
    });

    it('reports the group deleted', async () => {
        stubFetchAround(AROUND, json(CREW), noContent());
        const onDeleted = vi.fn<() => void>();
        renderEditor(vi.fn<() => void>(), onDeleted);

        await screen.findByRole('textbox', {name: 'Name'});
        await userEvent.click(screen.getByRole('button', {name: 'Delete'}));
        await userEvent.click(screen.getByRole('button', {name: 'Delete for good'}));

        await vi.waitFor(() => {
            expect(onDeleted).toHaveBeenCalledExactlyOnceWith();
        });
    });
});
