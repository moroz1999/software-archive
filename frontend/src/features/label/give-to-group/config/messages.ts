import type {TFunction} from 'i18next';
import type {BusinessMessages} from '@/shared/api';

/** Texts for the business error codes giving a label to a group can receive; others fall back by status. */
export function giveLabelMessages(t: TFunction): BusinessMessages {
    return {
        '401': t('errors.business.signInRequired'),
        '403': t('label.toGroupErrors.forbidden'),
        'label.owner-unknown': t('label.toGroupErrors.groupUnknown'),
        'label.of-person': t('label.toGroupErrors.ofPerson'),
    };
}
