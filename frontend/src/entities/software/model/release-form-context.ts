import {createRequiredContext} from '@/shared/lib/context';
import type {Hardware} from './software';
import type {ReleaseFormErrors, ReleaseFormValues} from './release-form';

/** The fields of the release form a refusal can name. */
export type ReleaseFormField = keyof ReleaseFormValues;

/** The texts of a refusal under the fields it names. */
export type ReleaseFormRefusals = Readonly<Partial<Record<ReleaseFormField, string>>>;

export interface ReleaseFormState {
    readonly values: ReleaseFormValues;
    /** Format errors found on the last submit; a field's error clears once the field is edited. */
    readonly errors: ReleaseFormErrors;
    /** Why the last submit was refused, under the fields the refusal names; a field's text clears once it is edited. */
    readonly refusals: ReleaseFormRefusals;
    /** The change handler of a text field. */
    readonly change: (field: Exclude<ReleaseFormField, 'hardware'>) => (value: string) => void;
    /** Replaces the chosen hardware. */
    readonly changeHardware: (codes: readonly string[]) => void;
    /** Checks the format rules and hands the draft on only when they pass. */
    readonly submit: () => void;
    /** The hardware to choose from: of the platform of the software. */
    readonly hardware: readonly Hardware[];
    /** Saving runs: the form is locked. */
    readonly busy: boolean;
    /** Why the last submit was refused, for what no field takes: shown at the top of the form. */
    readonly error: string | undefined;
}

/** The release form being filled in, for the fields inside a ReleaseFormProvider. */
export const [ReleaseFormContext, useReleaseForm] = createRequiredContext<ReleaseFormState>('ReleaseForm');
