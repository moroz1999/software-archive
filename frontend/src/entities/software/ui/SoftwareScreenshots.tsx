import type {ReactNode} from 'react';
import {useTranslation} from 'react-i18next';
import {useSkeleton} from '@/shared/lib/loading';
import {ScreenshotGallery, ScreenshotGallerySkeleton} from '@/shared/ui/screenshot-gallery';
import {Stack} from '@/shared/ui/stack';
import {Heading} from '@/shared/ui/typography';
import {galleryKey} from '../lib/gallery-texts';
import type {Gallery} from '../model/screenshot';
import {useScreenshots} from '../model/screenshots-context';

export interface SoftwareScreenshotsProps {
    /** What the screenshots are of, naming their viewer. */
    readonly title: string;
    /** The lazy-loading observer attached to the skeleton until the block enters the viewport. */
    readonly viewportRef?: (element: Element | null) => (() => void) | undefined;
    /** What can be done to the screenshot at an index, on its thumbnail. */
    readonly controls?: (index: number) => ReactNode;
    /** Shown under the screenshots, such as why a change of them failed. */
    readonly note?: ReactNode;
    /** The gallery the pictures are of, which names them; screenshots by default. */
    readonly gallery?: Gallery;
}

/** The screenshots inside a ScreenshotsProvider under their heading; nothing at all when there are none. */
export function SoftwareScreenshots({title, viewportRef, controls, note, gallery = 'screenshots'}: SoftwareScreenshotsProps) {
    const {t} = useTranslation();
    const query = useScreenshots();
    const skeleton = useSkeleton(query);

    if (skeleton || query.isPending) {
        return (
            <Stack as="section" gap="md" ariaLabel={t(galleryKey(gallery, 'heading'))}>
                <Heading level={2} variant="title">{t(galleryKey(gallery, 'heading'))}</Heading>
                <div ref={viewportRef} data-testid="software-screenshots-skeleton"><ScreenshotGallerySkeleton/></div>
            </Stack>
        );
    }
    if (query.isLoadingError || query.data.length === 0) {
        // A failure is shown by the error dialog (docs/error-handling.md); no screenshots take no place.
        return null;
    }
    return (
        <Stack as="section" gap="md" ariaLabel={t(galleryKey(gallery, 'heading'))}>
            <Heading level={2} variant="title">{t(galleryKey(gallery, 'heading'))}</Heading>
            <ScreenshotGallery urls={query.data.map((screenshot) => screenshot.url)} title={title} controls={controls} pictureLabel={(n, count) => t(galleryKey(gallery, 'nOfCount'), {n, count})}/>
            {note}
        </Stack>
    );
}
