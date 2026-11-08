import {describe, expect, it} from 'vitest';
import {render, screen} from '@testing-library/react';
import {CardGrid} from './CardGrid';

describe(CardGrid, () => {
    it('lays cards out as a list of fixed-width tracks', () => {
        render(<CardGrid><li>Card</li></CardGrid>);

        expect(screen.getByRole('list')).toHaveClass('grid');
        expect(screen.getByRole('listitem')).toHaveTextContent('Card');
    });
});
