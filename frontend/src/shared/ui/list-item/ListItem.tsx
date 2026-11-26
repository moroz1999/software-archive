import type {ReactNode} from 'react';
import {Text} from '../typography';
import styles from './ListItem.module.css';

export interface ListItemProps {
    /** What the item is called: shown whole, wrapping onto more lines when long. */
    readonly children: ReactNode;
    /** Picks the item, as opening it. */
    readonly onPick: () => void;
    /** Controls after the title, such as a remove button: as wide as they need, apart from picking. */
    readonly trailing?: ReactNode;
}

/** One item of a list to pick from: its title picks it, its trailing controls act on their own. */
export function ListItem({children, onPick, trailing}: ListItemProps) {
    return (
        <li className={styles.item}>
            <button type="button" className={styles.pick} onClick={() => {
                onPick();
            }}
            >
                <Text variant="bodySm" as="span">{children}</Text>
            </button>
            {trailing !== undefined && <span className={styles.trailing}>{trailing}</span>}
        </li>
    );
}
