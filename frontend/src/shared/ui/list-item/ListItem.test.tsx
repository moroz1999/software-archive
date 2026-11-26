import {describe, expect, it, vi} from 'vitest';
import {render, screen, within} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {ListItem} from './ListItem';

describe(ListItem, () => {
    it('is an item of a list picked by clicking its title', async () => {
        const onPick = vi.fn<() => void>();
        render(<ul><ListItem onPick={onPick}>Дубли меток Alcosoft</ListItem></ul>);

        const item = screen.getByRole('listitem');
        await userEvent.click(within(item).getByRole('button', {name: 'Дубли меток Alcosoft'}));

        expect(onPick).toHaveBeenCalledExactlyOnceWith();
    });

    it('keeps its trailing controls apart from picking it', async () => {
        const onPick = vi.fn<() => void>();
        const onRemove = vi.fn<() => void>();
        render(<ul><ListItem onPick={onPick} trailing={<button type="button" onClick={() => {
            onRemove();
        }}
        >Удалить</button>}>Беседа</ListItem></ul>);

        await userEvent.click(screen.getByRole('button', {name: 'Удалить'}));

        expect(onRemove).toHaveBeenCalledExactlyOnceWith();
        expect(onPick).not.toHaveBeenCalled();
    });
});
