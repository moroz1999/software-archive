import type {TFunction} from 'i18next';
import type {BusinessMessages} from '@/shared/api';

/** Texts for the business error codes saving a release can receive; others fall back by status. */
export function editReleaseMessages(t: TFunction): BusinessMessages {
    return {
        '401': t('errors.business.signInRequired'),
        '403': t('errors.business.forbidden'),
        '409': t('errors.business.conflict'),
        '422': t('errors.business.refused'),
        'hardware.unknown': t('errors.hardwareUnknown'),
        'software.credit-unknown': t('errors.softwareCreditUnknown'),
        'release.archive-too-large': t('release.archiveErrors.size'),
        'release.archive-name-invalid': t('release.archiveErrors.name'),
        'release.archive-change-invalid': t('release.archiveErrors.change'),
    };
}
