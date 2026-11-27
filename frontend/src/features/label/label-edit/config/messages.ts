import type {TFunction} from 'i18next';
import type {BusinessMessages} from '@/shared/api';

/** Texts for the business error codes saving a label can receive; others fall back by status. */
export function editLabelMessages(t: TFunction): BusinessMessages {
    return {
        'label.owner-unknown': t('label.errors.ownerUnknown'),
        'author.nameless': t('label.makeErrors.onlyName'),
        'location.unknown': t('errors.locationUnknown'),
        'location.primary-one': t('errors.locationPrimaryOne'),
        '401': t('errors.business.signInRequired'),
        '403': t('errors.business.forbidden'),
        '409': t('errors.business.conflict'),
        '422': t('errors.business.refused'),
    };
}
