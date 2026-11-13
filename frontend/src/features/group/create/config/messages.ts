import type {TFunction} from 'i18next';
import type {BusinessMessages} from '@/shared/api';

/** Texts for the business error codes adding a group can receive; others fall back by status. */
export function createGroupMessages(t: TFunction): BusinessMessages {
    return {
        '401': t('errors.business.signInRequired'),
        '403': t('errors.business.forbidden'),
        '409': t('errors.business.conflict'),
        '422': t('errors.business.refused'),
        'group.types-required': t('validation.groupTypesRequired'),
        'group.type-unknown': t('errors.groupTypeUnknown'),
        'group.member-unknown': t('errors.groupMemberUnknown'),
        'label.not-own': t('errors.labelNotOwn'),
        'label.display-one': t('errors.labelDisplayOne'),
        'location.unknown': t('errors.locationUnknown'),
        'location.primary-one': t('errors.locationPrimaryOne'),
    };
}
