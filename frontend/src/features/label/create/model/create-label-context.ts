import type {LocationValue} from '@/entities/locations';
import type {FieldRefusals} from '@/shared/api';
import {createRequiredContext} from '@/shared/lib/context';
import type {NewLabelFormErrors, NewLabelFormValues} from './new-label-form';

export interface CreateLabelState {
    readonly values: NewLabelFormValues;
    /** Format errors found on the last submit; a field's error clears once the field is edited. */
    readonly errors: NewLabelFormErrors;
    /** Why the last submit was refused, under the fields the refusal names; a field's text clears once it is edited. */
    readonly refusals: FieldRefusals;
    readonly changeName: (name: string) => void;
    readonly changeLocations: (locations: readonly LocationValue[]) => void;
    /** Checks the format rules and adds the label only when they pass. */
    readonly submit: () => void;
    /** Adding runs: the form is locked. */
    readonly busy: boolean;
    /** Why the last submit was refused, for what no field takes: shown at the top of the form. */
    readonly error: string | undefined;
}

/** The label being added, for the form inside a CreateLabelProvider. */
export const [CreateLabelContext, useCreateLabel] = createRequiredContext<CreateLabelState>('CreateLabel');
