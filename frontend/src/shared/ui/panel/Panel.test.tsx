import {describe, expect, it} from 'vitest';
import {render, screen} from '@testing-library/react';
import {MemoryRouter} from 'react-router';
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

    it('with a link is one link over its whole body, named by the heading that names the panel', () => {
        render(
            <MemoryRouter>
                <Panel as="article" labelledBy="title" href="/software/1">
                    <h3 id="title">Elite</h3>
                    <p>1985</p>
                </Panel>
            </MemoryRouter>,
        );

        const link = screen.getByRole('link', {name: 'Elite'});

        expect(link).toHaveAttribute('href', '/software/1');
        expect(link).toHaveTextContent('1985');
        expect(screen.getByRole('article', {name: 'Elite'})).toContainElement(link);
    });

    it('renders as an article', () => {
        render(<Panel as="article">Body</Panel>);

        expect(screen.getByRole('article')).toHaveTextContent('Body');
    });

    it('puts its footer after a linked body, outside the link', () => {
        render(<MemoryRouter><Panel title="Elite" href="/software/1" footer={<button type="button">Vote</button>}>Body</Panel></MemoryRouter>);

        expect(screen.getByRole('link', {name: 'Elite'})).not.toContainElement(screen.getByRole('button', {name: 'Vote'}));
    });
});
