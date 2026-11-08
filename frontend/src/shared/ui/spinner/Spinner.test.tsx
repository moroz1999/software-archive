import {describe, expect, it} from 'vitest';
import {render, screen} from '@testing-library/react';
import {Spinner} from './Spinner';

describe(Spinner, () => {
    it('announces loading with a medium spinner by default', () => {
        render(<Spinner/>);

        expect(screen.getByRole('status', {name: 'Loading'})).toHaveClass('spinner', 'md');
    });

    it('applies the size and the label', () => {
        render(<Spinner size="xl" label="Refreshing"/>);

        expect(screen.getByRole('status', {name: 'Refreshing'})).toHaveClass('xl');
    });
});
