import {useState} from 'react';
import {useTranslation} from 'react-i18next';
import {galleryKey, SCREENSHOT_TYPES, type ScreenshotId} from '@/entities/software';
import {useSkeleton} from '@/shared/lib/loading';
import {Button} from '@/shared/ui/button';
import {ConfirmDialog} from '@/shared/ui/confirm-dialog';
import {FileUpload} from '@/shared/ui/file-upload';
import {Grid} from '@/shared/ui/grid';
import {Inline} from '@/shared/ui/inline';
import {ScreenshotGallerySkeleton} from '@/shared/ui/screenshot-gallery';
import {Stack} from '@/shared/ui/stack';
import {Heading, Text} from '@/shared/ui/typography';
import {useEditScreenshots} from '../model/edit-screenshots-context';
import {MoveScreenshotButtons} from './MoveScreenshotButtons';
import {ScreenshotsChangeError} from './ScreenshotsChangeError';
import styles from './ScreenshotsEditor.module.css';

/** The screenshots inside an EditScreenshotsProvider, to add, delete and reorder. */
export function ScreenshotsEditor() {
    const {t} = useTranslation();
    const {gallery, query, upload, busy} = useEditScreenshots();
    const skeleton = useSkeleton(query);

    return (
        <Stack as="section" gap="md" ariaLabel={t(galleryKey(gallery, 'heading'))}>
            <Heading level={2} variant="title">{t(galleryKey(gallery, 'heading'))}</Heading>
            {skeleton || query.isPending
                ? <div data-testid="screenshots-editor-skeleton"><ScreenshotGallerySkeleton/></div>
                : query.isSuccess && <ScreenshotList/>}
            <ScreenshotsChangeError/>
            <Stack gap="xs">
                <FileUpload label={t(galleryKey(gallery, 'add'))} accept={SCREENSHOT_TYPES.join(',')} multiple disabled={busy} onFiles={upload}/>
                <Text variant="bodySm" tone="muted">{t('screenshots.hint')}</Text>
            </Stack>
        </Stack>
    );
}

function ScreenshotList() {
    const {t} = useTranslation();
    const {gallery, query, remove, busy, deleteError, deleted} = useEditScreenshots();
    // Which deletion is being asked about is the dialog's own UI state, not business state.
    const [asking, setAsking] = useState<{readonly id: ScreenshotId; readonly n: number} | null>(null);
    const screenshots = query.data ?? [];

    if (screenshots.length === 0) {
        return <Text tone="muted">{t(galleryKey(gallery, 'none'))}</Text>;
    }
    return (
        <>
            <Grid as="ul" desktopColumns={4} tabletColumns={3} mobileColumns={2} gap="md" ariaLabel={t(galleryKey(gallery, 'heading'))}>
                {screenshots.map((screenshot, index) => {
                    const n = index + 1;
                    return (
                        <li key={screenshot.id}>
                            <Stack gap="xs">
                                <img src={screenshot.url} alt={t(galleryKey(gallery, 'numbered'), {n})} loading="lazy" className={styles.picture}/>
                                <Inline gap="xs" justify="between">
                                    <MoveScreenshotButtons index={index}/>
                                    <Button size="sm" color="danger" ariaLabel={t(galleryKey(gallery, 'delete'), {n})} disabled={busy} onClick={() => {
                                        setAsking({id: screenshot.id, n});
                                    }}>{t('actions.delete')}</Button>
                                </Inline>
                            </Stack>
                        </li>
                    );
                })}
            </Grid>
            <ConfirmDialog
                open={asking !== null && deleted !== asking.id}
                title={t(galleryKey(gallery, 'deleteTitle'))}
                confirmLabel={t('actions.deleteForGood')}
                onConfirm={() => {
                    if (asking !== null) {
                        remove(asking.id);
                    }
                }}
                onCancel={() => {
                    setAsking(null);
                }}
                busy={busy}
                error={deleteError}
            >
                {asking === null ? null : t(galleryKey(gallery, 'deleteQuestion'), {n: asking.n})}
            </ConfirmDialog>
        </>
    );
}
