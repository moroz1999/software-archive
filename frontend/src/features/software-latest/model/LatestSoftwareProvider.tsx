import type {ReactNode} from 'react';
import {useQuery} from '@tanstack/react-query';
import {latestSoftwareQuery} from '../api/latest-software-query';
import {LatestSoftwareContext} from './latest-software-context';

/** Loads the latest software for the blocks inside; takes over the call its route preloaded. */
export function LatestSoftwareProvider({children}: {readonly children: ReactNode}) {
    const query = useQuery(latestSoftwareQuery());
    return <LatestSoftwareContext value={query}>{children}</LatestSoftwareContext>;
}
