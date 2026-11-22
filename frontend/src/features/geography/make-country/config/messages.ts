import type {TFunction} from 'i18next';
import type {BusinessMessages} from '@/shared/api';

/** Texts for the business error codes making a city a country can receive; others fall back by status. */
export function makeCountryMessages(t: TFunction): BusinessMessages {
    return {
        '401': t('errors.business.signInRequired'),
        '403': t('errors.business.forbidden'),
        '409': t('errors.business.conflict'),
        '422': t('errors.business.refused'),
        'geo.city-has-country': t('errors.geoCityHasCountry'),
        'geo.city-in-use-by-label': t('errors.geoCityInUseByLabel'),
    };
}
