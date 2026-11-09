import {describe, expect, it} from 'vitest';
import {render, screen, within} from '@testing-library/react';
import {MemoryRouter} from 'react-router';
import {Pagination} from './Pagination';

const hrefOf = (page: number) => `/authors?page=${String(page + 1)}`;

function renderPagination(page: number, totalPages: number, disabled = false) {
    render(<MemoryRouter><Pagination page={page} totalPages={totalPages} hrefOf={hrefOf} disabled={disabled}/></MemoryRouter>);
}

function pageItems(): string[] {
    return within(screen.getByRole('navigation', {name: 'Pages'})).getAllByRole('listitem').map((item) => item.textContent);
}

describe(Pagination, () => {
    it('shows nothing for a single page', () => {
        renderPagination(0, 1);

        expect(screen.queryByRole('navigation')).not.toBeInTheDocument();
    });

    it('lists every page when there are few', () => {
        renderPagination(1, 3);

        expect(pageItems()).toStrictEqual(['Previous', '1', '2', '3', 'Next']);
        expect(screen.getByRole('link', {name: '3'})).toHaveAttribute('href', '/authors?page=3');
    });

    it('marks the current page, which is no link', () => {
        renderPagination(1, 3);

        expect(screen.getByText('2')).toHaveAttribute('aria-current', 'page');
        expect(screen.queryByRole('link', {name: '2'})).not.toBeInTheDocument();
    });

    it('shows the first, the last and the neighbours of the current page, the gaps as ellipses', () => {
        renderPagination(9, 20);

        expect(pageItems()).toStrictEqual(['Previous', '1', '…', '9', '10', '11', '…', '20', 'Next']);
    });

    it('leads to the previous and the next page', () => {
        renderPagination(4, 20);

        expect(screen.getByRole('link', {name: 'Previous'})).toHaveAttribute('href', '/authors?page=4');
        expect(screen.getByRole('link', {name: 'Next'})).toHaveAttribute('href', '/authors?page=6');
    });

    it('has no previous on the first page and no next on the last', () => {
        renderPagination(0, 2);

        expect(screen.queryByRole('link', {name: 'Previous'})).not.toBeInTheDocument();
        expect(screen.getByText('Previous')).toHaveAttribute('aria-disabled', 'true');

        renderPagination(1, 2);

        expect(screen.getAllByText('Next').at(-1)).toHaveAttribute('aria-disabled', 'true');
    });

    it('locks every link while disabled', () => {
        renderPagination(4, 20, true);

        expect(screen.queryByRole('link')).not.toBeInTheDocument();
    });
});
