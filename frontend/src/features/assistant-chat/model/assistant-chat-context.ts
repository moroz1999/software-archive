import {createRequiredContext} from '@/shared/lib/context';
import type {Conversation, ConversationEntry, ConversationId, PlanId} from './chat';

export interface AssistantChatState {
    /** The open conversation; null for a new one, made by its first message. */
    readonly conversationId: ConversationId | null;
    /** What is read of the open conversation; null while it is read the first time. */
    readonly conversation: Conversation | null;
    /** The message being written. */
    readonly draft: string;
    readonly setDraft: (text: string) => void;
    /** Sends the draft from the page at the path; it is cleared once sent. */
    readonly send: (path: string, title: string) => void;
    readonly sending: boolean;
    readonly stop: () => void;
    readonly approve: (plan: PlanId) => void;
    readonly cancel: (plan: PlanId) => void;
    /** A change of a plan is being asked for. */
    readonly planBusy: boolean;
    /** The list of conversations, shown in place of the open one. */
    readonly listShown: boolean;
    readonly toggleList: () => void;
    /** The editor's conversations; null while they are read. */
    readonly conversations: readonly ConversationEntry[] | null;
    readonly openConversation: (id: ConversationId) => void;
    readonly startNew: () => void;
    readonly remove: (id: ConversationId) => void;
    readonly removing: boolean;
    /** A business error of the last thing asked, to be shown in the panel. */
    readonly error: string | undefined;
}

/** The chat of the panel of the AI assistant, inside the AssistantChatProvider. */
export const [AssistantChatContext, useAssistantChat] = createRequiredContext<AssistantChatState>('AssistantChat');
