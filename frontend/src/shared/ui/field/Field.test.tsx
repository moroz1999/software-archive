import {describe, expect, it, vi} from 'vitest';
import {render, screen} from '@testing-library/react';
import {Input} from '../input';
import {Field} from './Field';

describe(Field, () => {
    it('labels its control', () => {
        render(<Field label="Title">{(control) => <Input {...control} value="" onChange={vi.fn<(value: string) => void>()}/>}</Field>);

        expect(screen.getByRole('textbox', {name: 'Title'})).toHaveAttribute('aria-invalid', 'false');
    });

    it('shows its error under the control and ties them together', () => {
        render(<Field label="Title" error="Enter a title.">{(control) => <Input {...control} value="" onChange={vi.fn<(value: string) => void>()}/>}</Field>);

        const input = screen.getByRole('textbox', {name: 'Title'});

        expect(input).toHaveAttribute('aria-invalid', 'true');
        expect(input).toHaveAccessibleDescription('Enter a title.');
    });
});
