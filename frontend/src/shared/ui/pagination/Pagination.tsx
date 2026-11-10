import type {ReactNode} from 'react';
import {Link} from 'react-router';
import {useTranslation} from 'react-i18next';
import {classNames} from '@/shared/lib/class-names';
import styles from './Pagination.module.css';

export interface PaginationProps {
    /** The current page, counted from 0. */
    readonly page: number;
    readonly totalPages: number;
    /** Where a page, counted from 0, is. */
    readonly hrefOf: (page: number) => string;
    /** Locked while a page loads. */
    readonly disabled?: boolean;
}

type Slot = {readonly kind: 'page'; readonly page: number} | {readonly kind: 'gap'; readonly after: number};

/** The first and the last page and the neighbours of the current one; a gap of one page shows that page. */
function slots(page: number, totalPages: number): Slot[] {
    const shown = [...new Set([0, page - 1, page, page + 1, totalPages - 1])]
        .filter((candidate) => candidate >= 0 && candidate < totalPages)
        .sort((a, b) => a - b);
    const result: Slot[] = [];
    shown.forEach((current, index) => {
        const previous = shown[index - 1];
        if (previous !== undefined && current - previous === 2) {
            result.push({kind: 'page', page: previous + 1});
        } else if (previous !== undefined && current - previous > 2) {
            result.push({kind: 'gap', after: previous});
        }
        result.push({kind: 'page', page: current});
    });
    return result;
}

interface ItemProps {
    readonly href: string | null;
    readonly current?: boolean;
    readonly children: ReactNode;
}

function Item({href, current = false, children}: ItemProps) {
    if (current) {
        return <li><span aria-current="page" className={classNames(styles.item, styles.current)}>{children}</span></li>;
    }
    if (href === null) {
        return <li><span aria-disabled="true" className={classNames(styles.item, styles.disabled)}>{children}</span></li>;
    }
    return <li><Link to={href} className={styles.item}>{children}</Link></li>;
}

/** Page navigation of a paged list; nothing for a single page. */
export function Pagination({page, totalPages, hrefOf, disabled = false}: PaginationProps) {
    const {t} = useTranslation();
    if (totalPages <= 1) {
        return null;
    }
    const hrefFor = (target: number) => (disabled ? null : hrefOf(target));
    return (
        <nav aria-label={t('pagination.label')}>
            <ul className={styles.list}>
                <Item href={page > 0 ? hrefFor(page - 1) : null}>{t('pagination.previous')}</Item>
                {slots(page, totalPages).map((slot) => (slot.kind === 'gap'
                    ? <li key={`gap-${String(slot.after)}`}><span className={styles.gap}>…</span></li>
                    : (
                        <Item key={slot.page} href={hrefFor(slot.page)} current={slot.page === page}>
                            {String(slot.page + 1)}
                        </Item>
                    )))}
                <Item href={page < totalPages - 1 ? hrefFor(page + 1) : null}>{t('pagination.next')}</Item>
            </ul>
        </nav>
    );
}
