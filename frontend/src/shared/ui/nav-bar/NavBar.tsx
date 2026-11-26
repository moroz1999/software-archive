import type {ReactNode} from 'react';
import {Link, useLocation} from 'react-router';
import {classNames} from '@/shared/lib/class-names';
import {Inline} from '@/shared/ui/inline';
import styles from './NavBar.module.css';

interface NavBarBrand {
    readonly src: string;
    readonly darkSrc: string;
    readonly alt: string;
    readonly width: number;
    readonly height: number;
}

export interface NavBarProps {
    /** Accessible name of the navigation. */
    readonly label: string;
    readonly children: ReactNode;
    /** Branding before the section links. */
    readonly brand?: NavBarBrand;
    /** Controls at the end of the bar, apart from the links. */
    readonly actions?: ReactNode;
}

/** The bar of the app's sections over every page: a row of section links within the page width, controls at its end. */
export function NavBar({label, children, brand, actions}: NavBarProps) {
    return (
        <header className={styles.bar}>
            <div className={styles.row}>
                <Inline align="center" gap="lg" wrap>
                    {brand !== undefined && (
                        <picture>
                            <source media="(prefers-color-scheme: dark)" srcSet={brand.darkSrc}/>
                            <img className={styles.brandImage} src={brand.src} alt={brand.alt} width={brand.width} height={brand.height}/>
                        </picture>
                    )}
                    <nav aria-label={label}>
                        <ul className={styles.list}>{children}</ul>
                    </nav>
                </Inline>
                {actions !== undefined && <div className={styles.actions}>{actions}</div>}
            </div>
        </header>
    );
}

export interface NavBarLinkProps {
    readonly href: string;
    /** Current only on its own address, not on the addresses below it. */
    readonly end?: boolean;
    /** Another path prefix whose pages belong to this section. */
    readonly activePath?: string;
    readonly children: ReactNode;
}

/** A section of the bar; marked as current on its address and the addresses below it. */
export function NavBarLink({href, end = false, activePath, children}: NavBarLinkProps) {
    const {pathname} = useLocation();
    const section = activePath ?? href;
    const active = pathname === section || (!end && pathname.startsWith(`${section}/`));
    return (
        <li>
            <Link to={href} aria-current={active ? 'page' : undefined} className={classNames(styles.link, active && styles.active)}>
                {children}
            </Link>
        </li>
    );
}
