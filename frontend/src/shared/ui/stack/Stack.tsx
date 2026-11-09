import type {ReactNode} from 'react';
import {classNames} from '@/shared/lib/class-names';
import type {Gap} from './gap';
import styles from './Stack.module.css';

export type StackAlign = 'center' | 'start' | 'end' | 'stretch';

export interface StackProps {
    readonly children: ReactNode;
    readonly gap?: Gap;
    readonly align?: StackAlign;
    readonly as?: 'div' | 'section' | 'ul' | 'ol' | 'li';
    /** Accessible name, as of a list the stack holds. */
    readonly ariaLabel?: string;
    /** External contour only (margin, alignment); see docs/styles.md. */
    readonly className?: string;
}

const gapClass: Readonly<Record<Gap, string>> = {
    none: styles.gapNone,
    xs: styles.gapXs,
    sm: styles.gapSm,
    md: styles.gapMd,
    lg: styles.gapLg,
    xl: styles.gapXl,
    xxl: styles.gapXxl,
};

const alignClass: Readonly<Record<StackAlign, string>> = {
    center: styles.alignCenter,
    start: styles.alignStart,
    end: styles.alignEnd,
    stretch: styles.alignStretch,
};

/** Vertical layout: children one under another with a gap from the scale. */
export function Stack({children, gap = 'md', align, as: Element = 'div', ariaLabel, className}: StackProps) {
    return (
        <Element aria-label={ariaLabel} className={classNames(styles.stack, gapClass[gap], align !== undefined && alignClass[align], className)}>
            {children}
        </Element>
    );
}
