import {describe, expect, it} from 'vitest';
import {render, screen} from '@testing-library/react';
import {MemoryRouter} from 'react-router';
import {breadcrumbSteps} from '@/shared/lib/test';
import {Breadcrumbs} from '.';

describe(Breadcrumbs, () => {
    it('leads to every step above the page, the page itself named without a link', () => {
        render(<MemoryRouter><Breadcrumbs steps={[{label: 'Software', href: '/software'}, {label: 'Games', href: '/software/category/1'}, {label: 'Elite', href: '/software/2'}]}/></MemoryRouter>);

        expect(breadcrumbSteps()).toStrictEqual([['Software', '/software'], ['Games', '/software/category/1'], ['Elite', null]]);
    });

    it('marks the page itself as the current one', () => {
        render(<MemoryRouter><Breadcrumbs steps={[{label: 'Authors', href: '/authors'}, {label: 'Zed'}]}/></MemoryRouter>);

        expect(screen.getByText('Zed')).toHaveAttribute('aria-current', 'page');
    });
});
