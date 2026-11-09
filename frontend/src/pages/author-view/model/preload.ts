import type {QueryClient} from '@tanstack/react-query';
import type {LoaderFunctionArgs} from 'react-router';
import {authorDetailsQuery, requireAuthorId} from '@/entities/authors';
import {preloadView} from '@/shared/api';

/** Route loader: the author's details are the block at the top of the page; a malformed id is a page that does not exist. */
export function preloadAuthorViewPage(queryClient: QueryClient): (args: LoaderFunctionArgs) => Promise<null> {
    return ({params}) => {
        const id = requireAuthorId(params['id']);
        return preloadView([queryClient.query(authorDetailsQuery(id))]);
    };
}
