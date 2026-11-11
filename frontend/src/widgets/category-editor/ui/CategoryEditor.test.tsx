import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {MemoryRouter} from 'react-router';
import {toCategoryId} from '@/entities/categories';
import {json, noContent, renderWithQueries, stubFetchAround} from '@/shared/lib/test';
import {CategoryEditor} from './CategoryEditor';

const GAMES = '00000000-0000-0000-0006-000000000001';
const ACTION = '00000000-0000-0000-0006-000000000002';
const TREE = [{id: GAMES, title: 'Games', children: [{id: ACTION, title: 'Action', children: []}]}];

/** What the editor loads besides the category: the tree to choose the parent from. */
const AROUND = new Map([['/api/categories', json(TREE)]]);

function renderEditor(onSaved = vi.fn<() => void>(), onDeleted = vi.fn<() => void>()) {
    renderWithQueries(<MemoryRouter><CategoryEditor id={toCategoryId(ACTION)} onSaved={onSaved} onDeleted={onDeleted}/></MemoryRouter>);
}

describe(CategoryEditor, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('shows a skeleton, then the form with the category and its parent', async () => {
        stubFetchAround(AROUND, json({id: ACTION, title: 'Action', parentId: GAMES}));
        renderEditor();

        expect(screen.getByTestId('category-editor-skeleton')).toBeInTheDocument();
        await expect(screen.findByRole('textbox', {name: 'Title'})).resolves.toHaveValue('Action');
        await expect(screen.findByRole('option', {name: 'Games', selected: true})).resolves.toBeInTheDocument();
    });

    it('puts Delete next to the title and returns to the categories on Cancel', async () => {
        stubFetchAround(AROUND, json({id: ACTION, title: 'Action', parentId: GAMES}));
        renderEditor();

        await screen.findByRole('textbox', {name: 'Title'});

        expect(screen.getByRole('heading', {level: 1, name: 'Edit category'})).toBeInTheDocument();
        expect(screen.getByRole('button', {name: 'Delete'})).toBeInTheDocument();
        expect(screen.getByRole('link', {name: 'Cancel'})).toHaveAttribute('href', '/categories');
    });

    it('saves the category', async () => {
        const fetchMock = stubFetchAround(AROUND, json({id: ACTION, title: 'Action', parentId: GAMES}), noContent());
        const onSaved = vi.fn<() => void>();
        renderEditor(onSaved);

        await userEvent.type(await screen.findByRole('textbox', {name: 'Title'}), ' games');
        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        await vi.waitFor(() => {
            expect(onSaved).toHaveBeenCalledExactlyOnceWith();
        });

        expect(fetchMock.mock.calls.find(([, init]) => init?.method === 'PUT')?.[1]?.body).toBe(`{"title":"Action games","parentId":"${GAMES}"}`);
    });

    it('reports the category deleted', async () => {
        stubFetchAround(AROUND, json({id: ACTION, title: 'Action', parentId: GAMES}), noContent());
        const onDeleted = vi.fn<() => void>();
        renderEditor(vi.fn<() => void>(), onDeleted);

        await screen.findByRole('textbox', {name: 'Title'});
        await userEvent.click(screen.getByRole('button', {name: 'Delete'}));
        await userEvent.click(screen.getByRole('button', {name: 'Delete for good'}));

        await vi.waitFor(() => {
            expect(onDeleted).toHaveBeenCalledExactlyOnceWith();
        });
    });
});
