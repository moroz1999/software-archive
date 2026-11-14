import {type ReactNode, useId} from 'react';
import {classNames} from '@/shared/lib/class-names';
import {Text} from '../typography';
import {RequiredMark} from './RequiredMark';
import styles from './Form.module.css';

export interface FormFieldsetProps {
    /** Names the group for everyone: its legend. */
    readonly legend: string;
    /** Something has to be given: a star after the legend. */
    readonly required?: boolean;
    /** What is wrong with the group as a whole, shown under it. */
    readonly error?: string | undefined;
    /** Spans every column of a two-column section. */
    readonly fullWidth?: boolean;
    readonly children: ReactNode;
}

/** Controls that belong together under one legend, with an error of them all tied to the group. */
export function FormFieldset({legend, required = false, error, fullWidth = false, children}: FormFieldsetProps) {
    const errorId = `${useId()}-error`;
    const invalid = error !== undefined;
    return (
        <fieldset aria-describedby={invalid ? errorId : undefined} className={classNames(styles.fieldset, fullWidth && styles.fullWidth)}>
            <legend className={styles.legend}>
                <Text as="span" variant="label" tone="strong">{legend}</Text>
                {required && <RequiredMark/>}
            </legend>
            <div className={styles.control}>
                {children}
                {invalid && <Text as="span" id={errorId} variant="caption" tone="danger">{error}</Text>}
            </div>
        </fieldset>
    );
}
