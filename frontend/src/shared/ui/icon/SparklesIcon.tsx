import {classNames} from '@/shared/lib/class-names';
import type {IconProps} from './SearchIcon';
import styles from './Icon.module.css';

/** Three sparkles, the mark of the AI assistant, in the color of the text around it; decorative, the control names itself. */
export function SparklesIcon({className}: IconProps) {
    return (
        <svg viewBox="0 0 24 24" aria-hidden="true" focusable="false" className={classNames(styles.icon, className)}>
            <path
                fill="currentColor"
                d="M19 9l1.25-2.75L23 5l-2.75-1.25L19 1l-1.25 2.75L15 5l2.75 1.25L19 9zm-7.5.5L9 4 6.5 9.5 1 12l5.5 2.5L9 20l2.5-5.5L17 12l-5.5-2.5zM19 15l-1.25 2.75L15 19l2.75 1.25L19 23l1.25-2.75L23 19l-2.75-1.25L19 15z"
            />
        </svg>
    );
}
