import type {UseQueryResult} from '@tanstack/react-query';
import type {UserEntry} from '@/entities/users';
import {createRequiredContext} from '@/shared/lib/context';

/** The account to edit, for the blocks inside a UserDetailsProvider. */
export const [UserDetailsContext, useUserDetails] = createRequiredContext<UseQueryResult<UserEntry>>('UserDetails');
