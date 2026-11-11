import {type ReactNode, useId} from 'react';
import {classNames} from '@/shared/lib/class-names';
import {Stack} from '../stack';
import {Text} from '../typography';
import styles from './FieldGroup.module.css';

export interface FieldGroupProps {
    /** Names the group for everyone: its legend. */
    readonly legend: string;
    /** What is wrong with the group as a whole, shown under it. */
    readonly error?: string | undefined;
    readonly children: ReactNode;
    /** External contour only; see docs/styles.md. */
    readonly className?: string;
}

/** Controls that belong together under one legend, with an error of them all tied to the group. */
export function FieldGroup({legend, error, children, className}: FieldGroupProps) {
    const errorId = `${useId()}-error`;
    const invalid = error !== undefined;
    return (
        <fieldset aria-describedby={invalid ? errorId : undefined} className={classNames(styles.group, className)}>
            <legend className={styles.legend}><Text as="span" variant="label">{legend}</Text></legend>
            <Stack gap="md">
                {children}
                {invalid && <Text as="span" id={errorId} variant="caption" tone="danger">{error}</Text>}
            </Stack>
        </fieldset>
    );
}
