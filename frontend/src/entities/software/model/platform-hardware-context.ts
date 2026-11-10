import type {UseQueryResult} from '@tanstack/react-query';
import {createRequiredContext} from '@/shared/lib/context';
import type {Hardware} from './software';

/** The hardware of a platform, for the blocks inside a PlatformHardwareProvider. */
export const [PlatformHardwareContext, usePlatformHardware] = createRequiredContext<UseQueryResult<readonly Hardware[]>>('PlatformHardware');
