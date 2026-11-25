import {type ReactNode, useState} from 'react';
import {useMutation, useQuery, useQueryClient} from '@tanstack/react-query';
import {useTranslation} from 'react-i18next';
import {businessErrorMessage, businessErrorOf} from '@/shared/api';
import {approvePlan, cancelPlan, createConversation, deleteConversation, sendMessage, stopRun} from '../api/assistant-api';
import {ASSISTANT_QUERY_KEY, conversationQuery, conversationsQuery} from '../api/assistant-queries';
import {assistantMessages} from '../config/messages';
import {AssistantChatContext, type AssistantChatState} from './assistant-chat-context';
import {type ConversationId, type PlanId, toConversationId} from './chat';
import {pageContextOf} from './page-context';

interface Sending {
    readonly text: string;
    readonly path: string;
    readonly title: string;
}

/**
 * The chat with the assistant for its panel: the open conversation read while the assistant works on it, the
 * message being written, and what the editor asks of the conversation and its plans. It outlives the panel being
 * closed, so the conversation is there when it opens again.
 */
export function AssistantChatProvider({children}: {readonly children: ReactNode}) {
    const {t} = useTranslation();
    const queryClient = useQueryClient();
    const [chosenId, setConversationId] = useState<ConversationId | null>(null);
    const [draft, setDraft] = useState('');
    const [listShown, setListShown] = useState(false);

    const conversation = useQuery({
        ...conversationQuery(queryClient, chosenId ?? PLACEHOLDER),
        enabled: chosenId !== null,
    });
    // A conversation deleted in another window is gone: the panel is on a new one.
    const conversationId = conversation.data === null ? null : chosenId;
    const conversations = useQuery({...conversationsQuery(), enabled: listShown});

    const reread = async () => {
        await queryClient.invalidateQueries({queryKey: ASSISTANT_QUERY_KEY});
    };

    const send = useMutation({
        mutationFn: async ({text, path, title}: Sending) => {
            const id = conversationId ?? await createConversation();
            try {
                return await sendMessage(id, text, pageContextOf(path, title));
            } finally {
                setConversationId(id);
            }
        },
        onSuccess: async (sent) => {
            if (sent) {
                setDraft('');
            }
            await reread();
        },
    });
    const stop = useMutation({mutationFn: stopRun, onSettled: reread});
    const approve = useMutation({mutationFn: approvePlan, onSettled: reread});
    const cancel = useMutation({mutationFn: cancelPlan, onSettled: reread});
    const remove = useMutation({
        mutationFn: deleteConversation,
        onSuccess: async (_, id) => {
            if (id === conversationId) {
                setConversationId(null);
            }
            await reread();
        },
    });
    const mutations = [send, stop, approve, cancel, remove];

    const startNew = () => {
        setConversationId(null);
        setListShown(false);
        mutations.forEach((mutation) => {
            mutation.reset();
        });
    };

    const businessError = mutations.map((mutation) => businessErrorOf(mutation.error)).find((error) => error !== undefined);

    const state: AssistantChatState = {
        conversationId,
        conversation: conversationId === null ? null : conversation.data ?? null,
        draft,
        setDraft,
        send: (path, title) => {
            if (draft.trim() !== '') {
                send.mutate({text: draft, path, title});
            }
        },
        sending: send.isPending,
        stop: () => {
            if (conversationId !== null) {
                stop.mutate(conversationId);
            }
        },
        approve: (plan: PlanId) => {
            approve.mutate(plan);
        },
        cancel: (plan: PlanId) => {
            cancel.mutate(plan);
        },
        planBusy: approve.isPending || cancel.isPending,
        listShown,
        toggleList: () => {
            setListShown((shown) => !shown);
        },
        conversations: conversations.data ?? null,
        openConversation: (id) => {
            startNew();
            setConversationId(id);
        },
        startNew,
        remove: (id) => {
            remove.mutate(id);
        },
        removing: remove.isPending,
        error: businessError === undefined ? undefined : businessErrorMessage(businessError, assistantMessages(t)),
    };

    return <AssistantChatContext value={state}>{children}</AssistantChatContext>;
}

/** Stands for no conversation in the query that is off until one is open. */
const PLACEHOLDER = toConversationId('00000000-0000-0000-0000-000000000000');
