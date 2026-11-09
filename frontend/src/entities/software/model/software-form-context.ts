import {createRequiredContext} from '@/shared/lib/context';
import type {PlatformChoice, SoftwareFormErrors, SoftwareFormValues} from './software-form';

/** The texts of a refusal under the fields it names. */
export type SoftwareFormRefusals = Readonly<Partial<Record<keyof SoftwareFormValues, string>>>;

export interface SoftwareFormState {
    readonly values: SoftwareFormValues;
    /** Format errors found on the last submit; a field's error clears once the field is edited. */
    readonly errors: SoftwareFormErrors;
    /** Why the last submit was refused, under the fields the refusal names; a field's text clears once it is edited. */
    readonly refusals: SoftwareFormRefusals;
    /** The change handler of a field. */
    readonly change: (field: keyof SoftwareFormValues) => (value: string) => void;
    /** Checks the format rules and hands the draft on only when they pass. */
    readonly submit: () => void;
    /** The platforms to choose from. */
    readonly platforms: readonly PlatformChoice[];
    /** Saving runs: the form is locked. */
    readonly busy: boolean;
    /** Why the last submit was refused, for what no field takes: shown at the top of the form. */
    readonly error: string | undefined;
}

/** The form being filled in, for the fields inside a SoftwareFormProvider. */
export const [SoftwareFormContext, useSoftwareForm] = createRequiredContext<SoftwareFormState>('SoftwareForm');
