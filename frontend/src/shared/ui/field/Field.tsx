import {type ReactNode, useId} from 'react';
import {Stack} from '../stack';
import {Label, Text} from '../typography';

/** What a field hands to its control: wire these props into an `Input` or `Textarea`. */
export interface FieldControl {
    readonly id: string;
    readonly invalid: boolean;
    readonly describedBy: string | undefined;
}

export interface FieldProps {
    readonly label: string;
    /** A format error of the value, shown under the control. */
    readonly error?: string | undefined;
    /** What to know about the value, shown under the control. */
    readonly hint?: string | undefined;
    readonly children: (control: FieldControl) => ReactNode;
    /** External contour only; see docs/styles.md. */
    readonly className?: string;
}

/** A form control with its label above and its hint and error below, tied together for assistive technology. */
export function Field({label, error, hint, children, className}: FieldProps) {
    const id = useId();
    const errorId = `${id}-error`;
    const hintId = `${id}-hint`;
    const invalid = error !== undefined;
    const described = [hint === undefined ? null : hintId, invalid ? errorId : null].filter((part) => part !== null).join(' ');

    return (
        <Stack gap="xs" {...(className === undefined ? {} : {className})}>
            <Label htmlFor={id}>{label}</Label>
            {children({id, invalid, describedBy: described === '' ? undefined : described})}
            {hint !== undefined && <Text as="span" id={hintId} variant="caption" tone="muted">{hint}</Text>}
            {invalid && <Text as="span" id={errorId} variant="caption" tone="danger">{error}</Text>}
        </Stack>
    );
}
