import {type ReactNode, useState} from 'react';
import {type BusinessError, type BusinessMessages, splitRefusal, unedited} from '@/shared/api';
import type {UiLanguage} from '@/shared/lib/i18n';
import type {HardwareGroupItem, HardwareKind} from './hardware';
import {type HardwareDraft, type HardwareFormErrors, type HardwareFormValues, toHardwareFormValues, validateHardwareForm} from './hardware-form';
import {HardwareFormContext, type HardwareFormState, type HardwarePlatformChoice} from './hardware-form-context';

export interface HardwareFormProviderProps {
    readonly kind: HardwareKind;
    /** Called only with values that pass the format rules. */
    readonly onSubmit: (draft: HardwareDraft) => void;
    readonly initial?: HardwareFormValues;
    /** Editing keeps the code. */
    readonly codeFixed?: boolean;
    readonly groups?: readonly HardwareGroupItem[] | undefined;
    readonly platforms?: readonly HardwarePlatformChoice[] | undefined;
    readonly busy?: boolean;
    /** Why the last submit was refused (docs/error-handling.md). */
    readonly error?: BusinessError | undefined;
    /** The slice's texts for the codes of its business errors. */
    readonly messages?: BusinessMessages;
    readonly children: ReactNode;
}

/** The request fields a refusal can name. */
const FIELDS = ['code', 'group', 'platforms'] as const;

/** The values of the form of a group or of an item of hardware being filled in, their format rules and why the last submit was refused. */
export function HardwareFormProvider({kind, onSubmit, initial, codeFixed = false, groups, platforms, busy = false, error, messages = {}, children}: HardwareFormProviderProps) {
    const [values, setValues] = useState<HardwareFormValues>(() => initial ?? toHardwareFormValues());
    const [errors, setErrors] = useState<HardwareFormErrors>({});
    // The fields edited since the refusal arrived: their refusals no longer apply. A new refusal starts afresh.
    const [edited, setEdited] = useState<{readonly error: BusinessError | undefined; readonly fields: ReadonlySet<string>}>({error, fields: new Set()});
    if (edited.error !== error) {
        setEdited({error, fields: new Set()});
    }
    const refusal = splitRefusal(error, messages, FIELDS);

    function change(patch: Partial<HardwareFormValues>, field?: string) {
        setValues((current) => ({...current, ...patch}));
        setErrors({});
        if (field !== undefined) {
            setEdited((current) => ({...current, fields: new Set([...current.fields, field])}));
        }
    }

    const state: HardwareFormState = {
        kind,
        codeFixed,
        values,
        errors,
        refusals: unedited(refusal.fields, edited.fields),
        groups,
        platforms,
        changeCode: (code) => {
            change({code}, 'code');
        },
        changeName: (language: UiLanguage) => (value) => {
            setValues((current) => ({...current, names: {...current.names, [language]: value}}));
            setErrors({});
        },
        changeGroup: (group) => {
            change({group}, 'group');
        },
        changePlatform: (code, checked) => {
            const others = values.platforms.filter((platform) => platform !== code);
            change({platforms: checked ? [...others, code] : others}, 'platforms');
        },
        submit: () => {
            const result = validateHardwareForm(kind, values);
            if (result.ok) {
                setErrors({});
                onSubmit(result.draft);
            } else {
                setErrors(result.errors);
            }
        },
        busy,
        error: refusal.form,
    };

    return <HardwareFormContext value={state}>{children}</HardwareFormContext>;
}
