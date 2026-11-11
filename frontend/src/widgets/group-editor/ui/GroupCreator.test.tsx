import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {MemoryRouter} from 'react-router';
import {json, renderWithQueries, stubFetchRoutes} from '@/shared/lib/test';
import {GroupCreator} from './GroupCreator';

const ID = '00000000-0000-0000-0005-000000000001';
const FINLAND = '00000000-0000-0000-0002-000000000001';

describe(GroupCreator, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('adds a group with its types, labels and a place', async () => {
        const fetchMock = stubFetchRoutes(new Map([
            ['/api/group-types', json(['crack group', 'demogroup'])],
            ['/api/countries', json([{id: FINLAND, name: 'Finland'}])],
            [`/api/countries/${FINLAND}/cities`, json([])],
            ['/api/groups', json({id: ID}, 201)],
        ]));
        const onCreated = vi.fn<(id: string) => void>();
        renderWithQueries(<MemoryRouter><GroupCreator onCreated={onCreated}/></MemoryRouter>);

        await userEvent.type(screen.getByRole('textbox', {name: 'Name'}), 'Crew Ltd');
        await userEvent.click(await screen.findByRole('checkbox', {name: 'demogroup'}));
        await userEvent.click(screen.getByRole('button', {name: 'Add label'}));
        await userEvent.type(screen.getByRole('textbox', {name: 'Label 1'}), 'Crew');
        await userEvent.click(screen.getByRole('button', {name: 'Add location'}));
        await userEvent.selectOptions(screen.getByRole('combobox', {name: 'Country 1'}), await screen.findByRole('option', {name: 'Finland'}));
        await userEvent.click(screen.getByRole('button', {name: 'Add group'}));

        await vi.waitFor(() => {
            expect(onCreated).toHaveBeenCalledExactlyOnceWith(ID);
        });

        const post = fetchMock.mock.calls.find(([, init]) => init?.method === 'POST');

        expect(post?.[1]?.body).toBe(JSON.stringify({
            name: 'Crew Ltd',
            notes: null,
            types: ['demogroup'],
            labels: [{id: null, name: 'Crew', display: true}],
            locations: [{country: FINLAND, city: null, primary: true}],
        }));
    });

    it('returns to the catalog on Cancel', () => {
        stubFetchRoutes(new Map([['/api/group-types', json([])], ['/api/countries', json([])]]));
        renderWithQueries(<MemoryRouter><GroupCreator onCreated={vi.fn<(id: string) => void>()}/></MemoryRouter>);

        expect(screen.getByRole('heading', {level: 1, name: 'Add group'})).toBeInTheDocument();
        expect(screen.getByRole('link', {name: 'Cancel'})).toHaveAttribute('href', '/groups');
    });
});
