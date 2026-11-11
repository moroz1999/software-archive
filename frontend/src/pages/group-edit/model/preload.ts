import type {QueryClient} from '@tanstack/react-query';
import type {LoaderFunctionArgs} from 'react-router';
import {groupDetailsQuery, groupTypesQuery, requireGroupId} from '@/entities/groups';
import {countriesQuery} from '@/entities/locations';
import {preloadView} from '@/shared/api';

/** Route loader: the group's details, the group types and the countries of its locations are the block at the top of the page; a malformed id is a page that does not exist. */
export function preloadGroupEditPage(queryClient: QueryClient): (args: LoaderFunctionArgs) => Promise<null> {
    return ({params}) => {
        const id = requireGroupId(params['id']);
        return preloadView([queryClient.query(groupDetailsQuery(id)), queryClient.query(groupTypesQuery()), queryClient.query(countriesQuery())]);
    };
}
