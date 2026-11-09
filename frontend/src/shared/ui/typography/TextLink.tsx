import type {ReactNode} from 'react';
import {Link} from 'react-router';
import {isAppHref} from '@/shared/lib/href';
import {type TypographyTone, typographyToneClassName} from './typography';

export interface TextLinkProps {
    readonly children: ReactNode;
    /** A path of this app is navigated client-side; anything else is a plain link. */
    readonly href: string;
    readonly tone?: TypographyTone;
    /** External contour only; see docs/styles.md. */
    readonly className?: string;
}

/** A link inside text: takes the variant of the text around it and only sets the tone. */
export function TextLink({children, href, tone = 'link', className}: TextLinkProps) {
    const linkClass = typographyToneClassName(tone, className);
    if (isAppHref(href)) {
        return <Link to={href} className={linkClass}>{children}</Link>;
    }
    return <a href={href} className={linkClass}>{children}</a>;
}
