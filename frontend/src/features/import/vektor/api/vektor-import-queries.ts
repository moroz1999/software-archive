import {queryOptions} from '@tanstack/react-query';
import {fetchVektorSections} from './vektor-import-api';

/** The sections of vektor-06c.ru to choose from. */
export function vektorSectionsQuery() {
    return queryOptions({
        queryKey: ['imports', 'vektor', 'sections'],
        queryFn: ({signal}) => fetchVektorSections(signal),
    });
}
