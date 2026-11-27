import type {TFunction} from 'i18next';
import type {BusinessMessages} from '@/shared/api';

/** Texts for the business error codes deleting a label can receive; others fall back by status. */
export function deleteLabelMessages(t: TFunction): BusinessMessages {
    return {
        'author.nameless': t('label.makeErrors.onlyName'),
        '401': t('errors.business.signInRequired'),
        '403': t('errors.business.forbidden'),
        '409': t('errors.business.conflict'),
        '422': t('errors.business.refused'),
    };
}
