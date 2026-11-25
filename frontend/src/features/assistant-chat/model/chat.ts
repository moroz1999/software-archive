import type {JsonObject} from '@/shared/api';
import type {Brand} from '@/shared/lib/brand';
import {isUuid} from '@/shared/lib/uuid';

/** A conversation of an editor with the assistant (docs/domain/ai-assistant.md). */
export type ConversationId = Brand<string, 'ConversationId'>;

/** A plan the assistant proposed. */
export type PlanId = Brand<string, 'PlanId'>;

export function toConversationId(raw: string): ConversationId {
    if (!isUuid(raw)) {
        throw new TypeError(`Invalid conversation id: ${raw}`);
    }
    return raw as ConversationId;
}

export function toPlanId(raw: string): PlanId {
    if (!isUuid(raw)) {
        throw new TypeError(`Invalid plan id: ${raw}`);
    }
    return raw as PlanId;
}

function oneOf<T extends string>(raw: string, known: readonly T[], what: string): T {
    const value = known.find((code) => code === raw);
    if (value === undefined) {
        throw new TypeError(`Unknown ${what}: ${raw}`);
    }
    return value;
}

/** A conversation in the list of the editor's own, the latest first. */
export interface ConversationEntry {
    readonly id: ConversationId;
    /** Null until its first message. */
    readonly title: string | null;
    readonly running: boolean;
}

export type MessageRole = 'user' | 'assistant' | 'system';
export type MessageKind = 'text' | 'toolCall' | 'plan' | 'planProgress' | 'error' | 'stopped';

export interface ChatMessage {
    readonly seq: number;
    readonly role: MessageRole;
    readonly kind: MessageKind;
    readonly text: string;
    /** What the message holds besides its text: a call and its result, the plan it proposes. */
    readonly payload: JsonObject | null;
}

export function toMessageRole(raw: string): MessageRole {
    return oneOf(raw, ['user', 'assistant', 'system'], 'message role');
}

export function toMessageKind(raw: string): MessageKind {
    return oneOf(raw, ['text', 'toolCall', 'plan', 'planProgress', 'error', 'stopped'], 'message kind');
}

export type PlanStatus = 'proposed' | 'executing' | 'done' | 'failed' | 'stopped' | 'superseded' | 'cancelled';
export type PlanActionStatus = 'pending' | 'running' | 'done' | 'failed' | 'skipped';

export function toPlanStatus(raw: string): PlanStatus {
    return oneOf(raw, ['proposed', 'executing', 'done', 'failed', 'stopped', 'superseded', 'cancelled'], 'plan status');
}

export function toPlanActionStatus(raw: string): PlanActionStatus {
    return oneOf(raw, ['pending', 'running', 'done', 'failed', 'skipped'], 'plan action status');
}

/** A change of a plan: a write tool with its arguments, and the entry as it was when the plan was proposed. */
export interface PlanAction {
    readonly position: number;
    readonly tool: string;
    readonly arguments: JsonObject;
    /** The kind of change: create, update, delete, merge, … */
    readonly kind: string;
    readonly entityType: string;
    /** The entry the change changes, in the names of its arguments; null for one it adds. */
    readonly state: JsonObject | null;
    readonly status: PlanActionStatus;
    /** The id the change produced, once made. */
    readonly result: string | null;
    readonly error: string | null;
}

export interface ChatPlan {
    readonly id: PlanId;
    readonly title: string;
    readonly status: PlanStatus;
    readonly actions: readonly PlanAction[];
}

/** The task the assistant works on now. */
export interface ActiveRun {
    readonly stopRequested: boolean;
}

/** An entry of the archive a message or a plan mentions, as a link. */
export interface EntityRef {
    readonly type: string;
    readonly title: string;
    readonly path: string;
}

/** What is new in a conversation since a message, with every plan, the task at work and the entries mentioned. */
export interface ConversationUpdate {
    readonly messages: readonly ChatMessage[];
    readonly plans: readonly ChatPlan[];
    readonly activeRun: ActiveRun | null;
    readonly refs: Readonly<Record<string, EntityRef>>;
}

/** A conversation as the panel holds it: every message read so far. */
export interface Conversation extends ConversationUpdate {
    /** The last message read; the next read asks for those after it. */
    readonly lastSeq: number;
}

export const NO_CONVERSATION: Conversation = {messages: [], plans: [], activeRun: null, refs: {}, lastSeq: 0};

/** The conversation with what is new laid over it: messages appended, plans and the task replaced, links added. */
export function withUpdate(conversation: Conversation, update: ConversationUpdate): Conversation {
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
