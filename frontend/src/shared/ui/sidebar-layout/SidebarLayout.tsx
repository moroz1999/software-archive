import type {ReactNode} from 'react';
import {classNames} from '@/shared/lib/class-names';
import styles from './SidebarLayout.module.css';

export interface SidebarLayoutProps {
    /** The narrow column: navigation or filters of the content. */
    readonly aside: ReactNode;
    readonly children: ReactNode;
    /** External contour only; see docs/styles.md. */
    readonly className?: string;
}

/** A narrow column on the left of the content; above it on tablets and phones. */
export function SidebarLayout({aside, children, className}: SidebarLayoutProps) {
    return (
        <div className={classNames(styles.layout, className)}>
            <aside className={styles.aside}>{aside}</aside>
            <div className={styles.content}>{children}</div>
        </div>
    );
}
