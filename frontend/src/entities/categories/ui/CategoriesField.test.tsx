import {useState} from 'react';
import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {json, renderWithQueries, stubFetch} from '@/shared/lib/test';
import {CategoryTreeProvider} from '../model/CategoryTreeProvider';
import {CategoriesField} from './CategoriesField';

const GAMES = '00000000-0000-0000-0006-000000000001';
const ACTION = '00000000-0000-0000-0006-000000000002';
const TREE = [
    {id: GAMES, title: 'Games', children: [{id: ACTION, title: 'Action', children: []}]},
    {id: '00000000-0000-0000-0006-000000000004', title: 'Demoscene', children: []},
];

function Held({initial = []}: {readonly initial?: readonly string[]}) {
    const [value, setValue] = useState(initial);
    return <CategoryTreeProvider><CategoriesField value={value} onChange={setValue}/></CategoryTreeProvider>;
}

describe(CategoriesField, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('shows a skeleton, then the whole tree to choose from', async () => {
        stubFetch(json(TREE));
        renderWithQueries(<Held/>);

        expect(screen.getByTestId('categories-field-skeleton')).toBeInTheDocument();
        await expect(screen.findByRole('checkbox', {name: 'Action'})).resolves.not.toBeChecked();
        expect(screen.getByRole('group', {name: 'Categories'})).toContainElement(screen.getByRole('checkbox', {name: 'Demoscene'}));
    });

    it('shows the chosen categories and chooses more', async () => {
        stubFetch(json(TREE));
        renderWithQueries(<Held initial={[ACTION]}/>);

        await expect(screen.findByRole('checkbox', {name: 'Action'})).resolves.toBeChecked();

        await userEvent.click(screen.getByRole('checkbox', {name: 'Games'}));

        expect(screen.getByRole('checkbox', {name: 'Games'})).toBeChecked();
        expect(screen.getByText('2 selected')).toBeInTheDocument();
    });

    it('shows why the categories were refused', async () => {
        stubFetch(json(TREE));
        renderWithQueries(<CategoryTreeProvider>
            <CategoriesField value={[]} onChange={vi.fn<(value: readonly string[]) => void>()} error="Not in the archive."/>
        </CategoryTreeProvider>);

        await expect(screen.findByRole('group', {name: 'Categories'})).resolves.toHaveAccessibleDescription('Not in the archive.');
    });

    it('shows nothing when the tree cannot be loaded', async () => {
        const fetchMock = stubFetch(json({status: 500}, 500));
        renderWithQueries(<Held/>);

        await vi.waitFor(() => {
            expect(fetchMock).toHaveBeenCalledWith('/api/categories', expect.anything());
        });
        await vi.waitFor(() => {
            expect(screen.queryByTestId('categories-field-skeleton')).not.toBeInTheDocument();
        });

        expect(screen.queryByRole('group', {name: 'Categories'})).not.toBeInTheDocument();
    });
});
