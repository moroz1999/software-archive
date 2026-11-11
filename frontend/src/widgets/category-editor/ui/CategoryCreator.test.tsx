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
        stubFetch(json([{id: GAMES, title: 'Games', children: []}]), json({id: ACTION}, 201));
        const onCreated = vi.fn<(id: string) => void>();
        renderWithQueries(<MemoryRouter><CategoryCreator onCreated={onCreated}/></MemoryRouter>);

        expect(screen.getByRole('heading', {level: 1, name: 'Add category'})).toBeInTheDocument();
        expect(screen.getByRole('link', {name: 'Cancel'})).toHaveAttribute('href', '/categories');

        await userEvent.type(screen.getByRole('textbox', {name: 'Title'}), 'Action');
        await userEvent.selectOptions(screen.getByRole('combobox', {name: 'Parent'}), await screen.findByRole('option', {name: 'Games'}));
        await userEvent.click(screen.getByRole('button', {name: 'Add category'}));

        await vi.waitFor(() => {
            expect(onCreated).toHaveBeenCalledExactlyOnceWith(ACTION);
        });
    });
});
