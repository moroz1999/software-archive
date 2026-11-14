import {type ReactNode, useId} from 'react';
import {classNames} from '@/shared/lib/class-names';
import {Text} from '../typography';
import {RequiredMark} from './RequiredMark';
import styles from './Form.module.css';

/** What a field hands to its control: wire these props into an `Input`, a `Textarea` or a `Select`. */
export interface FieldControl {
    readonly id: string;
    readonly invalid: boolean;
    readonly describedBy: string | undefined;
}

export interface FormFieldProps {
    readonly label: string;
    /** A value has to be given: a star after the label. */
    readonly required?: boolean;
    /** A format error of the value, shown under the control. */
    readonly error?: string | undefined;
    /** What to know about the value, shown under the control. */
    readonly hint?: string | undefined;
    /** Spans every column of a two-column section. */
    readonly fullWidth?: boolean;
    readonly children: (control: FieldControl) => ReactNode;
    /** External contour only; see docs/styles.md. */
    readonly className?: string;
}

/** A form control with its label above and its hint and error below, tied together for assistive technology. */
export function FormField({label, required = false, error, hint, fullWidth = false, children, className}: FormFieldProps) {
    const id = useId();
    const errorId = `${id}-error`;
    const hintId = `${id}-hint`;
    const invalid = error !== undefined;
    const described = [hint === undefined ? null : hintId, invalid ? errorId : null].filter((part) => part !== null).join(' ');

    return (
        <div className={classNames(styles.field, fullWidth && styles.fullWidth, className)}>
            {/* The star stays beside the label, not in it: the name of the control is the label alone. */}
            <div className={styles.label}>
                <label htmlFor={id}>
                    <Text as="span" variant="label" tone="strong">{label}</Text>
                </label>
                {required && <RequiredMark/>}
            </div>
            <div className={styles.control}>
                {children({id, invalid, describedBy: described === '' ? undefined : described})}
                {hint !== undefined && <Text as="span" id={hintId} variant="caption" tone="muted">{hint}</Text>}
                {invalid && <Text as="span" id={errorId} variant="caption" tone="danger">{error}</Text>}
            </div>
        </div>
    );
}
