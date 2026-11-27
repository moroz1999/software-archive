import type {ReactNode} from 'react';
import {useQuery} from '@tanstack/react-query';
import {softwareDetailsQuery} from '../api/software-queries';
import {SoftwareDetailsContext} from './software-details-context';

export interface SoftwareDetailsProviderProps {
    readonly id: string;
    readonly children: ReactNode;
}

/** Loads one software's details for the blocks inside; takes over the call its route preloaded. */
export function SoftwareDetailsProvider({id, children}: SoftwareDetailsProviderProps) {
    const query = useQuery(softwareDetailsQuery(id));
    return <SoftwareDetailsContext value={query}>{children}</SoftwareDetailsContext>;
}
