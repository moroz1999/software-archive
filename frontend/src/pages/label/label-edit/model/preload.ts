import type {QueryClient} from '@tanstack/react-query';
import type {LoaderFunctionArgs} from 'react-router';
import {labelDetailsQuery, requireLabelId} from '@/entities/labels';
import {countriesQuery} from '@/entities/locations';
import {preloadView} from '@/shared/api';

/** Route loader: the label's details and the countries of its locations are the block at the top of the page; a malformed id is a page that does not exist. */
export function preloadLabelEditPage(queryClient: QueryClient): (args: LoaderFunctionArgs) => Promise<null> {
    return ({params}) => {
        const id = requireLabelId(params['id']);
        return preloadView([queryClient.query(labelDetailsQuery(id)), queryClient.query(countriesQuery())]);
    };
}
