import {describe, expect, it} from 'vitest';
import {render, screen} from '@testing-library/react';
import {Grid, GridItem} from './Grid';

describe(Grid, () => {
    it('is a single stretched column with the medium gap by default', () => {
        const {container} = render(<Grid><span>One</span></Grid>);

        expect(container.firstElementChild).toHaveClass(
            'grid', 'desktop1', 'tablet1', 'mobile1', 'rowsAuto', 'rowGapMd', 'columnGapMd', 'alignStretch', 'justifyStretch',
        );
    });

    it('can be a named list', () => {
        render(<Grid as="ul" ariaLabel="Authors"><li>One</li></Grid>);

        expect(screen.getByRole('list', {name: 'Authors'})).toHaveClass('grid');
    });

    it('falls back from mobile to tablet to desktop columns', () => {
        const {container} = render(<Grid desktopColumns={4} tabletColumns={2}><span>One</span></Grid>);

        expect(container.firstElementChild).toHaveClass('desktop4', 'tablet2', 'mobile2');
    });

    it('lets row and column gaps override the gap', () => {
        const {container} = render(<Grid gap="sm" rowGap="xl"><span>One</span></Grid>);

        expect(container.firstElementChild).toHaveClass('rowGapXl', 'columnGapSm');
    });

    it('renders as a list', () => {
        render(<Grid as="ul"><li>One</li></Grid>);

        expect(screen.getByRole('list')).toHaveClass('grid');
    });
});

describe(GridItem, () => {
    it('spans the desktop span on every breakpoint unless told otherwise', () => {
        const {container} = render(<GridItem desktopSpan={3} mobileSpan={1}>One</GridItem>);

        expect(container.firstElementChild).toHaveClass('item', 'spanDesktop3', 'spanTablet3', 'spanMobile1');
    });
});
