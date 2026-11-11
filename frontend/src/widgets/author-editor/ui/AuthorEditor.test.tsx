import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {MemoryRouter} from 'react-router';
import {json, noContent, renderWithQueries, stubFetchAround} from '@/shared/lib/test';
import {toAuthorId} from '@/entities/authors';
import {AuthorEditor} from './AuthorEditor';

const ID = '00000000-0000-0000-0003-000000000001';
const FINLAND = '00000000-0000-0000-0002-000000000001';
const HELSINKI = '00000000-0000-0000-0002-000000000011';
const ZED_LABEL = '00000000-0000-0000-0004-000000000001';
const ZED = {
    id: ID,
    name: 'Zed',
    realName: 'John Doe',
    notes: null,
    labels: [{id: ZED_LABEL, name: 'Zed', display: true}],
    locations: [{countryId: FINLAND, country: 'Finland', cityId: HELSINKI, city: 'Helsinki', primary: true}],
    groups: [],
};

/** What the locations editor loads besides the author. */
const AROUND = new Map([
    ['/api/countries', json([{id: FINLAND, name: 'Finland'}])],
    [`/api/countries/${FINLAND}/cities`, json([{id: HELSINKI, name: 'Helsinki'}])],
]);

function renderEditor(onSaved = vi.fn<() => void>(), onDeleted = vi.fn<() => void>()) {
    renderWithQueries(<MemoryRouter><AuthorEditor id={toAuthorId(ID)} onSaved={onSaved} onDeleted={onDeleted}/></MemoryRouter>);
}

describe(AuthorEditor, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('shows a skeleton, then the form with the author, their labels and locations, and a delete button', async () => {
        stubFetchAround(AROUND, json(ZED));
        renderEditor();

        expect(screen.getByTestId('author-editor-skeleton')).toBeInTheDocument();
        await expect(screen.findByRole('textbox', {name: 'Real name'})).resolves.toHaveValue('John Doe');
        expect(screen.getByRole('textbox', {name: 'Label 1'})).toHaveValue('Zed');
        await expect(screen.findByRole('option', {name: 'Helsinki', selected: true})).resolves.toBeInTheDocument();
        expect(screen.getByRole('button', {name: 'Delete'})).toBeInTheDocument();
    });

    it('puts Delete next to the title and returns to the author on Cancel', async () => {
        stubFetchAround(AROUND, json(ZED));
        renderEditor();

        await screen.findByRole('textbox', {name: 'Real name'});

        expect(screen.getByRole('heading', {level: 1, name: 'Edit author'})).toBeInTheDocument();
        expect(screen.getByRole('link', {name: 'Cancel'})).toHaveAttribute('href', `/authors/${ID}`);
    });

    it('saves the whole author with their labels and locations', async () => {
        const fetchMock = stubFetchAround(AROUND, json(ZED), noContent());
        const onSaved = vi.fn<() => void>();
        renderEditor(onSaved);

        await screen.findByRole('textbox', {name: 'Real name'});
        await userEvent.click(screen.getByRole('button', {name: 'Add label'}));
        await userEvent.type(screen.getByRole('textbox', {name: 'Label 2'}), 'Axe');
        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        await vi.waitFor(() => {
            expect(onSaved).toHaveBeenCalledExactlyOnceWith();
        });

        const put = fetchMock.mock.calls.find(([, init]) => init?.method === 'PUT');

        expect(put?.[1]?.body).toBe(JSON.stringify({
            realName: 'John Doe',
            notes: null,
            labels: [{id: ZED_LABEL, name: 'Zed', display: true}, {id: null, name: 'Axe', display: false}],
            locations: [{country: FINLAND, city: HELSINKI, primary: true}],
        }));
    });

    it('says why the labels were refused under the labels', async () => {
        stubFetchAround(AROUND, json(ZED), json({status: 422, code: 'label.not-own', errors: [{field: 'labels', code: 'label.not-own'}]}, 422));
        renderEditor();

        await screen.findByRole('textbox', {name: 'Real name'});
        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        await vi.waitFor(() => {
            expect(screen.getByRole('group', {name: 'Labels'})).toHaveAccessibleDescription('One of the labels belongs to someone else. Reload and try again.');
        });
    });

    it('says why the locations were refused under the locations', async () => {
        stubFetchAround(AROUND, json(ZED), json({status: 422, code: 'location.unknown', errors: [{field: 'locations', code: 'location.unknown'}]}, 422));
        renderEditor();

        await screen.findByRole('textbox', {name: 'Real name'});
        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        await vi.waitFor(() => {
            expect(screen.getByRole('group', {name: 'Locations'})).toHaveAccessibleDescription('One of the places is not in the archive any more. Reload and try again.');
        });
    });

    it('reports the author deleted', async () => {
        stubFetchAround(AROUND, json(ZED), noContent());
        const onDeleted = vi.fn<() => void>();
        renderEditor(vi.fn<() => void>(), onDeleted);

        await screen.findByRole('textbox', {name: 'Real name'});
        await userEvent.click(screen.getByRole('button', {name: 'Delete'}));
        await userEvent.click(screen.getByRole('button', {name: 'Delete for good'}));

        await vi.waitFor(() => {
            expect(onDeleted).toHaveBeenCalledExactlyOnceWith();
        });
    });
});
