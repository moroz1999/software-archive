import {useTranslation} from 'react-i18next';
import {TextLink} from '../typography';
import styles from './Breadcrumbs.module.css';

/** A step of the way to the page: what it is called and where it leads. */
export interface Crumb {
    readonly label: string;
    readonly href?: string;
}

export interface BreadcrumbsProps {
    /** From the top down to the page itself, the last step. */
    readonly steps: readonly Crumb[];
}

/** The way from the top of the site down to the page, each step above it leading there. */
export function Breadcrumbs({steps}: BreadcrumbsProps) {
    const {t} = useTranslation();
    return (
        <nav aria-label={t('breadcrumbs.label')} className={styles.trail}>
            <ol className={styles.steps}>
                {steps.map((step, index) => {
                    const last = index === steps.length - 1;
                    return (
                        <li key={`${step.label}:${step.href ?? ''}`} className={styles.step}>
                            {last || step.href === undefined
                                ? <span aria-current={last ? 'page' : undefined} className={styles.current}>{step.label}</span>
                                : <TextLink href={step.href} tone="muted-link">{step.label}</TextLink>}
                        </li>
                    );
                })}
            </ol>
        </nav>
    );
}
