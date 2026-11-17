import type {LabelDetails, LabelOwnerOption} from '@/entities/labels';
import type {LocationValue} from '@/entities/locations';
import type {FieldRefusals} from '@/shared/api';
import {createRequiredContext} from '@/shared/lib/context';
import type {LabelFormErrors, LabelFormValues} from './label-form';

export interface EditLabelState {
    /** The label being edited, as it was loaded. */
    readonly label: LabelDetails;
    readonly values: LabelFormValues;
    /** Format errors found on the last submit; a field's error clears once the field is edited. */
    readonly errors: LabelFormErrors;
    /** Why the last submit was refused, under the fields the refusal names; a field's text clears once it is edited. */
    readonly refusals: FieldRefusals;
    /** The form asks whether the label becomes the display name of the person chosen. */
    readonly offersDisplay: boolean;
    readonly changeName: (name: string) => void;
    readonly changeOwner: (owner: LabelOwnerOption | null) => void;
    readonly changeDisplay: (display: boolean) => void;
    readonly changeLocations: (locations: readonly LocationValue[]) => void;
    /** Checks the format rules and saves only when they pass. */
    readonly submit: () => void;
    /** Saving runs: the form is locked. */
    readonly busy: boolean;
    /** Why the last submit was refused, for what no field takes: shown at the top of the form. */
    readonly error: string | undefined;
}

/** The label being edited, for the form inside an EditLabelProvider. */
export const [EditLabelContext, useEditLabel] = createRequiredContext<EditLabelState>('EditLabel');
