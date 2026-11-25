package com.archive.backend.assistant.app.chat;

import com.archive.backend.assistant.domain.chat.ConversationId;
import com.archive.backend.assistant.domain.chat.MessageSeq;
import com.archive.backend.user.domain.Actor;

public record GetConversationStateQuery(Actor actor, ConversationId conversationId, MessageSeq after) {}
