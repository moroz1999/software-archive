import {useId, useState} from 'react';
import {classNames} from '@/shared/lib/class-names';
import styles from './RatingStars.module.css';

const STARS = [1, 2, 3, 4, 5] as const;
/** One star of the 24-unit box each star has. */
const STAR_PATH = 'M12 2.5l2.94 6.2 6.81.82-5.02 4.68 1.31 6.73L12 17.6l-6.04 3.33 1.31-6.73-5.02-4.68 6.81-.82z';

export interface RatingStarsProps {
    /** How many of the five stars are filled, a share of one for a part of a star. */
    readonly value: number;
    /** The name of the stars for assistive technology: the rating they show. */
    readonly label: string;
    /** Makes every star a button giving that many stars; without it the stars are only shown. */
    readonly onRate?: (stars: number) => void;
    /** The names of the star buttons, by the number of stars each gives. */
    readonly starLabel?: (stars: number) => string;
    /** The stars the reader gave, their button pressed. */
    readonly chosen?: number | null;
    readonly disabled?: boolean;
    /** A tooltip over the stars, as the number of votes behind the rating. */
    readonly title?: string;
    readonly size?: 'sm' | 'md';
    /** External contour only; see docs/styles.md. */
    readonly className?: string;
}

const SIZES = {sm: styles.sm, md: styles.md} as const;

/** The share of the five stars filled, in percent, to two decimals. */
function filledShare(stars: number): string {
    const share = Math.min(Math.max(stars, 0), STARS.length) / STARS.length;
    return `${String(Math.round(share * 10000) / 100)}%`;
}

/** Five stars filled as far as a rating goes; with `onRate` each star gives that many, pointing at one shows what it would. */
export function RatingStars({value, label, onRate, starLabel = String, chosen = null, disabled = false, title, size = 'md', className}: RatingStarsProps) {
    const clipId = useId();
    const [pointed, setPointed] = useState<number | null>(null);
    const stars = (
        <svg className={styles.stars} viewBox={`0 0 ${String(STARS.length * 24)} 24`} aria-hidden="true" focusable="false">
            <defs>
                <clipPath id={clipId}>
                    <rect data-rating-fill="" x="0" y="0" width={filledShare(pointed ?? value)} height="100%"/>
                </clipPath>
            </defs>
            <g className={styles.empty}>
                {STARS.map((star) => <path key={star} d={STAR_PATH} transform={`translate(${String((star - 1) * 24)} 0)`}/>)}
            </g>
            <g className={styles.filled} clipPath={`url(#${clipId})`}>
                {STARS.map((star) => <path key={star} d={STAR_PATH} transform={`translate(${String((star - 1) * 24)} 0)`}/>)}
            </g>
        </svg>
    );
    if (onRate === undefined) {
        return <span role="img" aria-label={label} title={title} className={classNames(styles.ratingStars, SIZES[size], className)}>{stars}</span>;
    }
    return (
        <div role="group" aria-label={label} title={title} className={classNames(styles.ratingStars, SIZES[size], className)}>
            {stars}
            <span className={styles.buttons}>
                {STARS.map((star) => (
                    <button
                        key={star}
                        type="button"
                        className={styles.star}
                        aria-label={starLabel(star)}
                        aria-pressed={chosen === star}
                        disabled={disabled}
                        onPointerEnter={() => {
                            setPointed(star);
                        }}
                        onPointerLeave={() => {
                            setPointed(null);
                        }}
                        onFocus={() => {
                            setPointed(star);
                        }}
                        onBlur={() => {
                            setPointed(null);
                        }}
                        onClick={() => {
                            onRate(star);
                        }}
                    />
                ))}
            </span>
        </div>
    );
}
