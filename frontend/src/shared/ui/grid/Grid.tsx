import type {ReactNode} from 'react';
import {classNames} from '@/shared/lib/class-names';
import type {Gap} from '../stack';
import styles from './Grid.module.css';

export type GridColumns = 1 | 2 | 3 | 4 | 5 | 6;
export type GridRows = 'auto' | 1 | 2 | 3 | 4;
export type GridAlignment = 'start' | 'center' | 'end' | 'stretch';

export interface GridProps {
    readonly children: ReactNode;
    readonly desktopColumns?: GridColumns;
    /** Below 992px; defaults to the desktop columns. */
    readonly tabletColumns?: GridColumns;
    /** Below 576px; defaults to the tablet columns. */
    readonly mobileColumns?: GridColumns;
    readonly rows?: GridRows;
    readonly gap?: Gap;
    /** Overrides the gap between rows. */
    readonly rowGap?: Gap;
    /** Overrides the gap between columns. */
    readonly columnGap?: Gap;
    readonly align?: GridAlignment;
    readonly justify?: GridAlignment;
    readonly as?: 'div' | 'ul';
    /** Accessible name, as of a list the grid holds. */
    readonly ariaLabel?: string;
    /** External contour only; see docs/styles.md. */
    readonly className?: string;
}

const desktopClass: Readonly<Record<GridColumns, string>> = {
    1: styles.desktop1, 2: styles.desktop2, 3: styles.desktop3, 4: styles.desktop4, 5: styles.desktop5, 6: styles.desktop6,
};
const tabletClass: Readonly<Record<GridColumns, string>> = {
    1: styles.tablet1, 2: styles.tablet2, 3: styles.tablet3, 4: styles.tablet4, 5: styles.tablet5, 6: styles.tablet6,
};
const mobileClass: Readonly<Record<GridColumns, string>> = {
    1: styles.mobile1, 2: styles.mobile2, 3: styles.mobile3, 4: styles.mobile4, 5: styles.mobile5, 6: styles.mobile6,
};
const rowsClass: Readonly<Record<GridRows, string>> = {
    auto: styles.rowsAuto, 1: styles.rows1, 2: styles.rows2, 3: styles.rows3, 4: styles.rows4,
};
const rowGapClass: Readonly<Record<Gap, string>> = {
    none: styles.rowGapNone, xs: styles.rowGapXs, sm: styles.rowGapSm, md: styles.rowGapMd,
    lg: styles.rowGapLg, xl: styles.rowGapXl, xxl: styles.rowGapXxl,
};
const columnGapClass: Readonly<Record<Gap, string>> = {
    none: styles.columnGapNone, xs: styles.columnGapXs, sm: styles.columnGapSm, md: styles.columnGapMd,
    lg: styles.columnGapLg, xl: styles.columnGapXl, xxl: styles.columnGapXxl,
};
const alignClass: Readonly<Record<GridAlignment, string>> = {
    start: styles.alignStart, center: styles.alignCenter, end: styles.alignEnd, stretch: styles.alignStretch,
};
const justifyClass: Readonly<Record<GridAlignment, string>> = {
    start: styles.justifyStart, center: styles.justifyCenter, end: styles.justifyEnd, stretch: styles.justifyStretch,
};

/** Responsive grid of equal columns: desktop, tablet (below 992px) and mobile (below 576px). */
export function Grid({
    children,
    desktopColumns = 1,
    tabletColumns,
    mobileColumns,
    rows = 'auto',
    gap = 'md',
    rowGap,
    columnGap,
    align = 'stretch',
    justify = 'stretch',
    as: Element = 'div',
    ariaLabel,
    className,
}: GridProps) {
    const tablet = tabletColumns ?? desktopColumns;
    return (
        <Element
            aria-label={ariaLabel}
            className={classNames(
                styles.grid,
                desktopClass[desktopColumns],
                tabletClass[tablet],
                mobileClass[mobileColumns ?? tablet],
                rowsClass[rows],
                rowGapClass[rowGap ?? gap],
                columnGapClass[columnGap ?? gap],
                alignClass[align],
                justifyClass[justify],
                className,
            )}
        >
            {children}
        </Element>
    );
}

export interface GridItemProps {
    readonly children: ReactNode;
    readonly desktopSpan?: GridColumns;
    /** Below 992px; defaults to the desktop span. */
    readonly tabletSpan?: GridColumns;
    /** Below 576px; defaults to the desktop span. */
    readonly mobileSpan?: GridColumns;
    readonly as?: 'div' | 'li';
    /** External contour only; see docs/styles.md. */
    readonly className?: string;
}

const spanDesktopClass: Readonly<Record<GridColumns, string>> = {
    1: styles.spanDesktop1, 2: styles.spanDesktop2, 3: styles.spanDesktop3,
    4: styles.spanDesktop4, 5: styles.spanDesktop5, 6: styles.spanDesktop6,
};
const spanTabletClass: Readonly<Record<GridColumns, string>> = {
    1: styles.spanTablet1, 2: styles.spanTablet2, 3: styles.spanTablet3,
    4: styles.spanTablet4, 5: styles.spanTablet5, 6: styles.spanTablet6,
};
const spanMobileClass: Readonly<Record<GridColumns, string>> = {
    1: styles.spanMobile1, 2: styles.spanMobile2, 3: styles.spanMobile3,
    4: styles.spanMobile4, 5: styles.spanMobile5, 6: styles.spanMobile6,
};

/** A grid cell spanning several columns; shrinks to its track. */
export function GridItem({children, desktopSpan = 1, tabletSpan, mobileSpan, as: Element = 'div', className}: GridItemProps) {
    return (
        <Element
            className={classNames(
                styles.item,
                spanDesktopClass[desktopSpan],
                spanTabletClass[tabletSpan ?? desktopSpan],
                spanMobileClass[mobileSpan ?? desktopSpan],
                className,
            )}
        >
            {children}
        </Element>
    );
}
