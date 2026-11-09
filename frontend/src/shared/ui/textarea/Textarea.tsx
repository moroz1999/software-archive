import {classNames} from '@/shared/lib/class-names';
import styles from './Textarea.module.css';

export interface TextareaProps {
    readonly id: string;
    readonly value: string;
    /** The new text, not the event. */
    readonly onChange: (value: string) => void;
    /** Visible lines before it scrolls. */
    readonly rows?: number;
    /** Accessible name when no label points to the textarea. */
    readonly ariaLabel?: string | undefined;
    readonly invalid?: boolean | undefined;
    /** Id of the element describing the textarea, such as its error. */
    readonly describedBy?: string | undefined;
    readonly disabled?: boolean;
    /** External contour only; see docs/styles.md. */
    readonly className?: string;
}

/** Multi-line text input, resizable vertically. */
export function Textarea({id, value, onChange, rows = 4, ariaLabel, invalid = false, describedBy, disabled, className}: TextareaProps) {
    return (
        <textarea
            id={id}
            value={value}
            rows={rows}
            onChange={(event) => {
                onChange(event.target.value);
            }}
            aria-label={ariaLabel}
            aria-invalid={invalid}
            aria-describedby={describedBy}
            disabled={disabled}
            className={classNames(styles.textarea, invalid && styles.invalid, className)}
        />
    );
}
