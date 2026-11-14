import {type ReactNode, useId} from 'react';
import {Link} from 'react-router';
import {classNames} from '@/shared/lib/class-names';
import {isAppHref} from '@/shared/lib/href';
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
    /**
     * The whole panel leads here: its body is one link named as the panel is. A path of this app is navigated
     * client-side; anything else is a plain link. Nothing inside may be interactive.
     */
    readonly href?: string;
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
    href,
    className,
}: PanelProps) {
    const titleId = useId();
    const nameId = title === undefined ? labelledBy : titleId;
    const content = (
        <>
            {title !== undefined && (
                <Heading id={titleId} level={titleLevel} variant={titleLevel === 2 ? 'headline' : 'title'} className={styles.title}>
                    {title}
                </Heading>
            )}
            <div className={styles.body}>{children}</div>
        </>
    );
    return (
        <Element
            aria-labelledby={nameId}
            className={classNames(
                styles.panel,
                radiusClass[radius],
                paddingClass[padding],
                variantClass[variant],
                surfaceClass[surface],
                contentBleed && styles.bleed,
                href !== undefined && styles.linked,
                className,
            )}
        >
            {href === undefined ? content : <PanelLink href={href} labelledBy={nameId}>{content}</PanelLink>}
        </Element>
    );
}

interface PanelLinkProps {
    readonly children: ReactNode;
    readonly href: string;
    readonly labelledBy: string | undefined;
}

/** The link over the whole body of a panel. */
function PanelLink({children, href, labelledBy}: PanelLinkProps) {
    if (isAppHref(href)) {
        return <Link to={href} aria-labelledby={labelledBy} className={styles.link}>{children}</Link>;
    }
    return <a href={href} aria-labelledby={labelledBy} className={styles.link}>{children}</a>;
}
