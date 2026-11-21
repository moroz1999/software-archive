import {screen, within} from '@testing-library/react';

/** The steps of the breadcrumbs of the page, each as its text and where it leads; null for the page itself. */
export function breadcrumbSteps(): readonly (readonly [string, string | null])[] {
    const trail = screen.getByRole('navigation', {name: 'Breadcrumbs'});
    return within(trail).getAllByRole('listitem').map((step) => [
        step.textContent,
        within(step).queryByRole('link')?.getAttribute('href') ?? null,
    ]);
}
