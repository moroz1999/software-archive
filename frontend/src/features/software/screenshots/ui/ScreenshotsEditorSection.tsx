import type {ReleaseId, SoftwareId} from '@/entities/software';
import {useInViewport} from '@/shared/lib/screen';
import {EditScreenshotsProvider} from '../model/EditScreenshotsProvider';
import {ScreenshotsEditor} from './ScreenshotsEditor';

export interface ScreenshotsEditorSectionProps {
    readonly softwareId: SoftwareId;
    /** The release whose screenshots these are; none for the software's own. */
    readonly releaseId?: ReleaseId | null;
}

/** The screenshots to change, below a form: asked for once they come into view (docs/data-loading.md). */
export function ScreenshotsEditorSection({softwareId, releaseId = null}: ScreenshotsEditorSectionProps) {
    const [ref, visible] = useInViewport();
    return (
        <div ref={ref}>
            <EditScreenshotsProvider softwareId={softwareId} releaseId={releaseId} enabled={visible}>
                <ScreenshotsEditor/>
            </EditScreenshotsProvider>
        </div>
    );
}
