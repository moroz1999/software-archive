import type {ReactNode} from 'react';
import {classNames} from '@/shared/lib/class-names';
import styles from './Badge.module.css';

export type BadgeVariant = 'primary' | 'secondary';

export interface BadgeProps {
    readonly children: ReactNode;
    readonly variant?: BadgeVariant;
    /** Makes the badge a button. */
    readonly onClick?: () => void;
    /** External contour only; see docs/styles.md. */
    readonly className?: string;
}

const variantClass: Readonly<Record<BadgeVariant, string>> = {primary: styles.primary, secondary: styles.secondary};

/** A short label over or next to content (a platform, a count). */
export function Badge({children, variant = 'primary', onClick, className}: BadgeProps) {
    if (onClick === undefined) {
        return <span className={classNames(styles.badge, variantClass[variant], className)}>{children}</span>;
    }
    return (
        <button
            type="button"
            className={classNames(styles.badge, variantClass[variant], styles.clickable, className)}
            onClick={() => {
                onClick();
            }}
        >
            {children}
        </button>
    );
}
