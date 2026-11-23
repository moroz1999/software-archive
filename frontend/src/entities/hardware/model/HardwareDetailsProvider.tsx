import type {ReactNode} from 'react';
import {useQuery} from '@tanstack/react-query';
import {hardwareDetailsQuery} from '../api/hardware-queries';
import type {HardwareCode, HardwareGroupCode, HardwareKind} from './hardware';
import {HardwareDetailsContext} from './hardware-context';

export interface HardwareDetailsProviderProps {
    readonly kind: HardwareKind;
    readonly code: HardwareCode | HardwareGroupCode;
    readonly children: ReactNode;
}

/** Loads one group or item of hardware for the blocks inside; takes over the call its route preloaded. */
export function HardwareDetailsProvider({kind, code, children}: HardwareDetailsProviderProps) {
    const query = useQuery(hardwareDetailsQuery(kind, code));
    return <HardwareDetailsContext value={query}>{children}</HardwareDetailsContext>;
}
