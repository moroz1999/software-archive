import type {ReactNode} from 'react';
import {useQuery} from '@tanstack/react-query';
import type {SoftwareId} from '@/entities/software';
import {softwareVotesQuery} from '../api/vote-queries';
import {SoftwareVotesContext} from './software-votes-context';

export interface SoftwareVotesProviderProps {
    readonly id: SoftwareId;
    /** The block has come into view: only then are the votes asked for (docs/data-loading.md). */
    readonly enabled: boolean;
    readonly children: ReactNode;
}

/** The votes of one software, asked for once their block comes into view (docs/data-loading.md). */
export function SoftwareVotesProvider({id, enabled, children}: SoftwareVotesProviderProps) {
    const query = useQuery({...softwareVotesQuery(id), enabled});
    return <SoftwareVotesContext value={query}>{children}</SoftwareVotesContext>;
}
