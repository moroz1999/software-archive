import type {ReactNode} from 'react';
import {useQuery} from '@tanstack/react-query';
import {creditedReleasesQuery} from '../api/software-queries';
import type {CreditedOwner} from './credited-software';
import {CreditedReleasesContext} from './credited-releases-context';

export interface CreditedReleasesProviderProps {
    readonly owner: CreditedOwner;
    /** The block has come into view: only then are the releases asked for (docs/data-loading.md). */
    readonly enabled: boolean;
    readonly children: ReactNode;
}

/** Loads the releases a person, a group or a label is credited on for the blocks inside. */
export function CreditedReleasesProvider({owner, enabled, children}: CreditedReleasesProviderProps) {
    const query = useQuery({...creditedReleasesQuery(owner), enabled});
    return <CreditedReleasesContext value={query}>{children}</CreditedReleasesContext>;
}
