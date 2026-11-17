import type {QueryClient} from '@tanstack/react-query';
import type {LoaderFunctionArgs} from 'react-router';
import {labelDetailsQuery, requireLabelId} from '@/entities/labels';
import {preloadView} from '@/shared/api';

/** Route loader: the label's details are the block at the top of the page; a malformed id is a page that does not exist. */
export function preloadLabelViewPage(queryClient: QueryClient): (args: LoaderFunctionArgs) => Promise<null> {
    return ({params}) => {
        const id = requireLabelId(params['id']);
        return preloadView([queryClient.query(labelDetailsQuery(id))]);
    };
}
