import {Skeleton} from '../skeleton';
import styles from './ScreenshotGallery.module.css';

const THUMBNAILS = [1, 2, 3] as const;

/** The place of a gallery while its screenshots load: thumbnails in the same grid. */
export function ScreenshotGallerySkeleton() {
    return (
        <div className={styles.grid} aria-hidden="true">
            {THUMBNAILS.map((thumbnail) => <Skeleton key={thumbnail} className={styles.thumbnailBone}/>)}
        </div>
    );
}
