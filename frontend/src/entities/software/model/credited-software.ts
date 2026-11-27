import type {components} from '@/shared/api';

/** A person, a group or a label whose page lists the software they are credited on; `kind` is the part of their address. */
export interface CreditedOwner {
    readonly kind: 'authors' | 'groups' | 'labels';
    readonly id: string;
}

/** One credit of the owner on the software: by themselves or under a label, named only when it is not their main name. */
export type OwnCredit = components['schemas']['CreditedSoftwareDto.OwnCreditDto'];

/** Software on the page of a person or a group: its card, and their credits on it. */
export type CreditedSoftware = components['schemas']['CreditedSoftwareDto'];
