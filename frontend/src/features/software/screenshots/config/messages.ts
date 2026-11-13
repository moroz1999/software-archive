import type {TFunction} from 'i18next';
import type {BusinessMessages} from '@/shared/api';

/** Texts for the business error codes a change of screenshots can receive; others fall back by status. */
export function screenshotsMessages(t: TFunction): BusinessMessages {
    return {
        '401': t('errors.business.signInRequired'),
        '403': t('errors.business.forbidden'),
        '409': t('errors.business.conflict'),
        '422': t('errors.business.refused'),
        'screenshot.format-unsupported': t('screenshots.errors.format'),
        'screenshot.too-large': t('screenshots.errors.size'),
        'screenshot.order-outdated': t('screenshots.errors.orderOutdated'),
    };
}
