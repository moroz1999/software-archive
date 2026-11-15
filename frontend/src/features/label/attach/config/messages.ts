import type {TFunction} from 'i18next';
import type {BusinessMessages} from '@/shared/api';

/** Texts for the business error codes attaching a label can receive; others fall back by status. */
export function attachLabelMessages(t: TFunction): BusinessMessages {
    return {
        '401': t('errors.business.signInRequired'),
        '403': t('errors.business.forbidden'),
        'label.owned': t('label.errors.owned'),
        'label.owner-unknown': t('label.errors.ownerUnknown'),
        'label.display-one': t('label.errors.displayOne'),
    };
}
