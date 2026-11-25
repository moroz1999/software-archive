package com.archive.backend.assistant.read.chat;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * A conversation as its owner sees it: the messages after the seq asked for, every plan, the run working on it if
 * any, and the entries the messages and the plans mention, by their ids.
 */
public record ConversationState(
        UUID id,
        @Nullable String title,
        List<ChatMessage> messages,
        List<ChatPlan> plans,
        @Nullable ChatRun activeRun,
        Map<String, EntityRef> refs) {}
