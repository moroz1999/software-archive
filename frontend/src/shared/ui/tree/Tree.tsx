import {type ReactNode, useState} from 'react';
import styles from './Tree.module.css';

export interface TreeItem {
    readonly id: string;
    /** What the item is called, for the labels of its toggle. */
    readonly name: string;
    /** What the row shows, such as a link. */
    readonly content: ReactNode;
    readonly children: readonly TreeItem[];
}

export interface TreeProps {
    readonly items: readonly TreeItem[];
    /** Accessible name of the whole tree. */
    readonly ariaLabel: string;
    readonly expandLabel: (name: string) => string;
    readonly collapseLabel: (name: string) => string;
}

/** A collapsible tree of rows: an item with children unfolds them under itself; everything starts folded. */
export function Tree({items, ariaLabel, expandLabel, collapseLabel}: TreeProps) {
    // Which items are unfolded is the tree's own UI state.
    const [expanded, setExpanded] = useState<ReadonlySet<string>>(new Set());

    function toggle(id: string) {
        setExpanded((current) => {
            const next = new Set(current);
            if (!next.delete(id)) {
                next.add(id);
            }
            return next;
        });
    }

    function renderItems(level: readonly TreeItem[], label?: string): ReactNode {
        return (
            <ul aria-label={label} className={styles.list}>
                {level.map((item) => {
                    const open = expanded.has(item.id);
                    return (
                        <li key={item.id}>
                            <div className={styles.row}>
                                {item.children.length === 0
                                    ? <span aria-hidden="true" className={styles.spacer}/>
                                    : (
                                        <button
                                            type="button"
                                            aria-expanded={open}
                                            aria-label={open ? collapseLabel(item.name) : expandLabel(item.name)}
                                            className={styles.toggle}
                                            onClick={() => {
                                                toggle(item.id);
                                            }}
                                        >
                                            <span aria-hidden="true" className={open ? styles.chevronOpen : styles.chevron}/>
                                        </button>
                                    )}
                                {item.content}
                            </div>
                            {open && <div className={styles.children}>{renderItems(item.children)}</div>}
                        </li>
                    );
                })}
            </ul>
        );
    }

    return renderItems(items, ariaLabel);
}
