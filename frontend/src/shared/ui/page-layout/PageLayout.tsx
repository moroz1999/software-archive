import type {ReactNode} from 'react';
import {SITE_NAME} from '@/shared/config';
import {classNames} from '@/shared/lib/class-names';
import {Breadcrumbs, type Crumb} from '../breadcrumbs';
import {Inline} from '../inline';
import {Heading} from '../typography';
import styles from './PageLayout.module.css';

export interface PageLayoutProps {
    /** The page's level 1 heading: its text, or a skeleton bone while it loads. */
    readonly title: ReactNode;
    /** The name of the browser tab before the name of the site; the title itself when it is text. */
    readonly tabTitle?: string;
    /** The way down to the page, over its title. */
    readonly breadcrumbs?: readonly Crumb[] | undefined;
    /** Buttons of the page, at the end of the title's line. */
    readonly actions?: ReactNode;
    /** The menu of what can be done with what the page shows, right after the text of its title. */
    readonly menu?: ReactNode;
    /** Independent content blocks of the page. */
    readonly children: ReactNode;
}

/** A routed page: its title and actions over its content blocks; it names the browser tab. */
export function PageLayout({title, tabTitle, breadcrumbs, actions, menu, children}: PageLayoutProps) {
    const tab = tabTitle ?? (typeof title === 'string' ? title : undefined);
    return (
        <main className={classNames(styles.page, breadcrumbs !== undefined && styles.withBreadcrumbs)}>
            <title>{tab === undefined ? SITE_NAME : `${tab} — ${SITE_NAME}`}</title>
            <div className={styles.header}>
                {breadcrumbs !== undefined && <Breadcrumbs steps={breadcrumbs}/>}
            <Inline gap="md" align="center" justify="between" wrap>
                {menu === undefined
                    ? <Heading level={1}>{title}</Heading>
                    : (
                        <Inline gap="sm" align="center">
                            <Heading level={1}>{title}</Heading>
                            {menu}
                        </Inline>
                    )}
                {actions}
            </Inline>
            </div>
            <div className={styles.content}>{children}</div>
        </main>
    );
}
