import type {TFunction} from 'i18next';
import type {BusinessMessages} from '@/shared/api';

/** Texts for the business error codes merging a country or a city can receive; others fall back by status. */
export function mergePlaceMessages(t: TFunction): BusinessMessages {
    return {
        '401': t('errors.business.signInRequired'),
        '403': t('errors.business.forbidden'),
        '404': t('errors.business.conflict'),
        '409': t('errors.business.conflict'),
        '422': t('errors.business.refused'),
        'geo.country-unknown': t('errors.geoCountryUnknown'),
        'geo.city-unknown': t('errors.geoCityUnknown'),
        'geo.merge-into-itself': t('errors.geoMergeIntoItself'),
    };
}
