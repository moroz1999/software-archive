import {useCallback, useEffect, useRef, useState} from 'react';
import {useTranslation} from 'react-i18next';
import {Button} from '../button';
import {Dialog} from '../dialog';
import {Inline} from '../inline';
import styles from './ScreenshotGallery.module.css';

interface PixelSize {
    readonly width: number;
    readonly height: number;
}

/** Space kept free between the picture and every edge of its area, in CSS pixels. */
const STAGE_MARGIN = 16;

/** The largest whole enlargement that fits the stage; an oversized picture instead shrinks just enough to fit. */
function integerFitScale(native: PixelSize, stage: PixelSize): number {
    const fit = Math.min(
        (stage.width - 2 * STAGE_MARGIN) / native.width,
        (stage.height - 2 * STAGE_MARGIN) / native.height,
    );
    return fit < 1 ? Math.max(fit, 0) : Math.floor(fit);
}

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
    const current = urls[index];
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

    if (current === undefined) {
        return null;
    }

    return (
        <Dialog open title={title} onClose={onClose} dismissible width="fullscreen">
            <div className={styles.viewer}>
                <FittedPicture key={current} src={current} alt={label(index)}/>
                {count > 1 && (
                    <Inline gap="sm" justify="between" align="center">
                        <Button color="primary" size="sm" ariaLabel={t('screenshots.previous')} onClick={() => {
                            onIndex(previous);
                        }}>‹</Button>
                        <Button color="primary" size="sm" ariaLabel={t('screenshots.next')} onClick={() => {
                            onIndex(next);
                        }}>›</Button>
                    </Inline>
                )}
            </div>
        </Dialog>
    );
}

interface FittedPictureProps {
    readonly src: string;
    readonly alt: string;
}

/** A pixel-art picture fitted to its available area without fractional enlargement. */
function FittedPicture({src, alt}: FittedPictureProps) {
    const stage = useRef<HTMLDivElement>(null);
    const nativeSize = useRef<PixelSize | null>(null);
    const [displaySize, setDisplaySize] = useState<PixelSize | null>(null);

    const updateDisplaySize = useCallback(() => {
        const element = stage.current;
        const native = nativeSize.current;
        if (element === null || native === null) {
            return;
        }
        const scale = integerFitScale(native, {width: element.clientWidth, height: element.clientHeight});
        setDisplaySize(scale > 0
            ? {width: Math.floor(native.width * scale), height: Math.floor(native.height * scale)}
            : null);
    }, []);

    useEffect(() => {
        let current = true;
        const image = new Image();
        image.onload = () => {
            if (!current) {
                return;
            }
            nativeSize.current = {width: image.naturalWidth, height: image.naturalHeight};
            updateDisplaySize();
        };
        image.src = src;
        return () => {
            current = false;
        };
    }, [src, updateDisplaySize]);

    useEffect(() => {
        const element = stage.current;
        if (element === null) {
            return undefined;
        }
        const observer = new ResizeObserver(updateDisplaySize);
        observer.observe(element);
        return () => {
            observer.disconnect();
        };
    }, [updateDisplaySize]);

    return (
        <div ref={stage} className={styles.pictureStage}>
            {displaySize !== null && <img src={src} alt={alt} width={displaySize.width} height={displaySize.height} className={styles.picture}/>}
        </div>
    );
}
