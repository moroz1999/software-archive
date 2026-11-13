import type {TFunction} from 'i18next';
import type {BusinessMessages} from '@/shared/api';

/** The code of the refusal of an account that is not confirmed yet: the user is offered a new link. */
export const UNCONFIRMED = 'user.unconfirmed';

/** Texts for the business error codes signing in can receive; others fall back by status. */
export function signInMessages(t: TFunction): BusinessMessages {
    return {
        '409': t('errors.business.conflict'),
        '422': t('errors.business.refused'),
        'user.credentials-invalid': t('account.credentialsInvalid'),
        [UNCONFIRMED]: t('account.unconfirmed'),
    };
}
