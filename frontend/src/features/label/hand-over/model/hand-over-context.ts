import type {LabelId} from '@/entities/labels';
import {createRequiredContext} from '@/shared/lib/context';

/** The label to hand over, under its name. */
export interface HandedLabel {
    readonly id: LabelId;
    readonly name: string;
}

export interface HandOverLabelState {
    readonly label: HandedLabel;
    /** Hands the label over to the group, the person or the other label of this id. */
    readonly handOver: (to: string) => void;
    readonly handingOver: boolean;
    /** Why the label could not be handed over. */
    readonly error: string | undefined;
    /** Forgets the refusal of the last try, when the dialog is closed. */
    readonly reset: () => void;
}

/** Handing the label over, for the controls inside a HandOverLabelProvider. */
export const [HandOverLabelContext, useHandOverLabel] = createRequiredContext<HandOverLabelState>('HandOverLabel');
