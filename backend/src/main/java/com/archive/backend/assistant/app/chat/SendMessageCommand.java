package com.archive.backend.assistant.app.chat;

import com.archive.backend.assistant.domain.chat.ConversationId;
import com.archive.backend.assistant.domain.chat.MessageText;
import com.archive.backend.assistant.domain.chat.PageContext;
import com.archive.backend.user.domain.Actor;

public record SendMessageCommand(Actor actor, ConversationId conversationId, MessageText text, PageContext page) {}
