import type {ReactNode} from 'react';
import {Link} from 'react-router';
import {classNames} from '@/shared/lib/class-names';
import {isAppHref} from '@/shared/lib/href';
import styles from './Chip.module.css';

export type ChipVariant = 'opaque' | 'filled' | 'mono' | 'mono-outline';
export type ChipColor = 'neutral' | 'primary' | 'danger' | 'artist' | 'code' | 'intro';
export type ChipSize = 'sm' | 'md';

export interface ChipProps {
    readonly children: ReactNode;
    /** Opaque and filled carry a color; mono and mono-outline are technical identifiers. */
    readonly variant?: ChipVariant;
    readonly color?: ChipColor;
    readonly size?: ChipSize;
    /** Makes the chip a link; a path of this app is navigated client-side. */
    readonly href?: string;
    /** External contour only; see docs/styles.md. */
    readonly className?: string;
}

const variantClass: Readonly<Record<ChipVariant, string>> = {
    opaque: styles.opaque, filled: styles.filled, mono: styles.mono, 'mono-outline': styles.monoOutline,
};
const colorClass: Readonly<Record<ChipColor, string>> = {
    neutral: styles.neutral, primary: styles.primary, danger: styles.danger,
    artist: styles.artist, code: styles.code, intro: styles.intro,
};
const sizeClass: Readonly<Record<ChipSize, string>> = {sm: styles.sm, md: styles.md};

/** A compact tag: a category, a role, a technical identifier. */
export function Chip({children, variant = 'opaque', color = 'neutral', size = 'md', href, className}: ChipProps) {
    const chipClass = classNames(styles.chip, variantClass[variant], colorClass[color], sizeClass[size], className);
    if (href === undefined) {
        return <span className={chipClass}>{children}</span>;
    }
    if (isAppHref(href)) {
        return <Link to={href} className={chipClass}>{children}</Link>;
    }
    return <a href={href} className={chipClass}>{children}</a>;
}
