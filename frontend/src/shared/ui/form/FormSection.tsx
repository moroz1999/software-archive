import {type ReactNode, useId} from 'react';
import {classNames} from '@/shared/lib/class-names';
import {Heading} from '../typography';
import type {FormColumns} from './Form';
import styles from './Form.module.css';

export interface FormSectionProps {
    /** Names the section: its heading. */
    readonly title: string;
    /** The columns of its fields when not those of the form. */
    readonly columns?: FormColumns;
    readonly children: ReactNode;
}

/** A titled block of a form: what belongs together, under a heading with a rule. */
export function FormSection({title, columns, children}: FormSectionProps) {
    const titleId = useId();
    return (
        <section aria-labelledby={titleId} className={styles.section}>
            <Heading level={2} variant="title" id={titleId} className={styles.sectionTitle}>{title}</Heading>
            <div className={classNames(styles.fields, columns === 1 && styles.fieldsColumns1, columns === 2 && styles.fieldsColumns2)}>
                {children}
            </div>
        </section>
    );
}
