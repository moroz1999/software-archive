import type {QueryClient} from '@tanstack/react-query';
import {type Conversation, NO_CONVERSATION, withUpdate} from '../model/chat';
import {listConversations, readConversation} from './assistant-api';

export const ASSISTANT_QUERY_KEY = ['assistant'] as const;

/** How often a conversation is read again while the assistant works on it. */
export const POLL_MS = 2000;

export function conversationsQuery() {
    return {
        queryKey: [...ASSISTANT_QUERY_KEY, 'conversations'] as const,
        queryFn: listConversations,
        staleTime: 0,
    };
}

function conversationKey(id: string) {
    return [...ASSISTANT_QUERY_KEY, 'conversation', id] as const;
}

/**
 * A conversation, read in parts: each read asks for the messages after those already held and lays them over them.
 * It is read again every 2 s while the assistant works on it. Null once the conversation is gone.
 */
export function conversationQuery(queryClient: QueryClient, id: string) {
    const queryKey = conversationKey(id);
    return {
        queryKey,
        queryFn: async (): Promise<Conversation | null> => {
            const held = queryClient.getQueryData<Conversation | null>(queryKey) ?? NO_CONVERSATION;
            const update = await readConversation(id, held.lastSeq);
            return update === null ? null : withUpdate(held, update);
        },
        staleTime: 0,
        refetchInterval: (query: {readonly state: {readonly data: Conversation | null | undefined}}) => (
            query.state.data?.activeRun ? POLL_MS : false
        ),
    };
}
