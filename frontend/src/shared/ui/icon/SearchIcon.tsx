import {classNames} from '@/shared/lib/class-names';
import styles from './Icon.module.css';

export interface IconProps {
    /** External contour only; see docs/styles.md. */
    readonly className?: string;
}

/** A magnifying glass, in the color of the text around it; decorative, the control names itself. */
export function SearchIcon({className}: IconProps) {
    return (
        <svg viewBox="0 0 24 24" aria-hidden="true" focusable="false" className={classNames(styles.icon, className)}>
            <circle cx="10.5" cy="10.5" r="6.5" fill="none" stroke="currentColor" strokeWidth="2"/>
            <line x1="15.5" y1="15.5" x2="21" y2="21" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
        </svg>
    );
}
