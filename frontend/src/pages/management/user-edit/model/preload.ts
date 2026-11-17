import type {QueryClient} from '@tanstack/react-query';
import type {LoaderFunctionArgs} from 'react-router';
import {requireUserId} from '@/entities/users';
import {userEditQuery} from '@/features/user/user-edit';
import {preloadView} from '@/shared/api';

/** Route loader: the user makes the form; a malformed id is a page that does not exist. */
export function preloadManagementUserEditPage(queryClient: QueryClient): (args: LoaderFunctionArgs) => Promise<null> {
    return ({params}) => {
        const id = requireUserId(params['id']);
        return preloadView([queryClient.query(userEditQuery(id))]);
    };
}
