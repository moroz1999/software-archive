import type {TFunction} from 'i18next';
import type {BusinessMessages} from '@/shared/api';

/** Texts for the business error codes deleting a group or an item of hardware can receive; others fall back by status. */
export function deleteHardwareMessages(t: TFunction): BusinessMessages {
    return {
        '401': t('errors.business.signInRequired'),
        '403': t('errors.business.forbidden'),
        '409': t('errors.business.conflict'),
        '422': t('errors.business.refused'),
        'hardware.group-in-use': t('errors.hardwareGroupInUse'),
        'hardware.in-use': t('errors.hardwareInUse'),
    };
}
