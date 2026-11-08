import {describe, expect, it} from 'vitest';
import {render} from '@testing-library/react';
import {Inset} from './Inset';

describe(Inset, () => {
    it('pads all sides with the medium padding by default', () => {
        const {container} = render(<Inset>Content</Inset>);

        expect(container.firstElementChild).toHaveClass('inset', 'paddingMd', 'sideAll');
    });

    it('pads only the requested sides', () => {
        const {container} = render(<Inset padding="xl" side="block">Content</Inset>);

        expect(container.firstElementChild).toHaveClass('paddingXl', 'sideBlock');
    });
});
