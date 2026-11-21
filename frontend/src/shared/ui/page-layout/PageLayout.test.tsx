import {describe, expect, it} from 'vitest';
import {render, screen} from '@testing-library/react';
import {MemoryRouter} from 'react-router';
import {PageLayout} from './PageLayout';

describe(PageLayout, () => {
    it('shows the page title over the content', () => {
        render(<PageLayout title="Software">Content</PageLayout>);

        expect(screen.getByRole('main')).toHaveTextContent('Content');
        expect(screen.getByRole('heading', {level: 1, name: 'Software'})).toBeInTheDocument();
    });

    it('shows page actions next to the title', () => {
        render(<PageLayout title="Software" actions={<span>Add</span>}>Content</PageLayout>);

        expect(screen.getByRole('heading', {level: 1, name: 'Software'})).toBeInTheDocument();
        expect(screen.getByText('Add')).toBeInTheDocument();
    });

    it('shows the menu of the page right after the text of its title', () => {
        render(<PageLayout title="Elite" menu={<span>Menu</span>}>Content</PageLayout>);

        const heading = screen.getByRole('heading', {level: 1, name: 'Elite'});

        expect(heading.nextElementSibling).toBe(screen.getByText('Menu'));
    });

    it('shows the way down to the page over its title', () => {
        render(<MemoryRouter><PageLayout title="Zed" breadcrumbs={[{label: 'Authors', href: '/authors'}, {label: 'Zed'}]}>Content</PageLayout></MemoryRouter>);

        const trail = screen.getByRole('navigation', {name: 'Breadcrumbs'});

        expect(trail.compareDocumentPosition(screen.getByRole('heading', {level: 1, name: 'Zed'}))).toBe(Node.DOCUMENT_POSITION_FOLLOWING);
    });

    it('shows no breadcrumbs on a page without them', () => {
        render(<PageLayout title="Software">Content</PageLayout>);

        expect(screen.queryByRole('navigation', {name: 'Breadcrumbs'})).not.toBeInTheDocument();
    });

    it('names the browser tab after its title and the site', () => {
        render(<PageLayout title="Software">Content</PageLayout>);

        expect(document.title).toBe('Software — Ретротека');
    });

    it('names the browser tab by the name it is given over the title', () => {
        render(<PageLayout title="Elite" tabTitle="Elite (Vector)">Content</PageLayout>);

        expect(document.title).toBe('Elite (Vector) — Ретротека');
    });

    it('names the browser tab after the site alone while the title is not text', () => {
        render(<PageLayout title={<span>Bone</span>}>Content</PageLayout>);

        expect(document.title).toBe('Ретротека');
    });
});
