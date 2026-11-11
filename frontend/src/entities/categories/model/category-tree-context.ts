import type {UseQueryResult} from '@tanstack/react-query';
import {createRequiredContext} from '@/shared/lib/context';
import type {CategoryNode} from './category';

/** The tree query, for the blocks inside a CategoryTreeProvider. */
export const [CategoryTreeContext, useCategoryTree] = createRequiredContext<UseQueryResult<readonly CategoryNode[]>>('CategoryTree');
