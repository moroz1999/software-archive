import type {QueryClient} from '@tanstack/react-query';
import type {LoaderFunctionArgs} from 'react-router';
import {authorDetailsQuery, requireAuthorId} from '@/entities/authors';
import {countriesQuery} from '@/entities/locations';
import {preloadView} from '@/shared/api';

/** Route loader: the author's details and the countries of their locations are the block at the top of the page; a malformed id is a page that does not exist. */
export function preloadAuthorEditPage(queryClient: QueryClient): (args: LoaderFunctionArgs) => Promise<null> {
    return ({params}) => {
        const id = requireAuthorId(params['id']);
        return preloadView([queryClient.query(authorDetailsQuery(id)), queryClient.query(countriesQuery())]);
    };
}
