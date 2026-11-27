import {api, apiError, NotFoundError} from '@/shared/api';
import type {ConversationEntry, ConversationUpdate} from '../model/chat';
import type {PageContext} from '../model/page-context';

/** The editor's conversations, the latest first. */
export async function listConversations(): Promise<readonly ConversationEntry[]> {
    const {data, error, response} = await api.GET('/api/assistant/conversations');
    if (error !== undefined) {
        throw apiError(response, error);
    }
    return data;
}

export async function createConversation(): Promise<string> {
    const {data, error, response} = await api.POST('/api/assistant/conversations');
    if (error !== undefined) {
        throw apiError(response, error);
    }
    return data.id;
}

/** What is new in the conversation after the message; null when the conversation is gone. */
export async function readConversation(id: string, afterSeq: number): Promise<ConversationUpdate | null> {
    return orGone(async () => {
        const {data, error, response} = await api.GET('/api/assistant/conversations/{id}', {params: {path: {id}, query: {afterSeq}}});
        if (error !== undefined) {
            throw apiError(response, error);
        }
        return data;
    });
}

/** Sends the message from the page; false when the conversation is gone. */
export async function sendMessage(id: string, text: string, page: PageContext): Promise<boolean> {
    return gone(() => api.POST('/api/assistant/conversations/{id}/messages', {params: {path: {id}}, body: {text, page}}));
}

export async function stopRun(id: string): Promise<boolean> {
    return gone(() => api.POST('/api/assistant/conversations/{id}/stop', {params: {path: {id}}}));
}

export async function approvePlan(id: string): Promise<boolean> {
    return gone(() => api.POST('/api/assistant/plans/{id}/approve', {params: {path: {id}}}));
}

export async function cancelPlan(id: string): Promise<boolean> {
    return gone(() => api.POST('/api/assistant/plans/{id}/cancel', {params: {path: {id}}}));
}

/** Deletes the conversation; one already gone is as good as deleted. */
export async function deleteConversation(id: string): Promise<void> {
    await gone(() => api.DELETE('/api/assistant/conversations/{id}', {params: {path: {id}}}));
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

/** A call answered with no content: true once done, false when what it concerns is gone. */
async function gone(call: () => Promise<{readonly error?: unknown; readonly response: Response}>): Promise<boolean> {
    return await orGone(async () => {
        const {error, response} = await call();
        if (error !== undefined) {
            throw apiError(response, error);
        }
        return true;
    }) ?? false;
}

