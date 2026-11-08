import {classNames} from '../../lib';
import styles from './Input.module.css';

export interface InputProps {
    readonly id: string;
    readonly value: string;
    /** The new text, not the event. */
    readonly onChange: (value: string) => void;
    readonly type?: 'text' | 'date';
    /** Accessible name when no label points to the input. */
    readonly ariaLabel?: string | undefined;
    readonly invalid?: boolean | undefined;
    /** Id of the element describing the input, such as its error. */
    readonly describedBy?: string | undefined;
    readonly disabled?: boolean;
    readonly maxLength?: number;
    /** External contour only; see docs/styles.md. */
    readonly className?: string;
}

/** One-line text or date input. */
export function Input({id, value, onChange, type = 'text', ariaLabel, invalid = false, describedBy, disabled, maxLength, className}: InputProps) {
    return (
        <input
            id={id}
            type={type}
            value={value}
            onChange={(event) => {
                onChange(event.target.value);
            }}
            aria-label={ariaLabel}
            aria-invalid={invalid}
            aria-describedby={describedBy}
            disabled={disabled}
            maxLength={maxLength}
            className={classNames(styles.input, invalid && styles.invalid, className)}
        />
    );
}
