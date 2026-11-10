import type {ReactNode} from 'react';
import {classNames} from '@/shared/lib/class-names';
import styles from './Table.module.css';

export type TableAlign = 'start' | 'center' | 'end';
export type TableSort = 'ascending' | 'descending' | 'none';

export interface TableProps {
    /** The name of the table for assistive technology. */
    readonly label: string;
    readonly children: ReactNode;
    /** Columns as wide as their headers set them, not as their content: a skeleton and the data line up. */
    readonly fixed?: boolean;
    /** External contour only; see docs/styles.md. */
    readonly className?: string;
}

export interface TableSectionProps {
    readonly children: ReactNode;
}

export interface TableCellProps {
    readonly children?: ReactNode;
    readonly align?: TableAlign;
}

export interface TableHeaderCellProps extends TableCellProps {
    /** How the rows are sorted by this column now; a column that sorts has it. */
    readonly sort?: TableSort;
    /** Sorts the rows by this column, or turns the order round. */
    readonly onSort?: () => void;
    /** The width of the column only; see docs/styles.md. */
    readonly className?: string;
}

const alignClass: Readonly<Record<TableAlign, string>> = {start: styles.start, center: styles.center, end: styles.end};
const SORT_ARROW: Readonly<Record<TableSort, string>> = {ascending: ' ↑', descending: ' ↓', none: ''};

/** Rows of data under named columns, scrolling sideways when wider than its place. */
export function Table({label, children, fixed = false, className}: TableProps) {
    return (
        <div className={classNames(styles.scroll, className)}>
            <table className={classNames(styles.table, fixed && styles.fixed)} aria-label={label}>{children}</table>
        </div>
    );
}

export function TableHead({children}: TableSectionProps) {
    return <thead>{children}</thead>;
}

export function TableBody({children}: TableSectionProps) {
    return <tbody>{children}</tbody>;
}

export function TableRow({children}: TableSectionProps) {
    return <tr className={styles.row}>{children}</tr>;
}

/** The name of a column; a column that sorts names its button and tells its order. */
export function TableHeaderCell({children, align = 'start', sort, onSort, className}: TableHeaderCellProps) {
    return (
        <th scope="col" className={classNames(styles.header, alignClass[align], className)} aria-sort={onSort === undefined ? undefined : sort ?? 'none'}>
            {onSort === undefined
                ? children
                : (
                    <button
                        type="button"
                        className={styles.sort}
                        onClick={() => {
                            onSort();
                        }}
                    >
                        {children}
                        <span aria-hidden="true">{SORT_ARROW[sort ?? 'none']}</span>
                    </button>
                )}
        </th>
    );
}

export function TableCell({children, align = 'start'}: TableCellProps) {
    return <td className={classNames(styles.cell, alignClass[align])}>{children}</td>;
}
