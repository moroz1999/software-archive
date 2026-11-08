import {type MouseEvent, type PointerEvent, type ReactNode, useEffect, useRef, useState} from 'react';
import {Link} from 'react-router';
import {classNames, isAppHref} from '../../lib';
import styles from './Button.module.css';

export type ButtonSize = 'xs' | 'sm' | 'md';
export type ButtonColor = 'primary' | 'secondary' | 'danger' | 'transparent' | 'outlined';
export type LinkTarget = '_self' | '_blank' | '_parent' | '_top';

interface CommonProps {
    readonly children: ReactNode;
    readonly size?: ButtonSize;
    readonly color?: ButtonColor;
    readonly disabled?: boolean;
    /** Icon-only button of equal width and height. */
    readonly square?: boolean;
    /** Square with fully rounded corners. */
    readonly round?: boolean;
    /** Takes the whole width of its container. */
    readonly block?: boolean;
    /** Accessible name when the content does not say it (icon-only buttons). */
    readonly ariaLabel?: string;
    /** External contour only (margin, alignment); see docs/styles.md. */
    readonly className?: string;
}

interface ActionProps extends CommonProps {
    readonly href?: never;
    readonly onClick?: () => void;
    readonly type?: 'button' | 'submit' | 'reset';
    /** Its action runs: locked and announced as busy. */
    readonly loading?: boolean;
}

interface LinkProps extends CommonProps {
    /** A path of this app is navigated client-side; anything else is a plain link. */
    readonly href: string;
    readonly target?: LinkTarget;
    readonly rel?: string;
}

export type ButtonProps = ActionProps | LinkProps;

const sizeClass: Readonly<Record<ButtonSize, string>> = {xs: styles.xs, sm: styles.sm, md: styles.md};
const colorClass: Readonly<Record<ButtonColor, string>> = {
    primary: styles.primary,
    secondary: styles.secondary,
    danger: styles.danger,
    transparent: styles.transparent,
    outlined: styles.outlined,
};

const RIPPLE_MS = 560;

/** A link opened in a new tab never gets access to this page. */
function safeRel(target: LinkTarget | undefined, rel: string | undefined): string | undefined {
    if (target !== '_blank') {
        return rel;
    }
    const parts = new Set((rel ?? '').split(' ').filter((part) => part !== ''));
    parts.add('noopener');
    parts.add('noreferrer');
    return [...parts].join(' ');
}

function preventClick(event: MouseEvent) {
    event.preventDefault();
}

/** Ripple from the pressed point: restarts on every press and stops by itself. */
function useRipple(disabled: boolean) {
    const [rippling, setRippling] = useState(false);
    const frame = useRef<number | undefined>(undefined);
    const timer = useRef<ReturnType<typeof setTimeout> | undefined>(undefined);

    useEffect(() => () => {
        if (frame.current !== undefined) {
            cancelAnimationFrame(frame.current);
        }
        clearTimeout(timer.current);
    }, []);

    function onPointerDown(event: PointerEvent<HTMLElement>) {
        if (disabled) {
            return;
        }
        const element = event.currentTarget;
        const rect = element.getBoundingClientRect();
        const x = event.clientX - rect.left;
        const y = event.clientY - rect.top;
        const reachX = Math.max(x, rect.width - x);
        const reachY = Math.max(y, rect.height - y);
        element.style.setProperty('--button-ripple-x', `${String(x)}px`);
        element.style.setProperty('--button-ripple-y', `${String(y)}px`);
        element.style.setProperty('--button-ripple-size', `${String(Math.ceil(Math.hypot(reachX, reachY) * 2))}px`);

        setRippling(false);
        if (frame.current !== undefined) {
            cancelAnimationFrame(frame.current);
        }
        clearTimeout(timer.current);
        frame.current = requestAnimationFrame(() => {
            setRippling(true);
            timer.current = setTimeout(() => {
                setRippling(false);
            }, RIPPLE_MS);
        });
    }

    return {rippling, onPointerDown};
}

/** The action control: a button, or a link that looks like one when given `href`. */
export function Button(props: ButtonProps) {
    const loading = props.href === undefined && props.loading === true;
    const disabled = props.disabled === true || loading;
    const {rippling, onPointerDown} = useRipple(disabled);
    const className = classNames(
        styles.button,
        sizeClass[props.size ?? 'md'],
        colorClass[props.color ?? 'primary'],
        props.square === true && styles.square,
        props.round === true && styles.round,
        props.block === true && styles.block,
        rippling && styles.rippling,
        props.className,
    );
    const content = (
        <>
            <span aria-hidden="true" className={styles.ripple}/>
            <span className={styles.content}>{props.children}</span>
        </>
    );

    if (props.href !== undefined) {
        if (!disabled && isAppHref(props.href) && props.target === undefined) {
            return (
                <Link to={props.href} className={className} aria-label={props.ariaLabel} onPointerDown={onPointerDown}>
                    {content}
                </Link>
            );
        }
        return (
            <a
                href={props.href}
                target={props.target}
                rel={safeRel(props.target, props.rel)}
                className={className}
                aria-label={props.ariaLabel}
                aria-disabled={disabled ? true : undefined}
                tabIndex={disabled ? -1 : undefined}
                onClick={disabled ? preventClick : undefined}
                onPointerDown={onPointerDown}
            >
                {content}
            </a>
        );
    }

    const {onClick} = props;

    return (
        <button
            className={className}
            type={props.type ?? 'button'}
            disabled={disabled}
            aria-label={props.ariaLabel}
            aria-busy={loading ? true : undefined}
            onPointerDown={onPointerDown}
            onClick={onClick === undefined ? undefined : () => {
                onClick();
            }}
        >
            {content}
        </button>
    );
}
