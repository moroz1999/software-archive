import type {UseQueryResult} from '@tanstack/react-query';
import {createRequiredContext} from '@/shared/lib/context';
import type {HardwareDetails, HardwareGroupItem} from './hardware';

/** Every group with its hardware, for the blocks inside a HardwareGroupsProvider. */
export const [HardwareGroupsContext, useHardwareGroups] = createRequiredContext<UseQueryResult<readonly HardwareGroupItem[]>>('HardwareGroups');

/** The details query of a group or an item of hardware, for the blocks inside a HardwareDetailsProvider. */
export const [HardwareDetailsContext, useHardwareDetails] = createRequiredContext<UseQueryResult<HardwareDetails>>('HardwareDetails');
