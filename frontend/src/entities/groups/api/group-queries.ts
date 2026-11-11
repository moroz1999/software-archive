import {queryOptions} from '@tanstack/react-query';
import type {GroupId} from '../model/group';
import {fetchGroup, fetchGroupTypes} from './group-api';

/** The common prefix of every group query. */
export const GROUP_QUERY_KEY = ['group'] as const;

/** One group with its details, the block at the top of its page. */
export function groupDetailsQuery(id: GroupId) {
    return queryOptions({
        queryKey: [...GROUP_QUERY_KEY, 'details', id],
        queryFn: ({signal}) => fetchGroup(id, signal),
    });
}

/** The reference book of group types to choose from. */
export function groupTypesQuery() {
    return queryOptions({
        queryKey: ['group-types'],
        queryFn: ({signal}) => fetchGroupTypes(signal),
    });
}
