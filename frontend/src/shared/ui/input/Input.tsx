import type {KeyboardEventHandler, Ref} from 'react';
import {classNames} from '@/shared/lib/class-names';
import styles from './Input.module.css';

export interface InputProps {
    readonly id: string;
    readonly value: string;
    /** The new text, not the event. */
    readonly onChange: (value: string) => void;
    readonly type?: 'text' | 'date' | 'search';
    /** A hint shown while the input is empty. */
    readonly placeholder?: string;
    /** Accessible name when no label points to the input. */
    readonly ariaLabel?: string | undefined;
    readonly invalid?: boolean | undefined;
    /** Id of the element describing the input, such as its error. */
    readonly describedBy?: string | undefined;
    readonly disabled?: boolean;
    readonly maxLength?: number;
    /** External contour only; see docs/styles.md. */
    readonly className?: string;
    readonly role?: 'combobox';
    readonly ariaAutocomplete?: 'list';
    readonly ariaControls?: string | undefined;
    readonly ariaExpanded?: boolean | undefined;
    readonly ariaActiveDescendant?: string | undefined;
    readonly onKeyDown?: KeyboardEventHandler<HTMLInputElement>;
    readonly onFocus?: () => void;
    readonly onBlur?: () => void;
    readonly inputRef?: Ref<HTMLInputElement>;
}

/** One-line text or date input. */
export function Input({id, value, onChange, type = 'text', ariaLabel, invalid = false, describedBy, disabled, maxLength, placeholder, className, role, ariaAutocomplete, ariaControls, ariaExpanded, ariaActiveDescendant, onKeyDown, onFocus, onBlur, inputRef}: InputProps) {
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
            role={role}
            aria-autocomplete={ariaAutocomplete}
            aria-controls={ariaControls}
            aria-expanded={ariaExpanded}
            aria-activedescendant={ariaActiveDescendant}
            disabled={disabled}
            maxLength={maxLength}
            placeholder={placeholder}
            className={classNames(styles.input, invalid && styles.invalid, className)}
            onKeyDown={onKeyDown}
            onFocus={onFocus}
            onBlur={onBlur}
            ref={inputRef}
        />
    );
}
