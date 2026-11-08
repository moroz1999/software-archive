import {describe, expect, it} from 'vitest';
import {render, screen} from '@testing-library/react';
import {ImagePlaceholder} from './ImagePlaceholder';

describe(ImagePlaceholder, () => {
    it('stands in for a missing 4:3 image under its label', () => {
        render(<ImagePlaceholder label="No screenshot"/>);

        expect(screen.getByRole('img', {name: 'No screenshot'})).toHaveClass('placeholder', 'ratio4x3');
    });

    it('applies the ratio', () => {
        render(<ImagePlaceholder label="No cover" ratio="1:1"/>);

        expect(screen.getByRole('img', {name: 'No cover'})).toHaveClass('ratio1x1');
    });
});
