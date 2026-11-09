import type {ReactNode} from 'react';
import {Link} from 'react-router';
import {classNames} from '@/shared/lib/class-names';
import styles from './NavChips.module.css';

export interface NavChipsProps {
    /** Accessible name of the strip. */
    readonly label: string;
    readonly children: ReactNode;
}

/** A wrapping strip of link chips (A–Z, years) with one of them active. */
export function NavChips({label, children}: NavChipsProps) {
    return (
        <nav aria-label={label}>
            <ul className={styles.list}>{children}</ul>
        </nav>
    );
}

export interface NavChipProps {
    readonly href: string;
    readonly active?: boolean;
    /** Locked: shown, but leads nowhere. */
    readonly disabled?: boolean;
    readonly children: ReactNode;
}

/** A chip of the strip: a link of this app, the active one marked as current. */
export function NavChip({href, active = false, disabled = false, children}: NavChipProps) {
    const chipClass = classNames(styles.chip, active && styles.active, disabled && styles.disabled);
    const current = active ? 'page' : undefined;
    return (
        <li>
            {disabled
                ? <span aria-disabled="true" aria-current={current} className={chipClass}>{children}</span>
                : <Link to={href} aria-current={current} className={chipClass}>{children}</Link>}
        </li>
    );
}
