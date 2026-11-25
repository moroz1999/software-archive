import type {TFunction} from 'i18next';
import type {BusinessMessages} from '@/shared/api';

/** Texts for the business error codes the chat can receive; others fall back by status. */
export function assistantMessages(t: TFunction): BusinessMessages {
    return {
        'assistant.busy': t('assistant.errors.busy'),
        'assistant.plan-not-proposed': t('assistant.errors.planNotProposed'),
        'assistant.conversation-not-found': t('assistant.errors.conversationGone'),
        'assistant.plan-not-found': t('assistant.errors.conversationGone'),
        'assistant.message-empty': t('assistant.errors.messageEmpty'),
        'assistant.message-too-long': t('assistant.errors.messageTooLong'),
        '401': t('errors.business.signInRequired'),
        '403': t('assistant.errors.forbidden'),
        '409': t('errors.business.conflict'),
        '422': t('errors.business.refused'),
    };
}
