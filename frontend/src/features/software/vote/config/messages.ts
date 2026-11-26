import type {TFunction} from 'i18next';
import type {BusinessMessages} from '@/shared/api';

/** Texts for the business error codes a vote can receive; others fall back by status. */
export function voteMessages(t: TFunction): BusinessMessages {
    return {
        '401': t('rating.signInToVote'),
    };
}
