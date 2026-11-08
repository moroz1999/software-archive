import type {ReactNode} from 'react';
import {classNames} from '../../lib';
import styles from './Inset.module.css';

export type InsetPadding = 'none' | 'xs' | 'sm' | 'md' | 'lg' | 'xl';
export type InsetSide = 'all' | 'block' | 'inline' | 'top' | 'right' | 'bottom' | 'left';

export interface InsetProps {
    readonly children: ReactNode;
    readonly padding?: InsetPadding;
    readonly side?: InsetSide;
    readonly as?: 'div' | 'li';
    /** External contour only; see docs/styles.md. */
    readonly className?: string;
}

const paddingClass: Readonly<Record<InsetPadding, string>> = {
    none: styles.paddingNone,
    xs: styles.paddingXs,
    sm: styles.paddingSm,
    md: styles.paddingMd,
    lg: styles.paddingLg,
    xl: styles.paddingXl,
};

const sideClass: Readonly<Record<InsetSide, string>> = {
    all: styles.sideAll,
    block: styles.sideBlock,
    inline: styles.sideInline,
    top: styles.sideTop,
    right: styles.sideRight,
    bottom: styles.sideBottom,
    left: styles.sideLeft,
};

/** Inner padding around content, on all or some sides. */
export function Inset({children, padding = 'md', side = 'all', as: Element = 'div', className}: InsetProps) {
    return <Element className={classNames(styles.inset, paddingClass[padding], sideClass[side], className)}>{children}</Element>;
}
