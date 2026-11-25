package com.archive.backend.assistant.app.chat;

import com.archive.backend.assistant.domain.chat.ConversationMessage;
import com.archive.backend.assistant.domain.chat.ConversationMessageRepository;
import com.archive.backend.assistant.domain.chat.ConversationRepository;
import com.archive.backend.assistant.domain.chat.MessageBody;
import com.archive.backend.assistant.domain.chat.MessageKind;
import com.archive.backend.assistant.domain.chat.MessageRole;
import com.archive.backend.assistant.domain.chat.Plan;
import com.archive.backend.assistant.domain.chat.PlanRepository;
import java.time.Clock;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** The editor drops the plan waiting for them; the conversation tells so. */
@Service
@RequiredArgsConstructor
public class CancelPlanHandler {

    private final ConversationRepository conversations;
    private final ConversationMessageRepository messages;
    private final PlanRepository plans;
    private final Clock clock;

    @Transactional
    public void handle(CancelPlanCommand c) {
        ChatAccess.requireUser(c.actor());
        Plan plan = ChatAccess.ownedPlan(plans, conversations, c.planId(), c.actor());
        Instant now = clock.instant();
        plan.cancel(now);
        plans.save(plan);
        messages.save(new ConversationMessage(
                plan.getConversationId(),
                MessageRole.SYSTEM,
                MessageKind.PLAN_PROGRESS,
                new MessageBody(
                        "The editor cancelled the plan \"" + plan.getTitle().value() + "\": nothing of it was made.",
                        "{\"planId\": \"" + plan.getId().value() + "\", \"status\": \"cancelled\"}"),
                now));
    }
}
