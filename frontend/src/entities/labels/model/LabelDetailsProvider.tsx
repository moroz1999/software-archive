import type {ReactNode} from 'react';
import {useQuery} from '@tanstack/react-query';
import {labelDetailsQuery} from '../api/label-queries';
import type {LabelId} from './label-id';
import {LabelDetailsContext} from './label-details-context';

export interface LabelDetailsProviderProps {
    readonly id: LabelId;
    readonly children: ReactNode;
}

/** Loads one label's details for the blocks inside; takes over the call its route preloaded. */
export function LabelDetailsProvider({id, children}: LabelDetailsProviderProps) {
    const query = useQuery(labelDetailsQuery(id));
    return <LabelDetailsContext value={query}>{children}</LabelDetailsContext>;
}
