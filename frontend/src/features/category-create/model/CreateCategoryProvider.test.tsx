import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {CategoryForm, CategoryTreeProvider} from '@/entities/categories';
import {subscribeSystemErrors, type SystemErrorReport} from '@/shared/api';
import {json, renderWithQueries, stubFetch} from '@/shared/lib/test';
import {CreateCategoryProvider} from './CreateCategoryProvider';

const GAMES = '00000000-0000-0000-0006-000000000001';
const ACTION = '00000000-0000-0000-0006-000000000002';
const TREE = [{id: GAMES, title: 'Games', children: []}];

function showForm(created = json({id: ACTION}, 201), onCreated = vi.fn<(id: string) => void>()) {
    // The tree loads first, then the category is sent.
    const fetchMock = stubFetch(json(TREE), created);
    renderWithQueries(
        <CategoryTreeProvider>
            <CreateCategoryProvider onCreated={onCreated}><CategoryForm submitLabel="Add category"/></CreateCategoryProvider>
        </CategoryTreeProvider>,
    );
    return {fetchMock, onCreated};
}

describe(CreateCategoryProvider, () => {
    const unsubscribers: (() => void)[] = [];

    afterEach(() => {
        vi.unstubAllGlobals();
        unsubscribers.forEach((unsubscribe) => {
            unsubscribe();
        });
        unsubscribers.length = 0;
    });

    it('adds the category under the chosen parent and hands its id on', async () => {
        const {fetchMock, onCreated} = showForm();

        await userEvent.type(screen.getByRole('textbox', {name: 'Title'}), 'Action');
        await userEvent.selectOptions(screen.getByRole('combobox', {name: 'Parent'}), await screen.findByRole('option', {name: 'Games'}));
        await userEvent.click(screen.getByRole('button', {name: 'Add category'}));

        await vi.waitFor(() => {
            expect(onCreated).toHaveBeenCalledExactlyOnceWith(ACTION);
        });

        const post = fetchMock.mock.calls.find(([, init]) => init?.method === 'POST');

        expect(post?.[1]?.body).toBe(`{"title":"Action","parentId":"${GAMES}"}`);
    });

    it('adds a category at the top when no parent is chosen', async () => {
        const {fetchMock, onCreated} = showForm();

        await screen.findByRole('option', {name: 'Games'});

        expect(screen.getByRole('combobox', {name: 'Parent'})).toHaveValue('');

        await userEvent.type(screen.getByRole('textbox', {name: 'Title'}), 'Series');
        await userEvent.click(screen.getByRole('button', {name: 'Add category'}));

        await vi.waitFor(() => {
            expect(onCreated).toHaveBeenCalledExactlyOnceWith(ACTION);
        });

        expect(fetchMock.mock.calls.find(([, init]) => init?.method === 'POST')?.[1]?.body).toBe('{"title":"Series","parentId":null}');
    });

    it('sends nothing without a title and says so under it', async () => {
        const {fetchMock} = showForm();

        await userEvent.click(screen.getByRole('button', {name: 'Add category'}));

        expect(screen.getByRole('textbox', {name: 'Title'})).toHaveAccessibleDescription('Enter a title.');
        expect(fetchMock.mock.calls.some(([, init]) => init?.method === 'POST')).toBe(false);
    });

    it('says why the parent was refused under the parent', async () => {
        showForm(json({status: 422, code: 'category.parent-unknown', errors: [{field: 'parentId', code: 'category.parent-unknown'}]}, 422));

        await userEvent.type(screen.getByRole('textbox', {name: 'Title'}), 'Action');
        await userEvent.selectOptions(screen.getByRole('combobox', {name: 'Parent'}), await screen.findByRole('option', {name: 'Games'}));
        await userEvent.click(screen.getByRole('button', {name: 'Add category'}));

        await vi.waitFor(() => {
            expect(screen.getByRole('combobox', {name: 'Parent'})).toHaveAccessibleDescription(
                'The parent category is not in the archive any more. Reload and try again.',
            );
        });
    });

    it('hands a system error to the error dialog and keeps the form', async () => {
        const reports: SystemErrorReport[] = [];
        unsubscribers.push(subscribeSystemErrors((report) => {
            reports.push(report);
        }));
        showForm(json({status: 500}, 500));

        await userEvent.type(screen.getByRole('textbox', {name: 'Title'}), 'Action');
        await userEvent.click(screen.getByRole('button', {name: 'Add category'}));

        await vi.waitFor(() => {
            expect(reports).toHaveLength(1);
        });

        expect(screen.getByRole('textbox', {name: 'Title'})).toHaveValue('Action');
    });
});
