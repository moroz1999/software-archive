package com.archive.backend.assistant.app.chat;

import com.archive.backend.assistant.domain.chat.ConversationId;
import com.archive.backend.user.domain.Actor;

public record StopRunCommand(Actor actor, ConversationId conversationId) {}
