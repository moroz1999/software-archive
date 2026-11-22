import {useTranslation} from 'react-i18next';
import {useSkeleton} from '@/shared/lib/loading';
import {ScreenshotGallery, ScreenshotGallerySkeleton} from '@/shared/ui/screenshot-gallery';
import {Stack} from '@/shared/ui/stack';
import {Heading} from '@/shared/ui/typography';
import {useScreenshots} from '../model/screenshots-context';

export interface SoftwareScreenshotsProps {
    /** What the screenshots are of, naming their viewer. */
    readonly title: string;
    /** The lazy-loading observer attached to the skeleton until the block enters the viewport. */
    readonly viewportRef?: (element: Element | null) => (() => void) | undefined;
}

/** The screenshots inside a ScreenshotsProvider under their heading; nothing at all when there are none. */
export function SoftwareScreenshots({title, viewportRef}: SoftwareScreenshotsProps) {
    const {t} = useTranslation();
    const query = useScreenshots();
    const skeleton = useSkeleton(query);

    if (skeleton || query.isPending) {
        return (
            <Stack as="section" gap="md" ariaLabel={t('screenshots.heading')}>
                <Heading level={2} variant="title">{t('screenshots.heading')}</Heading>
                <div ref={viewportRef} data-testid="software-screenshots-skeleton"><ScreenshotGallerySkeleton/></div>
            </Stack>
        );
    }
    if (query.isLoadingError || query.data.length === 0) {
        // A failure is shown by the error dialog (docs/error-handling.md); no screenshots take no place.
        return null;
    }
    return (
        <Stack as="section" gap="md" ariaLabel={t('screenshots.heading')}>
            <Heading level={2} variant="title">{t('screenshots.heading')}</Heading>
            <ScreenshotGallery urls={query.data.map((screenshot) => screenshot.url)} title={title}/>
        </Stack>
    );
}
