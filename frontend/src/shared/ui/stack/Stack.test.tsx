import {describe, expect, it} from 'vitest';
import {render, screen} from '@testing-library/react';
import {Stack} from './Stack';

describe(Stack, () => {
    it('can be a named list', () => {
        render(<Stack as="ul" ariaLabel="Aliases"><li>Zed</li></Stack>);

        expect(screen.getByRole('list', {name: 'Aliases'})).toHaveClass('stack');
    });

    it('stacks its children with the medium gap by default', () => {
        const {container} = render(<Stack><span>One</span></Stack>);

        expect(container.firstElementChild).toHaveClass('stack', 'gapMd');
        expect(screen.getByText('One')).toBeInTheDocument();
    });

    it('applies the requested gap and alignment', () => {
        const {container} = render(<Stack gap="xxl" align="center"><span>One</span></Stack>);

        expect(container.firstElementChild).toHaveClass('gapXxl', 'alignCenter');
    });

    it('renders as a list', () => {
        render(<Stack as="ul" gap="none"><li>One</li></Stack>);

        expect(screen.getByRole('list')).toHaveClass('gapNone');
    });
});
