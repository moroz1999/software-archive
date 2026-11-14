import type {ReactNode} from 'react';
import {classNames} from '@/shared/lib/class-names';
import styles from './Form.module.css';

export type FormRowColumns = 2 | 3;

export interface FormRowProps {
    /** How many equal columns the fields of the row take. */
    readonly columns: FormRowColumns;
    readonly children: ReactNode;
}

/** Short related fields side by side across the whole section, stacked on a phone. */
export function FormRow({columns, children}: FormRowProps) {
    return <div className={classNames(styles.row, columns === 2 ? styles.rowColumns2 : styles.rowColumns3)}>{children}</div>;
}
