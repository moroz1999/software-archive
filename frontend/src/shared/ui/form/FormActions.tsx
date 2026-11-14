import type {ReactNode} from 'react';
import {classNames} from '@/shared/lib/class-names';
import styles from './Form.module.css';

export type FormActionsAlign = 'start' | 'end' | 'between';

export interface FormActionsProps {
    /** The buttons of the form, the main one last. */
    readonly children: ReactNode;
    readonly align?: FormActionsAlign;
}

const alignClass: Readonly<Record<FormActionsAlign, string>> = {
    start: styles.actionsStart, end: styles.actionsEnd, between: styles.actionsBetween,
};

/** The row of the buttons of a form, at its end by default. */
export function FormActions({children, align = 'end'}: FormActionsProps) {
    return <div className={classNames(styles.actions, alignClass[align])}>{children}</div>;
}
