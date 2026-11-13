import {createRequiredContext} from '@/shared/lib/context';

/** How confirming a link went: still running, done, or why the link does not work. */
export type ConfirmationOutcome = 'confirming' | 'confirmed' | 'expired' | 'invalid' | 'failed';

/** The confirmation of a link, for the markup inside a ConfirmEmailProvider. */
export const [ConfirmEmailContext, useConfirmEmail] = createRequiredContext<ConfirmationOutcome>('ConfirmEmail');
