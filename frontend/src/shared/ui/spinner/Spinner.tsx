import {classNames} from '@/shared/lib/class-names';
import styles from './Spinner.module.css';

export type SpinnerSize = 'xs' | 'sm' | 'md' | 'lg' | 'xl';

export interface SpinnerProps {
    readonly size?: SpinnerSize;
    /** What is being waited for, announced to assistive technology. */
    readonly label?: string;
    /** External contour only; see docs/styles.md. */
    readonly className?: string;
}

const sizeClass: Readonly<Record<SpinnerSize, string>> = {
    xs: styles.xs, sm: styles.sm, md: styles.md, lg: styles.lg, xl: styles.xl,
};

/** Three nested rings turning while something loads. */
export function Spinner({size = 'md', label = 'Loading', className}: SpinnerProps) {
    return (
        <div role="status" aria-label={label} className={classNames(styles.spinner, sizeClass[size], className)}>
            <span className={styles.ring}/>
            <span className={styles.ring}/>
            <span className={styles.ring}/>
        </div>
    );
}
