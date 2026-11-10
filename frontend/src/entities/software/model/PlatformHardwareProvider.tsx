import type {ReactNode} from 'react';
import {useQuery} from '@tanstack/react-query';
import {platformHardwareQuery} from '../api/software-queries';
import {PlatformHardwareContext} from './platform-hardware-context';
import type {SoftwarePlatformCode} from './software';

export interface PlatformHardwareProviderProps {
    readonly platform: SoftwarePlatformCode;
    readonly children: ReactNode;
}

/** Loads the hardware of a platform for the blocks inside; takes over the call its route preloaded. */
export function PlatformHardwareProvider({platform, children}: PlatformHardwareProviderProps) {
    const query = useQuery(platformHardwareQuery(platform));
    return <PlatformHardwareContext value={query}>{children}</PlatformHardwareContext>;
}
