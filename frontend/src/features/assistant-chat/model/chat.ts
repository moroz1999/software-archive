import type {components} from '@/shared/api';

type Schemas = components['schemas'];

/** Free JSON the assistant's calls carry: the arguments of a tool, what a message holds, the entry a change concerns. */
export type JsonObject = Readonly<Record<string, unknown>>;

/** A conversation in the list of the editor's own, the latest first. */
export type ConversationEntry = Schemas['ConversationListEntryDto'];
export type MessageRole = Schemas['MessageRole'];
export type MessageKind = Schemas['MessageKind'];
export type ChatMessage = Schemas['ChatMessageDto'];
export type PlanStatus = Schemas['PlanStatus'];
export type PlanActionStatus = Schemas['PlanActionStatus'];
/** A change of a plan: a write tool with its arguments, and the entry as it was when the plan was proposed. */
export type PlanAction = Schemas['ChatPlanActionDto'];
export type ChatPlan = Schemas['ChatPlanDto'];
/** The task the assistant works on now. */
export type ActiveRun = Schemas['ChatRunDto'];
/** An entry of the archive a message or a plan mentions, as a link. */
export type EntityRef = Schemas['EntityRefDto'];
/** What is new in a conversation since a message, with every plan, the task at work and the entries mentioned. */
export type ConversationUpdate = Schemas['ConversationStateDto'];

/** What a conversation is made of, read at once or piece by piece. */
type ConversationParts = Pick<ConversationUpdate, 'messages' | 'plans' | 'activeRun' | 'refs'>;

/** A conversation as the panel holds it: every message read so far. */
export interface Conversation extends ConversationParts {
    /** The last message read; the next read asks for those after it. */
    readonly lastSeq: number;
}

export const NO_CONVERSATION: Conversation = {messages: [], plans: [], activeRun: null, refs: {}, lastSeq: 0};

/** The conversation with what is new laid over it: messages appended, plans and the task replaced, links added. */
export function withUpdate(conversation: Conversation, update: ConversationParts): Conversation {
    const known = new Set(conversation.messages.map((message) => message.seq));
    const messages = [...conversation.messages, ...update.messages.filter((message) => !known.has(message.seq))];
    return {
        messages,
        plans: update.plans,
        activeRun: update.activeRun,
        refs: {...conversation.refs, ...update.refs},
        lastSeq: Math.max(conversation.lastSeq, ...messages.map((message) => message.seq)),
    };
}
