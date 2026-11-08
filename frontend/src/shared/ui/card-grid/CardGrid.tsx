import type {ReactNode} from 'react';
import {classNames} from '../../lib';
import styles from './CardGrid.module.css';

export interface CardGridProps {
    /** One `<li>` per card. */
    readonly children: ReactNode;
    /** External contour only; see docs/styles.md. */
    readonly className?: string;
}

/** A list of cards in fixed-width tracks spread across the row; one full-width column on mobile. */
export function CardGrid({children, className}: CardGridProps) {
    return <ul className={classNames(styles.grid, className)}>{children}</ul>;
}
