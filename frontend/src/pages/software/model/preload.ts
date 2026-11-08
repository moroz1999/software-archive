import {softwareListRequest} from '../../../features/software-list';
import {preloadView} from '../../../shared/api';

/** Route loader: the software list is the block at the top of the page. */
export function preloadSoftwarePage(): Promise<null> {
    return preloadView([softwareListRequest]);
}
