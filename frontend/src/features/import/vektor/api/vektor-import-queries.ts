import {queryOptions} from '@tanstack/react-query';
import {runRefreshInterval} from '../model/import-run';
import {fetchLastImportRun, fetchVektorSections} from './vektor-import-api';

/** The common prefix of every import query. */
export const IMPORTS_QUERY_KEY = ['imports'] as const;

/** The sections of vektor-06c.ru to choose from. */
export function vektorSectionsQuery() {
    return queryOptions({
        queryKey: [...IMPORTS_QUERY_KEY, 'vektor', 'sections'],
        queryFn: ({signal}) => fetchVektorSections(signal),
    });
}

/** The report of the import started last, read again every few seconds while the run goes on. */
export function lastImportRunQuery() {
    return queryOptions({
        queryKey: [...IMPORTS_QUERY_KEY, 'last-run'],
        queryFn: ({signal}) => fetchLastImportRun(signal),
        refetchInterval: (query) => runRefreshInterval(query.state.data),
    });
}
