import {queryOptions} from '@tanstack/react-query';
import {type ImportRun, lastSeq, runRefreshInterval, withNewLines} from '../model/import-run';
import {fetchBasisCategories, fetchLastImportRun, fetchVektorSections} from './import-api';

/** The common prefix of every import query. */
export const IMPORTS_QUERY_KEY = ['imports'] as const;

/** The sections of vektor-06c.ru to choose from. */
export function vektorSectionsQuery() {
    return queryOptions({
        queryKey: [...IMPORTS_QUERY_KEY, 'vektor', 'sections'],
        queryFn: ({signal}) => fetchVektorSections(signal),
    });
}

/** The categories of "Базис" whose cards can be imported. */
export function basisCategoriesQuery() {
    return queryOptions({
        queryKey: [...IMPORTS_QUERY_KEY, 'basis', 'categories'],
        queryFn: ({signal}) => fetchBasisCategories(signal),
    });
}

const LAST_RUN_QUERY_KEY = [...IMPORTS_QUERY_KEY, 'last-run'] as const;

/**
 * The report of the import started last, read again every few seconds while the run goes on: each time only the lines
 * of its log after those already read, added after them.
 */
export function lastImportRunQuery() {
    return queryOptions({
        queryKey: LAST_RUN_QUERY_KEY,
        queryFn: async ({signal, client}) => {
            const previous = client.getQueryData<ImportRun | null>(LAST_RUN_QUERY_KEY);
            return withNewLines(previous, await fetchLastImportRun(lastSeq(previous), signal));
        },
        refetchInterval: (query) => runRefreshInterval(query.state.data),
    });
}
