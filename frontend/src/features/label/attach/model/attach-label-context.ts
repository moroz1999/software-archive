import type {LabelId, LabelOwnerOption} from '@/entities/labels';
import {createRequiredContext} from '@/shared/lib/context';

/** The label to attach, under its name. */
export interface AttachedLabel {
    readonly id: LabelId;
    readonly name: string;
}

export interface AttachLabelState {
    readonly label: AttachedLabel;
    readonly attach: (owner: LabelOwnerOption, display: boolean) => void;
    readonly attaching: boolean;
    /** Why the label could not be attached. */
    readonly error: string | undefined;
    /** Forgets the refusal of the last try, when the dialog is closed. */
    readonly reset: () => void;
}

/** Attaching the label, for the controls inside an AttachLabelProvider. */
export const [AttachLabelContext, useAttachLabel] = createRequiredContext<AttachLabelState>('AttachLabel');
