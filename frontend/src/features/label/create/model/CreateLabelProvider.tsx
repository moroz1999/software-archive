import {type ReactNode, useState} from 'react';
import {useMutation, useQueryClient} from '@tanstack/react-query';
import {useTranslation} from 'react-i18next';
import {AUTHOR_QUERY_KEY} from '@/entities/authors';
import {createLabel} from '@/entities/labels';
import {businessErrorOf, splitRefusal, unedited} from '@/shared/api';
import {createLabelMessages} from '../config/messages';
import {CreateLabelContext, type CreateLabelState} from './create-label-context';
import {EMPTY_NEW_LABEL, type NewLabelFormErrors, type NewLabelFormValues, validateNewLabelForm} from './new-label-form';

/** The fields of the form a refusal can name. */
const REFUSED_FIELDS = ['locations'] as const;

export interface CreateLabelProviderProps {
    readonly onCreated: (id: string) => void;
    readonly children: ReactNode;
}

/** Adding a label of nobody with its locations: the author catalog, where it is listed, goes stale once it is added. */
export function CreateLabelProvider({onCreated, children}: CreateLabelProviderProps) {
    const {t} = useTranslation();
    const queryClient = useQueryClient();
    const [values, setValues] = useState<NewLabelFormValues>(EMPTY_NEW_LABEL);
    const [errors, setErrors] = useState<NewLabelFormErrors>({});
    const [edited, setEdited] = useState<ReadonlySet<string>>(new Set());
    const {mutate, isPending, error} = useMutation({
        mutationFn: createLabel,
        onMutate: () => {
            setEdited(new Set());
        },
        onSuccess: async (id) => {
            await queryClient.invalidateQueries({queryKey: AUTHOR_QUERY_KEY, refetchType: 'none'});
            onCreated(id);
        },
    });
    const refusal = splitRefusal(businessErrorOf(error), createLabelMessages(t), REFUSED_FIELDS);

    const state: CreateLabelState = {
        values,
        errors,
        refusals: unedited(refusal.fields, edited),
        changeName: (name) => {
            setValues((current) => ({...current, name}));
            setErrors(({locations}) => (locations === undefined ? {} : {locations}));
        },
        changeLocations: (locations) => {
            setValues((current) => ({...current, locations}));
            setErrors(({name}) => (name === undefined ? {} : {name}));
            setEdited((current) => new Set([...current, 'locations']));
        },
        submit: () => {
            const result = validateNewLabelForm(values);
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

    return <CreateLabelContext value={state}>{children}</CreateLabelContext>;
}
