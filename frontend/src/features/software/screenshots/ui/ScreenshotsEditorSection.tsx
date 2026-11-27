import type {Gallery} from '@/entities/software';
import {useInViewport} from '@/shared/lib/screen';
import {EditScreenshotsProvider} from '../model/EditScreenshotsProvider';
import {ScreenshotsEditor} from './ScreenshotsEditor';

export interface ScreenshotsEditorSectionProps {
    readonly softwareId: string;
    /** The release whose screenshots these are; none for the software's own. */
    readonly releaseId?: string | null;
    /** The gallery of the software; screenshots by default. */
    readonly gallery?: Gallery;
}

/** The screenshots to change, below a form: asked for once they come into view (docs/data-loading.md). */
export function ScreenshotsEditorSection({softwareId, releaseId = null, gallery = 'screenshots'}: ScreenshotsEditorSectionProps) {
    const [ref, visible] = useInViewport();
    return (
        <div ref={ref}>
            <EditScreenshotsProvider softwareId={softwareId} releaseId={releaseId} gallery={gallery} enabled={visible}>
                <ScreenshotsEditor/>
            </EditScreenshotsProvider>
        </div>
    );
}
