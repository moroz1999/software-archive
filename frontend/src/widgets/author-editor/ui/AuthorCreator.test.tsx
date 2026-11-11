import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {MemoryRouter} from 'react-router';
import {json, renderWithQueries, stubFetchRoutes} from '@/shared/lib/test';
import {AuthorCreator} from './AuthorCreator';

const ID = '00000000-0000-0000-0003-000000000001';
const FINLAND = '00000000-0000-0000-0002-000000000001';
const HELSINKI = '00000000-0000-0000-0002-000000000011';

describe(AuthorCreator, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('adds an author known by a label only, with a place', async () => {
        const fetchMock = stubFetchRoutes(new Map([
            ['/api/countries', json([{id: FINLAND, name: 'Finland'}])],
            [`/api/countries/${FINLAND}/cities`, json([{id: HELSINKI, name: 'Helsinki'}])],
            ['/api/authors', json({id: ID}, 201)],
        ]));
        const onCreated = vi.fn<(id: string) => void>();
        renderWithQueries(<MemoryRouter><AuthorCreator onCreated={onCreated}/></MemoryRouter>);

        await userEvent.click(screen.getByRole('button', {name: 'Add label'}));
        await userEvent.type(screen.getByRole('textbox', {name: 'Label 1'}), 'Zed');
        await userEvent.click(screen.getByRole('button', {name: 'Add location'}));
        await userEvent.selectOptions(screen.getByRole('combobox', {name: 'Country 1'}), await screen.findByRole('option', {name: 'Finland'}));
        await userEvent.click(screen.getByRole('button', {name: 'Add author'}));

        await vi.waitFor(() => {
            expect(onCreated).toHaveBeenCalledExactlyOnceWith(ID);
        });

        const post = fetchMock.mock.calls.find(([, init]) => init?.method === 'POST');

        expect(post?.[1]?.body).toBe(JSON.stringify({
            realName: null,
            notes: null,
            labels: [{id: null, name: 'Zed', display: true}],
            locations: [{country: FINLAND, city: null, primary: true}],
        }));
    });

    it('returns to the catalog on Cancel', () => {
        stubFetchRoutes(new Map([['/api/countries', json([])]]));
        renderWithQueries(<MemoryRouter><AuthorCreator onCreated={vi.fn<(id: string) => void>()}/></MemoryRouter>);

        expect(screen.getByRole('heading', {level: 1, name: 'Add author'})).toBeInTheDocument();
        expect(screen.getByRole('link', {name: 'Cancel'})).toHaveAttribute('href', '/authors');
    });

    it('asks for the country of a location before sending', async () => {
        const fetchMock = stubFetchRoutes(new Map([['/api/countries', json([{id: FINLAND, name: 'Finland'}])]]));
        renderWithQueries(<MemoryRouter><AuthorCreator onCreated={vi.fn<(id: string) => void>()}/></MemoryRouter>);

        await userEvent.type(screen.getByRole('textbox', {name: 'Real name'}), 'John Doe');
        await userEvent.click(screen.getByRole('button', {name: 'Add location'}));
        await userEvent.click(screen.getByRole('button', {name: 'Add author'}));

        expect(screen.getByRole('combobox', {name: 'Country 1'})).toHaveAccessibleDescription('Choose a country.');
        expect(fetchMock.mock.calls.some(([, init]) => init?.method === 'POST')).toBe(false);
    });
});
