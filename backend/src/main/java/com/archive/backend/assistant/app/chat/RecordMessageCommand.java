package com.archive.backend.assistant.app.chat;

import com.archive.backend.assistant.domain.chat.ConversationId;
import com.archive.backend.assistant.domain.chat.MessageBody;
import com.archive.backend.assistant.domain.chat.MessageKind;
import com.archive.backend.assistant.domain.chat.MessageRole;

public record RecordMessageCommand(
        ConversationId conversationId, MessageRole role, MessageKind kind, MessageBody body) {}
