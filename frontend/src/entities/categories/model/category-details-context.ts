import type {UseQueryResult} from '@tanstack/react-query';
import {createRequiredContext} from '@/shared/lib/context';
import type {CategoryDetails} from './category';

/** The details query of the category, for the blocks inside a CategoryDetailsProvider. */
export const [CategoryDetailsContext, useCategoryDetails] = createRequiredContext<UseQueryResult<CategoryDetails>>('CategoryDetails');
