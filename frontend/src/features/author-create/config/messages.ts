import type {TFunction} from 'i18next';
import type {BusinessMessages} from '@/shared/api';

/** Texts for the business error codes adding an author can receive; others fall back by status. */
export function createAuthorMessages(t: TFunction): BusinessMessages {
    return {
        '409': t('errors.business.conflict'),
        '422': t('errors.business.refused'),
        'author.nameless': t('validation.authorNameless'),
        'label.not-own': t('errors.labelNotOwn'),
        'label.display-one': t('errors.labelDisplayOne'),
        'location.unknown': t('errors.locationUnknown'),
        'location.primary-one': t('errors.locationPrimaryOne'),
    };
}
