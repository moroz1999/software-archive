import {describe, expect, it, vi} from 'vitest';
import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {Input} from './Input';

describe(Input, () => {
    it('reports typed text as a plain value', async () => {
        const onChange = vi.fn<(value: string) => void>();
        render(<Input id="title" ariaLabel="Title" value="" onChange={onChange}/>);

        await userEvent.type(screen.getByRole('textbox', {name: 'Title'}), 'E');

        expect(onChange).toHaveBeenCalledExactlyOnceWith('E');
    });

    it('marks itself invalid and points to its error', () => {
        render(<Input id="title" ariaLabel="Title" value="" onChange={vi.fn<(value: string) => void>()} invalid describedBy="title-error"/>);

        const input = screen.getByRole('textbox', {name: 'Title'});

        expect(input).toHaveAttribute('aria-invalid', 'true');
        expect(input).toHaveAttribute('aria-describedby', 'title-error');
        expect(input).toHaveClass('input', 'invalid');
    });

    it('takes a date', () => {
        render(<Input id="date" ariaLabel="Release date" type="date" value="1985-09-20" onChange={vi.fn<(value: string) => void>()} disabled/>);

        const input = screen.getByLabelText('Release date');

        expect(input).toHaveAttribute('type', 'date');
        expect(input).toHaveValue('1985-09-20');
        expect(input).toBeDisabled();
    });
});
