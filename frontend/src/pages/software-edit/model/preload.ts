import type {LoaderFunctionArgs} from 'react-router';
import {parseSoftwareId} from '../../../entities/software';
import {preloadView} from '../../../shared/api';
import {softwareDetailsRequest} from '../../../widgets/software-editor';

/** Route loader: the software's details are the block at the top of the page; a malformed id loads nothing. */
export function preloadSoftwareEditPage({params}: LoaderFunctionArgs): Promise<null> {
    const id = parseSoftwareId(params['id']);
    return preloadView(id === null ? [] : [softwareDetailsRequest.for(id)]);
}
