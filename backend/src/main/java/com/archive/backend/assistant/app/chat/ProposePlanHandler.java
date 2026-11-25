package com.archive.backend.assistant.app.chat;

import com.archive.backend.assistant.domain.chat.Plan;
import com.archive.backend.assistant.domain.chat.PlanId;
import com.archive.backend.assistant.domain.chat.PlanRepository;
import java.time.Clock;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Stores the plan a run proposes, to wait for the editor. */
@Service
@RequiredArgsConstructor
public class ProposePlanHandler {

    private final PlanRepository plans;
    private final Clock clock;

    @Transactional
    public PlanId handle(ProposePlanCommand c) {
        PlanId id = new PlanId(UUID.randomUUID());
        plans.save(Plan.propose(id, c.conversationId(), c.title(), c.actions(), clock.instant()));
        return id;
    }
}
