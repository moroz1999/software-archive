import type {ReactNode} from 'react';
import {classNames} from '@/shared/lib/class-names';
import styles from './ItemRow.module.css';

export interface ItemRowProps {
    /** The fields of the item: they share the width of the row. */
    readonly children: ReactNode;
    /** How many fields share the width; on phones they stack. */
    readonly columns?: 1 | 2;
    /** Controls after the fields, such as a mark and a remove button: as wide as they need. */
    readonly trailing?: ReactNode;
    /** What is wrong with the item, under it. */
    readonly error?: ReactNode;
}

/** One item of an editable list on one line: its fields take the room, its controls follow them. */
export function ItemRow({children, columns = 1, trailing, error}: ItemRowProps) {
    return (
        <div className={styles.item}>
            <div className={styles.row}>
                <div className={classNames(styles.fields, columns === 2 && styles.fields2)}>{children}</div>
                {trailing !== undefined && <div className={styles.trailing}>{trailing}</div>}
            </div>
            {error}
        </div>
    );
}
