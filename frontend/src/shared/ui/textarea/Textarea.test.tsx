import {describe, expect, it, vi} from 'vitest';
import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {Textarea} from './Textarea';

describe(Textarea, () => {
    it('reports typed text as a plain value', async () => {
        const onChange = vi.fn<(value: string) => void>();
        render(<Textarea id="description" ariaLabel="Description" value="" onChange={onChange}/>);

        await userEvent.type(screen.getByRole('textbox', {name: 'Description'}), 'S');

        expect(onChange).toHaveBeenCalledExactlyOnceWith('S');
        expect(screen.getByRole('textbox', {name: 'Description'})).toHaveClass('textarea');
    });
});
