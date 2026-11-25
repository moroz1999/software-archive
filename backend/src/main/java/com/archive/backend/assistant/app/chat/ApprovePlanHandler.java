package com.archive.backend.assistant.app.chat;

import com.archive.backend.assistant.domain.chat.ConversationRepository;
import com.archive.backend.assistant.domain.chat.Plan;
import com.archive.backend.assistant.domain.chat.PlanRepository;
import com.archive.backend.assistant.domain.chat.Run;
import com.archive.backend.assistant.domain.chat.RunId;
import com.archive.backend.assistant.domain.chat.RunRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** The editor approves the plan waiting for them: a run making its changes is queued. */
@Service
@RequiredArgsConstructor
public class ApprovePlanHandler {

    private final ConversationRepository conversations;
    private final PlanRepository plans;
    private final RunRepository runs;
    private final Clock clock;

    @Transactional
    public RunId handle(ApprovePlanCommand c) {
        ChatAccess.requireUser(c.actor());
        Plan plan = ChatAccess.ownedPlan(plans, conversations, c.planId(), c.actor());
        Instant now = clock.instant();
        ChatAccess.requireFree(runs, plan.getConversationId(), now);
        plan.approve();
        plans.save(plan);
        RunId run = new RunId(UUID.randomUUID());
        runs.save(Run.execute(run, plan.getConversationId(), plan.getId(), now));
        return run;
    }
}
