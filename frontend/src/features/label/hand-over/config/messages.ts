import type {TFunction} from 'i18next';
import type {BusinessMessages} from '@/shared/api';

/** Texts for the business error codes giving a label to a group can receive; others fall back by status. */
export function giveToGroupMessages(t: TFunction): BusinessMessages {
    return {
        '401': t('errors.business.signInRequired'),
        '403': t('label.toGroupErrors.forbidden'),
        'label.owner-unknown': t('label.toGroupErrors.groupUnknown'),
        'label.of-person': t('label.toGroupErrors.ofPerson'),
    };
}

/** Texts for the business error codes merging a label into a person can receive; others fall back by status. */
export function mergeIntoAuthorMessages(t: TFunction): BusinessMessages {
    return {
        '401': t('errors.business.signInRequired'),
        '403': t('label.mergeErrors.forbidden'),
        'label.owner-unknown': t('label.mergeErrors.authorUnknown'),
        'label.of-person': t('label.mergeErrors.ofPerson'),
    };
}

/** Texts for the business error codes merging a label into another label can receive; others fall back by status. */
export function mergeIntoLabelMessages(t: TFunction): BusinessMessages {
    return {
        '401': t('errors.business.signInRequired'),
        '403': t('label.mergeErrors.forbidden'),
        'label.owner-unknown': t('label.mergeErrors.labelUnknown'),
        'label.merge-into-itself': t('label.mergeErrors.itself'),
        'label.of-person': t('label.mergeErrors.ofPerson'),
    };
}
