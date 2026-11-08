import {classNames} from '../../lib';
import styles from './Typography.module.css';

export type TypographyVariant = 'display' | 'headline' | 'title' | 'body' | 'bodySm' | 'mono' | 'caption' | 'label';
export type TypographyTone = 'primary' | 'muted' | 'strong' | 'link' | 'muted-link' | 'link-alt' | 'danger' | 'inherit';

const variantClass: Readonly<Record<TypographyVariant, string>> = {
    display: styles.display,
    headline: styles.headline,
    title: styles.title,
    body: styles.body,
    bodySm: styles.bodySm,
    mono: styles.mono,
    caption: styles.caption,
    label: styles.label,
};

const toneClass: Readonly<Record<TypographyTone, string>> = {
    primary: styles.tonePrimary,
    muted: styles.toneMuted,
    strong: styles.toneStrong,
    link: styles.toneLink,
    'muted-link': styles.toneMutedLink,
    'link-alt': styles.toneLinkAlt,
    danger: styles.toneDanger,
    inherit: styles.toneInherit,
};

/** Class names of a typography variant in a tone, optionally cut to one line with an ellipsis. */
export function typographyClassName(variant: TypographyVariant, tone: TypographyTone, truncate: boolean, className?: string): string {
    return classNames(variantClass[variant], toneClass[tone], truncate && styles.truncate, className);
}

/** Class names of a tone alone, for an element inside text of another variant. */
export function typographyToneClassName(tone: TypographyTone, className?: string): string {
    return classNames(toneClass[tone], className);
}
