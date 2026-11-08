import {classNames} from '../../lib';
import styles from './ImagePlaceholder.module.css';

export type ImageRatio = '4:3' | '1:1';

export interface ImagePlaceholderProps {
    /** What image is missing, for assistive technology. */
    readonly label: string;
    readonly ratio?: ImageRatio;
    /** External contour only; see docs/styles.md. */
    readonly className?: string;
}

const ratioClass: Readonly<Record<ImageRatio, string>> = {'4:3': styles.ratio4x3, '1:1': styles.ratio1x1};

/** Stands in for an image that does not exist yet, keeping its place and proportions. */
export function ImagePlaceholder({label, ratio = '4:3', className}: ImagePlaceholderProps) {
    return (
        <div role="img" aria-label={label} className={classNames(styles.placeholder, ratioClass[ratio], className)}>
            <svg className={styles.icon} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" aria-hidden="true">
                <rect x="3" y="4" width="18" height="16" rx="2"/>
                <circle cx="9" cy="10" r="2"/>
                <path d="M21 17l-5-5-9 8"/>
            </svg>
        </div>
    );
}
