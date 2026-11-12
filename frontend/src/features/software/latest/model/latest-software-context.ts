import type {UseQueryResult} from '@tanstack/react-query';
import type {Software} from '@/entities/software';
import type {Page} from '@/shared/api';
import {createRequiredContext} from '@/shared/lib/context';

/** The latest software query, for the blocks inside a LatestSoftwareProvider. */
export const [LatestSoftwareContext, useLatestSoftware] = createRequiredContext<UseQueryResult<Page<Software>>>('LatestSoftware');
