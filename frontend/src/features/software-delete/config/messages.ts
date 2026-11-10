import type {TFunction} from 'i18next';
import type {BusinessMessages} from '@/shared/api';

/** Texts for the business error codes deleting software can receive; others fall back by status. */
export function deleteSoftwareMessages(t: TFunction): BusinessMessages {
    return {'409': t('errors.business.conflict'), '422': t('errors.business.refused')};
}
