import {useEffect, useState} from 'react';
import {useTranslation} from 'react-i18next';
import {Button} from '../button';
import {Dialog} from '../dialog';
import {Inline} from '../inline';
import styles from './ScreenshotGallery.module.css';

export interface ScreenshotGalleryProps {
    /** The addresses of the pictures, in their order. */
    readonly urls: readonly string[];
    /** What the pictures are of: the title of the viewer. */
    readonly title: string;
}

/** Screenshots as a grid of 4:3 thumbnails; a thumbnail opens its picture whole in a viewer that goes through them all. */
export function ScreenshotGallery({urls, title}: ScreenshotGalleryProps) {
    const {t} = useTranslation();
    const [open, setOpen] = useState<number | null>(null);
    const count = urls.length;
    const label = (index: number) => t('screenshots.nOfCount', {n: index + 1, count});

    return (
        <>
            <ul className={styles.grid}>
                {urls.map((url, index) => (
                    <li key={url}>
                        <button type="button" className={styles.thumbnail} aria-label={label(index)} onClick={() => {
                            setOpen(index);
                        }}>
                            <img src={url} alt={label(index)} aria-hidden="true" loading="lazy" className={styles.thumbnailPicture}/>
                        </button>
                    </li>
                ))}
            </ul>
            {open !== null && (
                <Viewer urls={urls} title={title} index={open} label={label} onIndex={setOpen} onClose={() => {
                    setOpen(null);
                }}/>
            )}
        </>
    );
}

interface ViewerProps {
    readonly urls: readonly string[];
    readonly title: string;
    readonly index: number;
    readonly label: (index: number) => string;
    readonly onIndex: (index: number) => void;
    readonly onClose: () => void;
}

/** One picture whole, going round through the others by its buttons and the arrow keys. */
function Viewer({urls, title, index, label, onIndex, onClose}: ViewerProps) {
    const {t} = useTranslation();
    const count = urls.length;
    const previous = (index + count - 1) % count;
    const next = (index + 1) % count;

    useEffect(() => {
        if (count < 2) {
            return undefined;
        }
        const onKeyDown = (event: KeyboardEvent) => {
            if (event.key === 'ArrowLeft') {
                onIndex(previous);
            } else if (event.key === 'ArrowRight') {
                onIndex(next);
            }
        };
        document.addEventListener('keydown', onKeyDown);
        return () => {
            document.removeEventListener('keydown', onKeyDown);
        };
    }, [count, previous, next, onIndex]);

    return (
        <Dialog open title={title} onClose={onClose} dismissible width="wide">
            <img src={urls[index]} alt={label(index)} className={styles.picture}/>
            {count > 1 && (
                <Inline gap="sm" justify="between" align="center">
                    <Button color="outlined" size="sm" ariaLabel={t('screenshots.previous')} onClick={() => {
                        onIndex(previous);
                    }}>‹</Button>
                    <Button color="outlined" size="sm" ariaLabel={t('screenshots.next')} onClick={() => {
                        onIndex(next);
                    }}>›</Button>
                </Inline>
            )}
        </Dialog>
    );
}
