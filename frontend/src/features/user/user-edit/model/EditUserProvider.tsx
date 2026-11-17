import {type ReactNode, useState} from 'react';
import {useMutation, useQueryClient} from '@tanstack/react-query';
import {useTranslation} from 'react-i18next';
import {type UserEntry, USERS_QUERY_KEY} from '@/entities/users';
import {businessErrorOf, splitRefusal, unedited} from '@/shared/api';
import {updateUser} from '../api/user-edit-api';
import {editUserMessages} from '../config/messages';
import {EditUserContext, type EditUserState} from './edit-user-context';
import {type UserDraft, type UserFormErrors, type UserFormValues, toUserFormValues, validateUserForm} from './user-form';

export interface EditUserProviderProps {
    readonly user: UserEntry;
    readonly onSaved: () => void;
    readonly children: ReactNode;
}

function withoutError(errors: UserFormErrors, field: keyof UserFormErrors): UserFormErrors {
    const {name, email} = errors;
    return {
        ...(field === 'name' || name === undefined ? {} : {name}),
        ...(field === 'email' || email === undefined ? {} : {email}),
    };
}

/** Saving the name, the email and the groups of a user: the lists of users go stale once saved. */
export function EditUserProvider({user, onSaved, children}: EditUserProviderProps) {
    const {t} = useTranslation();
    const queryClient = useQueryClient();
    const [values, setValues] = useState<UserFormValues>(() => toUserFormValues(user));
    const [errors, setErrors] = useState<UserFormErrors>({});
    const [edited, setEdited] = useState<ReadonlySet<string>>(new Set());
    const {mutate, isPending, error} = useMutation({
        mutationFn: (draft: UserDraft) => updateUser(user.id, draft),
        onMutate: () => {
            setEdited(new Set());
        },
        onSuccess: async () => {
            await queryClient.invalidateQueries({queryKey: USERS_QUERY_KEY, refetchType: 'none'});
            onSaved();
        },
    });
    const refusal = splitRefusal(businessErrorOf(error), editUserMessages(t), ['name', 'email']);

    const state: EditUserState = {
        user,
        values,
        errors,
        refusals: unedited(refusal.fields, edited),
        change: (field) => (value) => {
            setValues((current) => ({...current, [field]: value}));
            setErrors((current) => withoutError(current, field));
            setEdited((current) => new Set([...current, field]));
        },
        toggleGroup: (group, on) => {
            setValues((current) => {
                const groups = new Set(current.groups);
                if (on) {
                    groups.add(group);
                } else {
                    groups.delete(group);
                }
                return {...current, groups};
            });
        },
        submit: () => {
            const result = validateUserForm(values);
            if (result.ok) {
                setErrors({});
                mutate(result.draft);
            } else {
                setErrors(result.errors);
            }
        },
        busy: isPending,
        error: refusal.form,
    };

    return <EditUserContext value={state}>{children}</EditUserContext>;
}
