import type {UseQueryResult} from '@tanstack/react-query';
import {createRequiredContext} from '@/shared/lib/context';
import type {AuthorDetails} from './author';

/** The details query of the author, for the blocks inside an AuthorDetailsProvider. */
export const [AuthorDetailsContext, useAuthorDetails] = createRequiredContext<UseQueryResult<AuthorDetails>>('AuthorDetails');
