import {describe, expect, it} from 'vitest';
import {render, screen} from '@testing-library/react';
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
});
