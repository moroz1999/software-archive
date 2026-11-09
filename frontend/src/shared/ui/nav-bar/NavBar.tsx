import type {ReactNode} from 'react';
import {NavLink} from 'react-router';
import {classNames} from '@/shared/lib/class-names';
import styles from './NavBar.module.css';

export interface NavBarProps {
    /** Accessible name of the navigation. */
    readonly label: string;
    readonly children: ReactNode;
}

/** The bar of the app's sections over every page: a row of section links within the page width. */
export function NavBar({label, children}: NavBarProps) {
    return (
        <header className={styles.bar}>
            <nav aria-label={label} className={styles.nav}>
                <ul className={styles.list}>{children}</ul>
            </nav>
        </header>
    );
}

export interface NavBarLinkProps {
    readonly href: string;
    /** Current only on its own address, not on the addresses below it. */
    readonly end?: boolean;
    readonly children: ReactNode;
}

/** A section of the bar; marked as current on its address and the addresses below it. */
export function NavBarLink({href, end = false, children}: NavBarLinkProps) {
    return (
        <li>
            <NavLink to={href} end={end} className={({isActive}) => classNames(styles.link, isActive && styles.active)}>
                {children}
            </NavLink>
        </li>
    );
}
