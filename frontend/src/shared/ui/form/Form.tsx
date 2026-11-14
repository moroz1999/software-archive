import type {ReactNode, SyntheticEvent} from 'react';
import {classNames} from '@/shared/lib/class-names';
import styles from './Form.module.css';

export type FormColumns = 1 | 2;

export interface FormProps {
    readonly children: ReactNode;
    /** Called on submit; the page never reloads and the browser checks nothing itself. */
    readonly onSubmit: () => void;
    /** How many columns the fields of every section take; a section can set its own. */
    readonly columns?: FormColumns;
    /** Names the form for assistive technology. */
    readonly ariaLabel?: string;
    /** External contour only; see docs/styles.md. */
    readonly className?: string;
}

/** A form of the design system: its message, sections, fields and actions in one column, as wide as forms are. */
export function Form({children, onSubmit, columns = 1, ariaLabel, className}: FormProps) {
    function submit(event: SyntheticEvent) {
        event.preventDefault();
        onSubmit();
    }
    return (
        <form noValidate onSubmit={submit} aria-label={ariaLabel} className={classNames(styles.form, columns === 2 && styles.columns2, className)}>
            {children}
        </form>
    );
}
