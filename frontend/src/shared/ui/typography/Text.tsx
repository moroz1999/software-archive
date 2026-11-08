import type {ReactNode} from 'react';
import {type TypographyTone, type TypographyVariant, typographyClassName} from './typography';

export interface TextProps {
    readonly children: ReactNode;
    readonly variant?: TypographyVariant;
    readonly tone?: TypographyTone;
    /** One line, cut with an ellipsis. */
    readonly truncate?: boolean;
    readonly as?: 'p' | 'span' | 'div';
    readonly id?: string;
    /** `alert` for a message that appears after an action. */
    readonly role?: 'alert' | 'status';
    /** External contour only; see docs/styles.md. */
    readonly className?: string;
}

/** Text in one of the typography variants and tones. */
export function Text({children, variant = 'body', tone = 'inherit', truncate = false, as: Element = 'p', id, role, className}: TextProps) {
    return <Element id={id} role={role} className={typographyClassName(variant, tone, truncate, className)}>{children}</Element>;
}
