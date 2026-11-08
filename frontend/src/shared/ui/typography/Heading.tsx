import type {ReactNode} from 'react';
import {typographyClassName} from './typography';

export type HeadingLevel = 1 | 2 | 3;
export type HeadingVariant = 'display' | 'headline' | 'title';
export type HeadingTone = 'primary' | 'muted' | 'danger' | 'inherit';

export interface HeadingProps {
    readonly children: ReactNode;
    /** The document outline level; the look follows it unless `variant` is given. */
    readonly level: HeadingLevel;
    readonly variant?: HeadingVariant;
    readonly tone?: HeadingTone;
    /** One line, cut with an ellipsis. */
    readonly truncate?: boolean;
    readonly id?: string;
    /** External contour only; see docs/styles.md. */
    readonly className?: string;
}

const element = {1: 'h1', 2: 'h2', 3: 'h3'} as const;
const variantOfLevel: Readonly<Record<HeadingLevel, HeadingVariant>> = {1: 'display', 2: 'headline', 3: 'title'};

/** A heading: display for pages, headline for sections, title for panels and cards. */
export function Heading({children, level, variant, tone = 'primary', truncate = false, id, className}: HeadingProps) {
    const Element = element[level];
    return <Element id={id} className={typographyClassName(variant ?? variantOfLevel[level], tone, truncate, className)}>{children}</Element>;
}
