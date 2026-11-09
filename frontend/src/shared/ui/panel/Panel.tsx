import {type ReactNode, useId} from 'react';
import {classNames} from '@/shared/lib/class-names';
import {Heading} from '../typography';
import styles from './Panel.module.css';

export type PanelRadius = 'sm' | 'md' | 'lg' | 'xl';
export type PanelPadding = 'none' | 'sm' | 'md' | 'lg';
export type PanelVariant = 'elevated' | 'flat';
export type PanelSurface = 'default' | 'muted' | 'primary';

export interface PanelProps {
    readonly children: ReactNode;
    readonly radius?: PanelRadius;
    readonly padding?: PanelPadding;
    /** Elevated lies on the page with a shadow; flat is nested in another panel with a border. */
    readonly variant?: PanelVariant;
    readonly surface?: PanelSurface;
    /** A heading above the body; it also names the panel. */
    readonly title?: string;
    /** The id of a heading inside the body that names the panel, when it has no title. */
    readonly labelledBy?: string;
    readonly titleLevel?: 2 | 3;
    /** The body goes edge to edge, clipped by the corners (images on cards). */
    readonly contentBleed?: boolean;
    readonly as?: 'div' | 'section' | 'article';
    /** External contour only; see docs/styles.md. */
    readonly className?: string;
}

const radiusClass: Readonly<Record<PanelRadius, string>> = {
    sm: styles.radiusSm, md: styles.radiusMd, lg: styles.radiusLg, xl: styles.radiusXl,
};
const paddingClass: Readonly<Record<PanelPadding, string>> = {
    none: styles.paddingNone, sm: styles.paddingSm, md: styles.paddingMd, lg: styles.paddingLg,
};
const variantClass: Readonly<Record<PanelVariant, string>> = {elevated: styles.elevated, flat: styles.flat};
const surfaceClass: Readonly<Record<PanelSurface, string>> = {
    default: styles.surfaceDefault, muted: styles.surfaceMuted, primary: styles.surfacePrimary,
};

/** A surface grouping related content, optionally under a title. */
export function Panel({
    children,
    radius = 'md',
    padding = 'md',
    variant = 'elevated',
    surface = 'default',
    title,
    labelledBy,
    titleLevel = 3,
    contentBleed = false,
    as: Element = 'div',
    className,
}: PanelProps) {
    const titleId = useId();
    return (
        <Element
            aria-labelledby={title === undefined ? labelledBy : titleId}
            className={classNames(
                styles.panel,
                radiusClass[radius],
                paddingClass[padding],
                variantClass[variant],
                surfaceClass[surface],
                contentBleed && styles.bleed,
                className,
            )}
        >
            {title !== undefined && (
                <Heading id={titleId} level={titleLevel} variant={titleLevel === 2 ? 'headline' : 'title'} className={styles.title}>
                    {title}
                </Heading>
            )}
            <div className={styles.body}>{children}</div>
        </Element>
    );
}
