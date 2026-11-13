import {createRequiredContext} from '@/shared/lib/context';
import type {Ability, CurrentUser} from './user';

export interface CurrentUserState {
    /** The signed-in user; null for a public visitor and while it is not known yet. */
    readonly user: CurrentUser | null;
    /** Whether it is known yet who is signed in. */
    readonly known: boolean;
}

/** Who is signed in, for everything inside the CurrentUserProvider of the app. */
export const [CurrentUserContext, useCurrentUser] = createRequiredContext<CurrentUserState>('CurrentUser');

/** Whether the signed-in user may do this; a public visitor, and anyone while it is not known yet, may not. */
export function useAbility(ability: Ability): boolean {
    return useCurrentUser().user?.abilities.includes(ability) ?? false;
}
