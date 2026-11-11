import type {ReactNode} from 'react';
import {classNames} from '@/shared/lib/class-names';
import type {Gap} from '../stack';
import styles from './Inline.module.css';

export type InlineAlign = 'center' | 'start' | 'end' | 'stretch' | 'baseline';
export type InlineJustify = 'start' | 'center' | 'end' | 'between';

export interface InlineProps {
    readonly children: ReactNode;
    readonly gap?: Gap;
    readonly align?: InlineAlign;
    readonly justify?: InlineJustify;
    /** Lets children move to the next line when out of room. */
    readonly wrap?: boolean;
    readonly as?: 'div' | 'span' | 'ul' | 'li';
    /** Accessible name, as of a list the row holds. */
    readonly ariaLabel?: string;
    /** External contour only (margin, alignment, flex-grow); see docs/styles.md. */
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

const alignClass: Readonly<Record<InlineAlign, string>> = {
    center: styles.alignCenter,
    start: styles.alignStart,
    end: styles.alignEnd,
    stretch: styles.alignStretch,
    baseline: styles.alignBaseline,
};

const justifyClass: Readonly<Record<InlineJustify, string>> = {
    start: styles.justifyStart,
    center: styles.justifyCenter,
    end: styles.justifyEnd,
    between: styles.justifyBetween,
};

/** Horizontal layout: children in a row with a gap from the scale. */
export function Inline({children, gap = 'md', align, justify, wrap = false, as: Element = 'div', ariaLabel, className}: InlineProps) {
    return (
        <Element
            aria-label={ariaLabel}
            className={classNames(
                styles.inline,
                gapClass[gap],
                align !== undefined && alignClass[align],
                justify !== undefined && justifyClass[justify],
                wrap && styles.wrap,
                className,
            )}
        >
            {children}
        </Element>
    );
}
