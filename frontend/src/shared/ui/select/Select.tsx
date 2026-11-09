import {classNames} from '@/shared/lib/class-names';
import styles from './Select.module.css';

export interface SelectOption {
    readonly value: string;
    readonly label: string;
}

export interface SelectProps {
    readonly id: string;
    readonly value: string;
    /** The chosen value, not the event; empty for the placeholder. */
    readonly onChange: (value: string) => void;
    readonly options: readonly SelectOption[];
    /** The option of no choice, with the empty value. */
    readonly placeholder: string;
    /** Accessible name when no label points to the select. */
    readonly ariaLabel?: string | undefined;
    readonly disabled?: boolean;
    /** External contour only; see docs/styles.md. */
    readonly className?: string;
}

/** A choice of one option out of a list, the placeholder standing for no choice. */
export function Select({id, value, onChange, options, placeholder, ariaLabel, disabled, className}: SelectProps) {
    return (
        <select
            id={id}
            value={value}
            onChange={(event) => {
                onChange(event.target.value);
            }}
            aria-label={ariaLabel}
            disabled={disabled}
            className={classNames(styles.select, className)}
        >
            <option value="">{placeholder}</option>
            {options.map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}
        </select>
    );
}
