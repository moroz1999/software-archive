import type {Software} from '@/entities/software';
import type {UseQueryResult} from '@tanstack/react-query';
import type {Page} from '@/shared/api';
import {createRequiredContext} from '@/shared/lib/context';

/** The query of the first page of the archive's software, for the blocks inside a SoftwareListProvider. */
export const [SoftwareListContext, useSoftwareList] = createRequiredContext<UseQueryResult<Page<Software>>>('SoftwareList');
