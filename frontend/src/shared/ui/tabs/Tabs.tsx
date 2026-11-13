import type {ReactNode} from 'react';
import {NavLink} from 'react-router';
import {classNames} from '@/shared/lib/class-names';
import styles from './Tabs.module.css';

export interface TabsProps {
    /** Accessible name of the tab navigation. */
    readonly label: string;
    readonly children: ReactNode;
}

/** Links between sibling sections, presented as tabs. */
export function Tabs({label, children}: TabsProps) {
    return (
        <nav aria-label={label}>
            <ul className={styles.list}>{children}</ul>
        </nav>
    );
}

export interface TabProps {
    readonly href: string;
    /** Current only on its own address, not on addresses below it. */
    readonly end?: boolean;
    /** Shown as a future section, but leads nowhere. */
    readonly disabled?: boolean;
    readonly children: ReactNode;
}

/** One section of tab navigation. */
export function Tab({href, end = false, disabled = false, children}: TabProps) {
    return (
        <li>
            {disabled
                ? <span aria-disabled="true" className={classNames(styles.tab, styles.disabled)}>{children}</span>
                : (
                    <NavLink
                        to={href}
                        end={end}
                        className={({isActive}) => classNames(styles.tab, isActive && styles.active)}
                    >
                        {children}
                    </NavLink>
                )}
        </li>
    );
}
