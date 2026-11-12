import type {TFunction} from 'i18next';
import type {BusinessMessages} from '@/shared/api';

/** Texts for the business error codes adding software can receive; others fall back by status. */
export function createSoftwareMessages(t: TFunction): BusinessMessages {
    return {
        '409': t('errors.business.conflict'),
        '422': t('errors.business.refused'),
        'hardware.unknown': t('errors.hardwareUnknown'),
        'category.unknown': t('errors.categoryUnknown'),
    };
}
