import type {ReactNode} from 'react';
import {type TypographyTone, typographyClassName} from './typography';

export interface LabelProps {
    readonly children: ReactNode;
    /** The id of the labelled control. */
    readonly htmlFor: string;
    readonly tone?: TypographyTone;
    /** External contour only; see docs/styles.md. */
    readonly className?: string;
}

/** The caption of a form control in the label variant. */
export function Label({children, htmlFor, tone = 'inherit', className}: LabelProps) {
    return <label htmlFor={htmlFor} className={typographyClassName('label', tone, false, className)}>{children}</label>;
}
