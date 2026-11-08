import {describe, expect, it} from 'vitest';
import {render, screen} from '@testing-library/react';
import {Inline} from './Inline';

describe(Inline, () => {
    it('lays children out in a non-wrapping row with the medium gap by default', () => {
        const {container} = render(<Inline><span>One</span></Inline>);

        expect(container.firstElementChild).toHaveClass('inline', 'gapMd');
        expect(container.firstElementChild).not.toHaveClass('wrap');
    });

    it('applies gap, alignment, justification and wrapping', () => {
        const {container} = render(<Inline gap="xs" align="baseline" justify="between" wrap><span>One</span></Inline>);

        expect(container.firstElementChild).toHaveClass('gapXs', 'alignBaseline', 'justifyBetween', 'wrap');
    });

    it('renders as a list', () => {
        render(<Inline as="ul"><li>One</li></Inline>);

        expect(screen.getByRole('list')).toHaveClass('inline');
    });
});
