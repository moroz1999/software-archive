import {classNames} from '@/shared/lib/class-names';
import type {IconProps} from './SearchIcon';
import styles from './Icon.module.css';

/** A pencil, in the color of the text around it; decorative, the control names itself. */
export function PencilIcon({className}: IconProps) {
    return (
        <svg viewBox="0 0 24 24" aria-hidden="true" focusable="false" className={classNames(styles.icon, className)}>
            <path d="M4 20l4-1 11-11-3-3L5 16l-1 4z" fill="none" stroke="currentColor" strokeWidth="2" strokeLinejoin="round"/>
            <line x1="14" y1="7" x2="17" y2="10" stroke="currentColor" strokeWidth="2"/>
        </svg>
    );
}
