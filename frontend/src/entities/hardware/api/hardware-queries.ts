import {queryOptions} from '@tanstack/react-query';
import type {HardwareCode, HardwareGroupCode, HardwareKind} from '../model/hardware';
import {fetchHardwareDetails, fetchHardwareGroups} from './hardware-api';

/** The common prefix of every query of the reference book as the management keeps it: a change makes them all stale. */
export const HARDWARE_REFERENCE_QUERY_KEY = ['hardware-reference'] as const;

/** Every group with its hardware, the list of the management and the groups of the form. */
export function hardwareGroupsQuery() {
    return queryOptions({
        queryKey: [...HARDWARE_REFERENCE_QUERY_KEY, 'groups'],
        queryFn: ({signal}) => fetchHardwareGroups(signal),
    });
}

/** One group or item of hardware, the block of its form. */
export function hardwareDetailsQuery(kind: HardwareKind, code: HardwareCode | HardwareGroupCode) {
    return queryOptions({
        queryKey: [...HARDWARE_REFERENCE_QUERY_KEY, kind, code],
        queryFn: ({signal}) => fetchHardwareDetails(kind, code, signal),
    });
}
