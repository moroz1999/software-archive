import type {TFunction} from 'i18next';
import type {BusinessMessages} from '@/shared/api';

/** Texts for the business error codes deleting a country or a city can receive; others fall back by status. */
export function deletePlaceMessages(t: TFunction): BusinessMessages {
    return {
        '401': t('errors.business.signInRequired'),
        '403': t('errors.business.forbidden'),
        '409': t('errors.business.conflict'),
        '422': t('errors.business.refused'),
        'geo.country-in-use': t('errors.geoCountryInUse'),
        'geo.city-in-use': t('errors.geoCityInUse'),
    };
}
