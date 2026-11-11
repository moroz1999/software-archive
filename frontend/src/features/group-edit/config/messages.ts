import type {TFunction} from 'i18next';
import type {BusinessMessages} from '@/shared/api';

/** Texts for the business error codes saving a group can receive; others fall back by status. */
export function editGroupMessages(t: TFunction): BusinessMessages {
    return {
        '409': t('errors.business.conflict'),
        '422': t('errors.business.refused'),
        'group.types-required': t('validation.groupTypesRequired'),
        'group.type-unknown': t('errors.groupTypeUnknown'),
        'label.not-own': t('errors.labelNotOwn'),
        'label.display-one': t('errors.labelDisplayOne'),
        'location.unknown': t('errors.locationUnknown'),
        'location.primary-one': t('errors.locationPrimaryOne'),
    };
}
