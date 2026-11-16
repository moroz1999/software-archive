import type {LabelId, NamedLabelOwner} from '@/entities/labels';
import {createRequiredContext} from '@/shared/lib/context';

/** The label to make an owner of, under its name, with whoever has it now. */
export interface MadeOwnerLabel {
    readonly id: LabelId;
    readonly name: string;
    readonly owner: NamedLabelOwner | null;
}

export interface MakeOwnerState {
    readonly label: MadeOwnerLabel;
    readonly makeAuthor: () => void;
    readonly makeGroup: (types: readonly string[]) => void;
    readonly making: boolean;
    /** Why no owner could be made of the label. */
    readonly error: string | undefined;
    /** Forgets the refusal of the last try, when the dialog is closed. */
    readonly reset: () => void;
}

/** Making a person or a group of the label, for the controls inside a MakeOwnerProvider. */
export const [MakeOwnerContext, useMakeOwner] = createRequiredContext<MakeOwnerState>('MakeOwner');
