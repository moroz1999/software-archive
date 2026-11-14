import type {ReactNode} from 'react';
import {useQuery} from '@tanstack/react-query';
import {creditedSoftwareQuery} from '../api/software-queries';
import type {CreditedOwner} from './credited-software';
import {CreditedSoftwareContext} from './credited-software-context';

export interface CreditedSoftwareProviderProps {
    readonly owner: CreditedOwner;
    /** The block has come into view: only then is the software asked for (docs/data-loading.md). */
    readonly enabled: boolean;
    readonly children: ReactNode;
}

/** Loads the software a person or a group is credited on for the blocks inside. */
export function CreditedSoftwareProvider({owner, enabled, children}: CreditedSoftwareProviderProps) {
    const query = useQuery({...creditedSoftwareQuery(owner), enabled});
    return <CreditedSoftwareContext value={query}>{children}</CreditedSoftwareContext>;
}
