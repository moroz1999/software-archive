import type {TFunction} from 'i18next';
import type {BusinessMessages} from '@/shared/api';

/** Texts for the business error codes changing the groups of a user can receive; others fall back by status. */
export function changeUserGroupsMessages(t: TFunction): BusinessMessages {
    return {
        'user.last-admin': t('users.errors.lastAdmin'),
        'user.unconfirmed-groups': t('users.errors.unconfirmed'),
        '401': t('errors.business.signInRequired'),
        '403': t('users.errors.forbidden'),
        '409': t('errors.business.conflict'),
        '422': t('errors.business.refused'),
    };
}
