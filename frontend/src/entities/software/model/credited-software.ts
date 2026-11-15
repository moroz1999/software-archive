import type {Software} from './software';

/** A person, a group or a label whose page lists the software they are credited on; `kind` is the part of their address. */
export interface CreditedOwner {
    readonly kind: 'authors' | 'groups' | 'labels';
    readonly id: string;
}

/** One credit of the owner on the software: by themselves or under a label, named only when it is not their main name. */
export interface OwnCredit {
    readonly as: string | null;
    readonly roles: readonly string[];
}

/** Software on the page of a person or a group: its card, and their credits on it. */
export interface CreditedSoftware extends Software {
    readonly credits: readonly OwnCredit[];
}
