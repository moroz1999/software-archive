import {classNames} from '@/shared/lib/class-names';
import type {IconProps} from './SearchIcon';
import styles from './Icon.module.css';

/** A chevron pointing right, in the color of the text around it; decorative, the control names itself. */
export function ChevronRightIcon({className}: IconProps) {
    return (
        <svg viewBox="0 0 24 24" aria-hidden="true" focusable="false" className={classNames(styles.icon, className)}>
            <path d="M9 5l7 7-7 7" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"/>
        </svg>
    );
}
