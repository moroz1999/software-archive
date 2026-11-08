import {describe, expect, it} from 'vitest';
import {render, screen} from '@testing-library/react';
import {Panel} from './Panel';

describe(Panel, () => {
    it('is an elevated default surface with medium radius and padding by default', () => {
        const {container} = render(<Panel>Body</Panel>);

        expect(container.firstElementChild).toHaveClass('panel', 'radiusMd', 'paddingMd', 'elevated', 'surfaceDefault');
        expect(container.firstElementChild).not.toHaveClass('bleed');
    });

    it('applies radius, padding, variant, surface and content bleed', () => {
        const {container} = render(<Panel radius="xl" padding="none" variant="flat" surface="muted" contentBleed>Body</Panel>);

        expect(container.firstElementChild).toHaveClass('radiusXl', 'paddingNone', 'flat', 'surfaceMuted', 'bleed');
    });

    it('shows its title as a heading of the requested level and names the region by it', () => {
        render(<Panel as="section" title="Releases" titleLevel={2}>Body</Panel>);

        expect(screen.getByRole('heading', {level: 2, name: 'Releases'})).toBeInTheDocument();
        expect(screen.getByRole('region', {name: 'Releases'})).toHaveTextContent('Body');
    });

    it('uses a level 3 title by default', () => {
        render(<Panel title="Credits">Body</Panel>);

        expect(screen.getByRole('heading', {level: 3, name: 'Credits'})).toBeInTheDocument();
    });

    it('renders as an article', () => {
        render(<Panel as="article">Body</Panel>);

        expect(screen.getByRole('article')).toHaveTextContent('Body');
    });
});
