import type {ReactNode} from 'react';
import {Inline} from '../inline';
import {Heading} from '../typography';
import styles from './PageLayout.module.css';

export interface PageLayoutProps {
    readonly title: string;
    /** Buttons of the page, next to its title. */
    readonly actions?: ReactNode;
    /** Independent content blocks of the page. */
    readonly children: ReactNode;
}

/** A routed page: its title and actions over its content blocks. */
export function PageLayout({title, actions, children}: PageLayoutProps) {
    return (
        <main className={styles.page}>
            <Inline gap="md" align="center" justify="between" wrap>
                <Heading level={1}>{title}</Heading>
                {actions}
            </Inline>
            <div className={styles.content}>{children}</div>
        </main>
    );
}
