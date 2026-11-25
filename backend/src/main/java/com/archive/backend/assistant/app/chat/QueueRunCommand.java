package com.archive.backend.assistant.app.chat;

import com.archive.backend.assistant.domain.chat.ConversationId;

public record QueueRunCommand(ConversationId conversationId) {}
