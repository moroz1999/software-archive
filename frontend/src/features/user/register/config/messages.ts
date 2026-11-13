import type {TFunction} from 'i18next';
import type {BusinessMessages} from '@/shared/api';

/** Texts for the business error codes registering can receive; others fall back by status. */
export function registerMessages(t: TFunction): BusinessMessages {
    return {
        '409': t('errors.business.conflict'),
        '422': t('errors.business.refused'),
        'user.name-taken': t('account.nameTaken'),
        'user.email-taken': t('account.emailTaken'),
    };
}
