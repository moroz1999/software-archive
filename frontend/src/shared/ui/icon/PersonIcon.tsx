import {classNames} from '@/shared/lib/class-names';
import type {IconProps} from './SearchIcon';
import styles from './Icon.module.css';

/** A person, in the color of the text around it; decorative, what it marks names itself. */
export function PersonIcon({className}: IconProps) {
    return (
        <svg viewBox="0 0 24 24" aria-hidden="true" focusable="false" className={classNames(styles.icon, className)}>
            <circle cx="12" cy="8" r="4" fill="none" stroke="currentColor" strokeWidth="2"/>
            <path d="M4 21c0-4.4 3.6-7 8-7s8 2.6 8 7" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
        </svg>
    );
}
