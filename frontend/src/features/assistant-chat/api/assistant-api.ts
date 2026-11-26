import {
    deleteResource, getJson, type JsonObject, mapResponse, NotFoundError, postJson, postNoContent, readArray, readBoolean,
    readNullableString, readNumber, readObject, readString,
} from '@/shared/api';
import {
    type ActiveRun, type ChatMessage, type ChatPlan, type ConversationEntry, type ConversationId, type ConversationUpdate, type EntityRef,
    type PlanAction, type PlanId, toConversationId, toMessageKind, toMessageRole, toPlanActionStatus, toPlanId, toPlanStatus,
} from '../model/chat';
import type {PageContext} from '../model/page-context';

const BASE = '/api/assistant';


function nullableObject(value: unknown, what: string): JsonObject | null {
    return value === null || value === undefined ? null : readObject(value, what);
}

function readMessage(raw: unknown): ChatMessage {
    const dto = readObject(raw, 'message');
    return {
        seq: readNumber(dto, 'seq'),
        role: toMessageRole(readString(dto, 'role')),
        kind: toMessageKind(readString(dto, 'kind')),
        text: readString(dto, 'text'),
        payload: nullableObject(dto['payload'], 'payload'),
    };
}

function readAction(raw: unknown): PlanAction {
    const dto = readObject(raw, 'plan action');
    const state = dto['state'];
    return {
        position: readNumber(dto, 'position'),
        tool: readString(dto, 'tool'),
        arguments: readObject(dto['arguments'], 'arguments'),
        kind: readString(dto, 'kind'),
        entityType: readString(dto, 'entityType'),
        state: Array.isArray(state) ? state : nullableObject(state, 'state'),
        status: toPlanActionStatus(readString(dto, 'status')),
        result: readNullableString(dto, 'result'),
        error: readNullableString(dto, 'error'),
    };
}

function readPlan(raw: unknown): ChatPlan {
    const dto = readObject(raw, 'plan');
    return {
        id: toPlanId(readString(dto, 'id')),
        title: readString(dto, 'title'),
        status: toPlanStatus(readString(dto, 'status')),
        actions: readArray(dto['actions'], 'actions').map(readAction),
    };
}

function readRun(raw: unknown): ActiveRun | null {
    return raw === null || raw === undefined ? null : {stopRequested: readBoolean(readObject(raw, 'run'), 'stopRequested')};
}

function readRefs(raw: unknown): Readonly<Record<string, EntityRef>> {
    const dto = readObject(raw, 'refs');
    return Object.fromEntries(Object.entries(dto).map(([id, value]) => {
        const ref = readObject(value, 'ref');
        return [id, {type: readString(ref, 'type'), title: readString(ref, 'title'), path: readString(ref, 'path')}];
    }));
}

/** The update of a conversation in the body of `GET /api/assistant/conversations/{id}`. */
export function readConversationUpdate(raw: unknown): ConversationUpdate {
    return mapResponse(() => {
        const dto = readObject(raw, 'conversation');
        return {
            messages: readArray(dto['messages'], 'messages').map(readMessage),
            plans: readArray(dto['plans'], 'plans').map(readPlan),
            activeRun: readRun(dto['activeRun']),
            refs: readRefs(dto['refs']),
        };
    });
}

/** The editor's conversations, the latest first. */
export async function listConversations(): Promise<readonly ConversationEntry[]> {
    const raw = await getJson(`${BASE}/conversations`);
    return mapResponse(() => readArray(raw, 'conversations').map((value) => {
        const dto = readObject(value, 'conversation');
        return {id: toConversationId(readString(dto, 'id')), title: readNullableString(dto, 'title'), running: readBoolean(dto, 'running')};
    }));
}

export async function createConversation(): Promise<ConversationId> {
    const raw = await postJson(`${BASE}/conversations`, {});
    return mapResponse(() => toConversationId(readString(readObject(raw, 'conversation'), 'id')));
}

/** What is new in the conversation after the message; null when the conversation is gone. */
export async function readConversation(id: ConversationId, afterSeq: number): Promise<ConversationUpdate | null> {
    return orGone(async () => readConversationUpdate(await getJson(`${BASE}/conversations/${id}?afterSeq=${String(afterSeq)}`)));
}

/** Sends the message from the page; false when the conversation is gone. */
export async function sendMessage(id: ConversationId, text: string, page: PageContext): Promise<boolean> {
    return gone(() => postNoContent(`${BASE}/conversations/${id}/messages`, {text, page}));
}

export async function stopRun(id: ConversationId): Promise<boolean> {
    return gone(() => postNoContent(`${BASE}/conversations/${id}/stop`));
}

export async function approvePlan(id: PlanId): Promise<boolean> {
    return gone(() => postNoContent(`${BASE}/plans/${id}/approve`));
}

export async function cancelPlan(id: PlanId): Promise<boolean> {
    return gone(() => postNoContent(`${BASE}/plans/${id}/cancel`));
}

/** Deletes the conversation; one already gone is as good as deleted. */
export async function deleteConversation(id: ConversationId): Promise<void> {
    await gone(() => deleteResource(`${BASE}/conversations/${id}`));
}

/**
 * A conversation or a plan that is gone (deleted in another window) is no page to leave for: the panel starts anew,
 * so its not-found is taken here and not thrown to the route.
 */
async function orGone<T>(call: () => Promise<T>): Promise<T | null> {
    try {
        return await call();
    } catch (error) {
        if (error instanceof NotFoundError) {
            return null;
        }
        throw error;
    }
}

async function gone(call: () => Promise<void>): Promise<boolean> {
    return await orGone(async () => {
        await call();
        return true;
    }) ?? false;
}

