import {type ReactNode, useState} from 'react';
import {useMutation, useQueryClient} from '@tanstack/react-query';
import {useTranslation} from 'react-i18next';
import {AUTHOR_QUERY_KEY} from '@/entities/authors';
import {GROUP_QUERY_KEY} from '@/entities/groups';
import {LABEL_QUERY_KEY, type LabelDetails, type LabelDraft, updateLabel} from '@/entities/labels';
import {SOFTWARE_QUERY_KEY} from '@/entities/software';
import {businessErrorOf, splitRefusal, unedited} from '@/shared/api';
import {editLabelMessages} from '../config/messages';
import {EditLabelContext, type EditLabelState} from './edit-label-context';
import {type LabelFormErrors, type LabelFormValues, offersDisplay, toLabelFormValues, validateLabelForm} from './label-form';

/** The fields of the form a refusal can name. */
const REFUSED_FIELDS = ['owner', 'locations'] as const;

export interface EditLabelProviderProps {
    readonly label: LabelDetails;
    readonly onSaved: () => void;
    readonly children: ReactNode;
}

/**
 * Saving the name, the owner and the locations of a label: the label, its owners old and new, the catalogs and the
 * software credited under it read differently afterwards.
 */
export function EditLabelProvider({label, onSaved, children}: EditLabelProviderProps) {
    const {t} = useTranslation();
    const queryClient = useQueryClient();
    const [values, setValues] = useState<LabelFormValues>(() => toLabelFormValues(label));
    const [errors, setErrors] = useState<LabelFormErrors>({});
    const [edited, setEdited] = useState<ReadonlySet<string>>(new Set());
    const {mutate, isPending, error} = useMutation({
        mutationFn: (draft: LabelDraft) => updateLabel(label.id, draft),
        onMutate: () => {
            setEdited(new Set());
        },
        onSuccess: async () => {
            await Promise.all([LABEL_QUERY_KEY, AUTHOR_QUERY_KEY, GROUP_QUERY_KEY, SOFTWARE_QUERY_KEY].map((queryKey) =>
                queryClient.invalidateQueries({queryKey, refetchType: 'none'})));
            onSaved();
        },
    });
    const refusal = splitRefusal(businessErrorOf(error), editLabelMessages(t), REFUSED_FIELDS);

    function markEdited(field: string) {
        setEdited((current) => new Set([...current, field]));
    }

    const state: EditLabelState = {
        label,
        values,
        errors,
        refusals: unedited(refusal.fields, edited),
        offersDisplay: offersDisplay(values, label),
        changeName: (name) => {
            setValues((current) => ({...current, name}));
            setErrors(({locations}) => (locations === undefined ? {} : {locations}));
        },
        changeOwner: (owner) => {
            setValues((current) => ({...current, owner, display: false}));
            markEdited('owner');
        },
        changeDisplay: (display) => {
            setValues((current) => ({...current, display}));
            markEdited('owner');
        },
        changeLocations: (locations) => {
            setValues((current) => ({...current, locations}));
            setErrors(({name}) => (name === undefined ? {} : {name}));
            markEdited('locations');
        },
        submit: () => {
            const result = validateLabelForm(values, label);
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

    return <EditLabelContext value={state}>{children}</EditLabelContext>;
}
