import {useCallback, useEffect, useRef, useState} from 'react';
import {createPortal} from 'react-dom';
import {useTranslation} from 'react-i18next';
import {Button} from '../button';
import {CloseIcon} from '../icon';
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
    /** What the pictures are of: names the viewer for assistive technology. */
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

/** One picture whole over the dimmed page, going round through the others by its buttons and the arrow keys. */
function Viewer({urls, title, index, label, onIndex, onClose}: ViewerProps) {
    const {t} = useTranslation();
    const [backdrop, setBackdrop] = useState<HTMLDivElement | null>(null);
    const count = urls.length;
    const current = urls[index];
    const previous = (index + count - 1) % count;
    const next = (index + 1) % count;

    useEffect(() => {
        backdrop?.focus();
    }, [backdrop]);

    useEffect(() => {
        const onKeyDown = (event: KeyboardEvent) => {
            if (event.key === 'Escape') {
                onClose();
            } else if (count > 1 && event.key === 'ArrowLeft') {
                onIndex(previous);
            } else if (count > 1 && event.key === 'ArrowRight') {
                onIndex(next);
            }
        };
        document.addEventListener('keydown', onKeyDown);
        return () => {
            document.removeEventListener('keydown', onKeyDown);
        };
    }, [count, previous, next, onIndex, onClose]);

    // A click on the backdrop closes the viewer only when it started there too, so a press on the picture released
    // beside it keeps the viewer open.
    useEffect(() => {
        const element = backdrop;
        if (element === null) {
            return undefined;
        }
        let pressedOnBackdrop = false;
        const onPointerDown = (event: PointerEvent) => {
            pressedOnBackdrop = event.target === element;
        };
        const onClick = (event: MouseEvent) => {
            if (pressedOnBackdrop && event.target === element) {
                onClose();
            }
        };
        element.addEventListener('pointerdown', onPointerDown);
        element.addEventListener('click', onClick);
        return () => {
            element.removeEventListener('pointerdown', onPointerDown);
            element.removeEventListener('click', onClick);
        };
    }, [backdrop, onClose]);

    if (current === undefined) {
        return null;
    }

    return createPortal(
        <div ref={setBackdrop} role="dialog" aria-modal="true" aria-label={title} tabIndex={-1} className={styles.viewer}>
            <Button color="primary" size="sm" square ariaLabel={t('actions.close')} onClick={onClose} className={styles.close}>
                <CloseIcon/>
            </Button>
            <FittedPicture key={current} src={current} alt={label(index)} stage={backdrop}/>
            {count > 1 && (
                <>
                    <Button color="primary" size="sm" ariaLabel={t('screenshots.previous')} onClick={() => {
                        onIndex(previous);
                    }} className={styles.previous}>‹</Button>
                    <Button color="primary" size="sm" ariaLabel={t('screenshots.next')} onClick={() => {
                        onIndex(next);
                    }} className={styles.next}>›</Button>
                </>
            )}
        </div>,
        document.body,
    );
}

interface FittedPictureProps {
    readonly src: string;
    readonly alt: string;
    /** The element whose content box is the area the picture is fitted to. */
    readonly stage: HTMLElement | null;
}

/** The content box of an element: its inner size without its padding. */
function contentSize(element: HTMLElement): PixelSize {
    const style = getComputedStyle(element);
    const padding = (value: string) => Number.parseFloat(value) || 0;
    return {
        width: element.clientWidth - padding(style.paddingLeft) - padding(style.paddingRight),
        height: element.clientHeight - padding(style.paddingTop) - padding(style.paddingBottom),
    };
}

/** A pixel-art picture fitted to the area of its stage without fractional enlargement. */
function FittedPicture({src, alt, stage}: FittedPictureProps) {
    const nativeSize = useRef<PixelSize | null>(null);
    const [displaySize, setDisplaySize] = useState<PixelSize | null>(null);

    const updateDisplaySize = useCallback(() => {
        const native = nativeSize.current;
        if (stage === null || native === null) {
            return;
        }
        const scale = integerFitScale(native, contentSize(stage));
        setDisplaySize(scale > 0
            ? {width: Math.floor(native.width * scale), height: Math.floor(native.height * scale)}
            : null);
    }, [stage]);

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
        if (stage === null) {
            return undefined;
        }
        const observer = new ResizeObserver(updateDisplaySize);
        observer.observe(stage);
        return () => {
            observer.disconnect();
        };
    }, [stage, updateDisplaySize]);

    return displaySize === null
        ? null
        : <img src={src} alt={alt} width={displaySize.width} height={displaySize.height} className={styles.picture}/>;
}
