import {classNames} from '@/shared/lib/class-names';
import type {IconProps} from './SearchIcon';
import styles from './Icon.module.css';

/** A gamepad, in the color of the text around it; decorative, what it marks names itself. */
export function GameIcon({className}: IconProps) {
    return (
        <svg viewBox="0 0 24 24" aria-hidden="true" focusable="false" className={classNames(styles.icon, className)}>
            <rect x="2" y="7" width="20" height="11" rx="5" fill="none" stroke="currentColor" strokeWidth="2"/>
            <path d="M7 10.5v4M5 12.5h4" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
            <circle cx="16" cy="11.5" r="1" fill="currentColor"/>
            <circle cx="18" cy="13.5" r="1" fill="currentColor"/>
        </svg>
    );
}
