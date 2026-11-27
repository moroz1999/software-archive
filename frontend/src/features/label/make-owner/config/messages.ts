import type {TFunction} from 'i18next';
import type {BusinessMessages} from '@/shared/api';

/** Texts for the business error codes making an owner of a label can receive; others fall back by status. */
export function makeOwnerMessages(t: TFunction): BusinessMessages {
    return {
        '401': t('errors.business.signInRequired'),
        '403': t('errors.business.forbidden'),
        'label.owned': t('label.errors.owned'),
        'author.nameless': t('label.makeErrors.onlyName'),
        'group.type-unknown': t('label.makeErrors.typeUnknown'),
    };
}
