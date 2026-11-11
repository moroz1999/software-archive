import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {CategoryForm, CategoryTreeProvider, toCategoryId} from '@/entities/categories';
import {json, noContent, renderWithQueries, stubFetchAround} from '@/shared/lib/test';
import {EditCategoryProvider} from './EditCategoryProvider';

const GAMES = '00000000-0000-0000-0006-000000000001';
const ACTION = '00000000-0000-0000-0006-000000000002';
const SHOOTERS = '00000000-0000-0000-0006-000000000003';
const DEMOSCENE = '00000000-0000-0000-0006-000000000004';
const TREE = [
    {id: GAMES, title: 'Games', children: [{id: ACTION, title: 'Action', children: [{id: SHOOTERS, title: 'Shooters', children: []}]}]},
    {id: DEMOSCENE, title: 'Demoscene', children: []},
];
const CATEGORY = {id: toCategoryId(ACTION), title: 'Action', parentId: toCategoryId(GAMES)};

const AROUND = new Map([['/api/categories', json(TREE)]]);

function renderEditor(onSaved = vi.fn<() => void>()) {
    renderWithQueries(
        <CategoryTreeProvider>
            <EditCategoryProvider category={CATEGORY} onSaved={onSaved}><CategoryForm submitLabel="Save"/></EditCategoryProvider>
        </CategoryTreeProvider>,
    );
}

describe(EditCategoryProvider, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('starts from the category and saves it under another parent', async () => {
        const fetchMock = stubFetchAround(AROUND, noContent());
        const onSaved = vi.fn<() => void>();
        renderEditor(onSaved);

        expect(screen.getByRole('textbox', {name: 'Title'})).toHaveValue('Action');
        await expect(screen.findByRole('option', {name: 'Games', selected: true})).resolves.toBeInTheDocument();

        await userEvent.selectOptions(screen.getByRole('combobox', {name: 'Parent'}), screen.getByRole('option', {name: 'Demoscene'}));
        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        await vi.waitFor(() => {
            expect(onSaved).toHaveBeenCalledExactlyOnceWith();
        });

        expect(fetchMock.mock.calls[0]?.[0]).toBe(`/api/categories/${ACTION}`);
        expect(fetchMock.mock.calls[0]?.[1]).toMatchObject({method: 'PUT', body: `{"title":"Action","parentId":"${DEMOSCENE}"}`});
    });

    it('offers neither the category itself nor its subcategories as its parent', async () => {
        stubFetchAround(AROUND, noContent());
        renderEditor();

        await screen.findByRole('option', {name: 'Games'});

        expect(screen.queryByRole('option', {name: 'Action'})).not.toBeInTheDocument();
        expect(screen.queryByRole('option', {name: 'Shooters'})).not.toBeInTheDocument();
    });

    it('says why the parent was refused under the parent until it is changed', async () => {
        stubFetchAround(AROUND, json({status: 422, code: 'category.parent-inside', errors: [{field: 'parentId', code: 'category.parent-inside'}]}, 422));
        renderEditor();

        await screen.findByRole('option', {name: 'Games'});
        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        await vi.waitFor(() => {
            expect(screen.getByRole('combobox', {name: 'Parent'})).toHaveAccessibleDescription('A category cannot go under itself or its own subcategory.');
        });

        await userEvent.selectOptions(screen.getByRole('combobox', {name: 'Parent'}), screen.getByRole('option', {name: 'Demoscene'}));

        expect(screen.getByRole('combobox', {name: 'Parent'})).not.toHaveAccessibleDescription();
    });
});
