package com.archive.backend.assistant.app.chat;

import com.archive.backend.assistant.domain.chat.AssistantBusyException;
import com.archive.backend.assistant.domain.chat.Conversation;
import com.archive.backend.assistant.domain.chat.ConversationId;
import com.archive.backend.assistant.domain.chat.ConversationNotFoundException;
import com.archive.backend.assistant.domain.chat.ConversationRepository;
import com.archive.backend.assistant.domain.chat.Plan;
import com.archive.backend.assistant.domain.chat.PlanId;
import com.archive.backend.assistant.domain.chat.PlanNotFoundException;
import com.archive.backend.assistant.domain.chat.PlanRepository;
import com.archive.backend.assistant.domain.chat.Run;
import com.archive.backend.assistant.domain.chat.RunError;
import com.archive.backend.assistant.domain.chat.RunId;
import com.archive.backend.assistant.domain.chat.RunRepository;
import com.archive.backend.assistant.domain.chat.RunStatus;
import com.archive.backend.user.domain.Actor;
import java.time.Instant;

/** What every use case of the chat checks: who asks, whose the conversation is, whether the assistant is free. */
final class ChatAccess {

    private ChatAccess() {}

    static void requireUser(Actor actor) {
        actor.requireAllowed(actor.mayUseAssistant());
    }

    static Conversation owned(ConversationRepository conversations, ConversationId id, Actor actor) {
        Conversation conversation = conversations.findById(id).orElseThrow(ConversationNotFoundException::new);
        conversation.requireOwner(actor);
        return conversation;
    }

    /** A plan of a conversation of the actor; of anybody else's, none. */
    static Plan ownedPlan(PlanRepository plans, ConversationRepository conversations, PlanId id, Actor actor) {
        Plan plan = plans.findById(id).orElseThrow(PlanNotFoundException::new);
        Conversation conversation =
                conversations.findById(plan.getConversationId()).orElseThrow(PlanNotFoundException::new);
        if (!conversation.getOwner().equals(actor.id())) {
            throw new PlanNotFoundException();
        }
        return plan;
    }

    /** No run works on the conversation; one left over past its life is ended as dead. */
    static void requireFree(RunRepository runs, ConversationId id, Instant now) {
        runs.findActive(id).ifPresent(run -> {
            if (!run.deadAt(now)) {
                throw new AssistantBusyException();
            }
            run.end(RunStatus.FAILED, new RunError("The run was taken for dead: it ran past its time."), now);
            runs.save(run);
        });
    }

    static Run run(RunRepository runs, RunId id) {
        return runs.findById(id).orElseThrow(() -> new IllegalStateException("No run " + id.value()));
    }
}
