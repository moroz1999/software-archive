import {createRequiredContext} from '@/shared/lib/context';
import type {UiLanguage} from '@/shared/lib/i18n';
import type {HardwareGroupItem, HardwareKind} from './hardware';
import type {HardwareFormErrors, HardwareFormValues} from './hardware-form';

/** A platform an item of hardware can belong to. */
export interface HardwarePlatformChoice {
    readonly code: string;
    readonly name: string;
}

/** Why the last submit was refused for a field, until that field is edited. */
export interface HardwareFieldRefusals {
    readonly code?: string;
    readonly group?: string;
    readonly platforms?: string;
}

export interface HardwareFormState {
    /** A group or an item: only an item has a group and platforms. */
    readonly kind: HardwareKind;
    /** Editing keeps the code: it is shown, not changed. */
    readonly codeFixed: boolean;
    readonly values: HardwareFormValues;
    /** Format errors found on the last submit; they clear once the form is edited. */
    readonly errors: HardwareFormErrors;
    readonly refusals: HardwareFieldRefusals;
    /** The groups to choose from, in their order; undefined while they load. */
    readonly groups: readonly HardwareGroupItem[] | undefined;
    /** The platforms to choose from; undefined while they load. */
    readonly platforms: readonly HardwarePlatformChoice[] | undefined;
    readonly changeCode: (value: string) => void;
    readonly changeName: (language: UiLanguage) => (value: string) => void;
    readonly changeGroup: (value: string) => void;
    readonly changePlatform: (code: string, checked: boolean) => void;
    /** Checks the format rules and hands the draft on only when they pass. */
    readonly submit: () => void;
    /** Saving runs: the form is locked. */
    readonly busy: boolean;
    /** Why the last submit was refused, for what no field takes: shown at the top of the form. */
    readonly error: string | undefined;
}

/** The form being filled in, for the fields inside a HardwareFormProvider. */
export const [HardwareFormContext, useHardwareForm] = createRequiredContext<HardwareFormState>('HardwareForm');
