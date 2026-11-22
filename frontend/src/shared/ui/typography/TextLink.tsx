import type {ReactNode} from 'react';
import {Link} from 'react-router';
import {classNames} from '@/shared/lib/class-names';
import {isAppHref} from '@/shared/lib/href';
import {type TypographyTone, typographyToneClassName} from './typography';
import styles from './Typography.module.css';

export interface TextLinkProps {
    readonly children: ReactNode;
    /** A path of this app is navigated client-side; anything else opens in a new tab. */
    readonly href: string;
    readonly tone?: TypographyTone;
    /** The link of the page shown: marked as current and set off from the others. */
    readonly current?: boolean;
    /** The link is clicked anywhere over the nearest box around it that is positioned (`position: relative`). */
    readonly cover?: boolean;
    /** External contour only; see docs/styles.md. */
    readonly className?: string;
}

/** A link inside text: takes the variant of the text around it and only sets the tone. */
export function TextLink({children, href, tone = 'link', current = false, cover = false, className}: TextLinkProps) {
    const linkClass = typographyToneClassName(current ? 'strong' : tone, classNames(cover && styles.cover, className));
    const ariaCurrent = current ? 'page' : undefined;
    if (isAppHref(href)) {
        return <Link to={href} aria-current={ariaCurrent} className={linkClass}>{children}</Link>;
    }
    return <a href={href} target="_blank" rel="noopener noreferrer" aria-current={ariaCurrent} className={linkClass}>{children}</a>;
}
