import {useTranslation} from 'react-i18next';
import {galleryKey} from '@/entities/software';
import {Button} from '@/shared/ui/button';
import {ChevronLeftIcon, ChevronRightIcon} from '@/shared/ui/icon';
import {Inline} from '@/shared/ui/inline';
import {useEditScreenshots} from '../model/edit-screenshots-context';

export interface MoveScreenshotButtonsProps {
    /** The place of the screenshot among those inside the EditScreenshotsProvider. */
    readonly index: number;
}

/** Chevrons moving a screenshot a place, neither past the ends nor while a change runs. */
export function MoveScreenshotButtons({index}: MoveScreenshotButtonsProps) {
    const {t} = useTranslation();
    const {gallery, query, move, busy} = useEditScreenshots();
    const screenshots = query.data ?? [];
    const screenshot = screenshots[index];
    if (screenshot === undefined) {
        return null;
    }
    const n = index + 1;
    return (
        <Inline gap="xs">
            <Button color="secondary" square ariaLabel={t(galleryKey(gallery, 'moveLeft'), {n})} disabled={busy || index === 0} onClick={() => {
                move(screenshot.id, -1);
            }}><ChevronLeftIcon/></Button>
            <Button color="secondary" square ariaLabel={t(galleryKey(gallery, 'moveRight'), {n})} disabled={busy || n === screenshots.length} onClick={() => {
                move(screenshot.id, 1);
            }}><ChevronRightIcon/></Button>
        </Inline>
    );
}
