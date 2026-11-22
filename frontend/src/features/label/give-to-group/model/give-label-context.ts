import type {LabelId} from '@/entities/labels';
import {createRequiredContext} from '@/shared/lib/context';

/** The label to give to a group, under its name. */
export interface GivenLabel {
    readonly id: LabelId;
    readonly name: string;
}

export interface GiveLabelState {
    readonly label: GivenLabel;
    readonly give: (group: string) => void;
    readonly giving: boolean;
    /** Why the label could not be given. */
    readonly error: string | undefined;
    /** Forgets the refusal of the last try, when the dialog is closed. */
    readonly reset: () => void;
}

/** Giving the label to a group, for the controls inside a GiveLabelProvider. */
export const [GiveLabelContext, useGiveLabel] = createRequiredContext<GiveLabelState>('GiveLabel');
