import type {ReactNode} from 'react';
import {useMutation, useQueryClient} from '@tanstack/react-query';
import {useTranslation} from 'react-i18next';
import {type GrantedGroup, type UserEntry, USERS_QUERY_KEY} from '@/entities/users';
import {businessErrorMessage, businessErrorOf} from '@/shared/api';
import {changeUserGroups} from '../api/change-groups-api';
import {changeUserGroupsMessages} from '../config/messages';
import {ChangeUserGroupsContext, type ChangeUserGroupsState} from './change-user-groups-context';

export interface ChangeUserGroupsProviderProps {
    readonly user: UserEntry;
    readonly onChanged: () => void;
    readonly children: ReactNode;
}

/** Changing the groups of a user: the lists of users are read again once it is done. */
export function ChangeUserGroupsProvider({user, onChanged, children}: ChangeUserGroupsProviderProps) {
    const {t} = useTranslation();
    const queryClient = useQueryClient();
    const {mutate, isPending, error, reset} = useMutation({
        mutationFn: (groups: readonly GrantedGroup[]) => changeUserGroups(user.id, groups),
        onSuccess: async () => {
            onChanged();
            await queryClient.invalidateQueries({queryKey: USERS_QUERY_KEY});
        },
    });
    const businessError = businessErrorOf(error);

    const state: ChangeUserGroupsState = {
        user,
        save: (groups) => {
            mutate(groups);
        },
        saving: isPending,
        error: businessError === undefined ? undefined : businessErrorMessage(businessError, changeUserGroupsMessages(t)),
        reset,
    };

    return <ChangeUserGroupsContext value={state}>{children}</ChangeUserGroupsContext>;
}
