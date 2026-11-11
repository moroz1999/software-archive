import type {UseQueryResult} from '@tanstack/react-query';
import {createRequiredContext} from '@/shared/lib/context';

/** The codes of the group types to choose from, for the blocks inside a GroupTypesProvider. */
export const [GroupTypesContext, useGroupTypes] = createRequiredContext<UseQueryResult<readonly string[]>>('GroupTypes');
