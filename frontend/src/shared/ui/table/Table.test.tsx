import {describe, expect, it, vi} from 'vitest';
import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {Table, TableBody, TableCell, TableHead, TableHeaderCell, TableRow} from './Table';

describe(Table, () => {
    it('is a table of named columns and rows', () => {
        render(
            <Table label="Releases">
                <TableHead>
                    <TableRow><TableHeaderCell>Title</TableHeaderCell><TableHeaderCell align="center">Year</TableHeaderCell></TableRow>
                </TableHead>
                <TableBody>
                    <TableRow><TableCell>Elite</TableCell><TableCell align="center">1985</TableCell></TableRow>
                </TableBody>
            </Table>,
        );

        expect(screen.getByRole('table', {name: 'Releases'})).toBeInTheDocument();
        expect(screen.getAllByRole('columnheader').map((header) => header.textContent)).toStrictEqual(['Title', 'Year']);
        expect(screen.getAllByRole('cell').map((cell) => cell.textContent)).toStrictEqual(['Elite', '1985']);
    });

    it('sorts by a column through its header', async () => {
        const onSort = vi.fn<() => void>();
        render(
            <Table label="Releases">
                <TableHead><TableRow><TableHeaderCell sort="ascending" onSort={onSort}>Title</TableHeaderCell></TableRow></TableHead>
            </Table>,
        );

        await userEvent.click(screen.getByRole('button', {name: 'Title'}));

        expect(onSort).toHaveBeenCalledExactlyOnceWith();
        expect(screen.getByRole('columnheader', {name: 'Title'})).toHaveAttribute('aria-sort', 'ascending');
    });

    it('tells a sortable column not sorted now', () => {
        render(
            <Table label="Releases">
                <TableHead><TableRow><TableHeaderCell sort="none" onSort={vi.fn<() => void>()}>Year</TableHeaderCell></TableRow></TableHead>
            </Table>,
        );

        expect(screen.getByRole('columnheader', {name: 'Year'})).toHaveAttribute('aria-sort', 'none');
    });

    it('has no sort button on a column that does not sort', () => {
        render(
            <Table label="Releases">
                <TableHead><TableRow><TableHeaderCell>Version</TableHeaderCell></TableRow></TableHead>
            </Table>,
        );

        expect(screen.queryByRole('button')).not.toBeInTheDocument();
        expect(screen.getByRole('columnheader', {name: 'Version'})).not.toHaveAttribute('aria-sort');
    });
});
