import {fetchSoftwarePage} from '../../../entities/software';
import {viewRequest} from '../../../shared/api';

/** The software list call; the route preloads it when the list is at the top of the view. */
export const softwareListRequest = viewRequest((signal) => fetchSoftwarePage(0, signal));
