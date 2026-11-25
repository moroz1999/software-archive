package com.archive.backend.assistant.app.chat;

import com.archive.backend.assistant.domain.chat.ConversationId;
import com.archive.backend.assistant.domain.chat.PlanAction;
import com.archive.backend.assistant.domain.chat.PlanTitle;
import java.util.List;

public record ProposePlanCommand(ConversationId conversationId, PlanTitle title, List<PlanAction> actions) {}
