import type {TFunction} from 'i18next';
import type {BusinessMessages} from '@/shared/api';

/** Texts for the business error codes saving a category can receive; others fall back by status. */
export function editCategoryMessages(t: TFunction): BusinessMessages {
    return {
        '409': t('errors.business.conflict'),
        '422': t('errors.business.refused'),
        'category.parent-unknown': t('errors.categoryParentUnknown'),
        'category.parent-inside': t('errors.categoryParentInside'),
    };
}
