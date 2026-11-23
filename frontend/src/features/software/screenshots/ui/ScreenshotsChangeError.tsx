import {Text} from '@/shared/ui/typography';
import {useEditScreenshots} from '../model/edit-screenshots-context';

/** Why the last change of the screenshots inside an EditScreenshotsProvider failed; nothing when it did not. */
export function ScreenshotsChangeError() {
    const {error} = useEditScreenshots();
    return error === undefined ? null : <Text role="alert" tone="danger">{error}</Text>;
}
