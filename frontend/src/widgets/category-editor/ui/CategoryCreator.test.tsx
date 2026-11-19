import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {MemoryRouter} from 'react-router';
import {json, renderWithQueries, stubFetch} from '@/shared/lib/test';
import {CategoryCreator} from './CategoryCreator';

const GAMES = '00000000-0000-0000-0006-000000000001';
const ACTION = '00000000-0000-0000-0006-000000000002';

describe(CategoryCreator, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('adds a category under a parent of the tree, Cancel returning to the categories', async () => {
        const fetchMock = stubFetch(json([{id: GAMES, title: 'Games', children: []}]), json({id: ACTION}, 201));
        const onCreated = vi.fn<(id: string) => void>();
        renderWithQueries(<MemoryRouter><CategoryCreator onCreated={onCreated}/></MemoryRouter>);

        expect(screen.getByRole('heading', {level: 1, name: 'Add category'})).toBeInTheDocument();
        expect(screen.getByRole('link', {name: 'Cancel'})).toHaveAttribute('href', '/management/categories');

        await userEvent.type(screen.getByRole('textbox', {name: 'Title in Russian'}), 'Экшен');
        await userEvent.selectOptions(screen.getByRole('combobox', {name: 'Parent'}), await screen.findByRole('option', {name: 'Games'}));
        await userEvent.click(screen.getByRole('button', {name: 'Add category'}));

        await vi.waitFor(() => {
            expect(onCreated).toHaveBeenCalledExactlyOnceWith(ACTION);
        });

        expect(fetchMock.mock.calls.find(([, init]) => init?.method === 'POST')?.[1]?.body).toBe(`{"titles":{"ru":"Экшен"},"parentId":"${GAMES}"}`);
    });

    it('needs the title in Russian, not the one in English', () => {
        stubFetch(json([{id: GAMES, title: 'Games', children: []}]));
        renderWithQueries(<MemoryRouter><CategoryCreator onCreated={vi.fn<(id: string) => void>()}/></MemoryRouter>);

        expect(screen.getByRole('textbox', {name: 'Title in Russian'})).toBeRequired();
        expect(screen.getByRole('textbox', {name: 'Title in English'})).not.toBeRequired();
    });

    it('refuses a category without a Russian title before sending it', async () => {
        const fetchMock = stubFetch(json([{id: GAMES, title: 'Games', children: []}]));
        renderWithQueries(<MemoryRouter><CategoryCreator onCreated={vi.fn<(id: string) => void>()}/></MemoryRouter>);

        await userEvent.type(screen.getByRole('textbox', {name: 'Title in English'}), 'Action');
        await userEvent.click(screen.getByRole('button', {name: 'Add category'}));

        expect(screen.getByRole('textbox', {name: 'Title in Russian'})).toHaveAccessibleDescription('Enter a title.');
        expect(fetchMock.mock.calls.filter(([, init]) => init?.method === 'POST')).toHaveLength(0);
    });
});
