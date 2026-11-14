import type {TFunction} from 'i18next';
import type {BusinessMessages} from '@/shared/api';

/** Texts for the business error codes starting an import can receive; others fall back by status. */
export function vektorImportMessages(t: TFunction): BusinessMessages {
    return {
        'import.running': t('imports.errors.running'),
        '401': t('errors.business.signInRequired'),
        '403': t('imports.errors.forbidden'),
    };
}
