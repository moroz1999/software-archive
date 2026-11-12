import {classNames} from '@/shared/lib/class-names';
import type {IconProps} from './SearchIcon';
import styles from './Icon.module.css';

/** A cross, in the color of the text around it; decorative, the control names itself. */
export function CloseIcon({className}: IconProps) {
    return (
        <svg viewBox="0 0 24 24" aria-hidden="true" focusable="false" className={classNames(styles.icon, className)}>
            <path d="M6 6l12 12M18 6L6 18" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
        </svg>
    );
}
