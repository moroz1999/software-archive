package com.archive.backend.assistant.app.chat;

import com.archive.backend.assistant.domain.chat.Plan;
import com.archive.backend.assistant.domain.chat.PlanRepository;
import com.archive.backend.assistant.domain.chat.PlanStep;
import java.time.Clock;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Stores how a change of a plan being made went. */
@Service
@RequiredArgsConstructor
public class RecordPlanStepHandler {

    private final PlanRepository plans;
    private final Clock clock;

    @Transactional
    public void handle(RecordPlanStepCommand c) {
        Plan plan = plans.findById(c.planId())
                .orElseThrow(
                        () -> new IllegalStateException("No plan " + c.planId().value()));
        Instant now = clock.instant();
        switch (c.step()) {
            case PlanStep.Started s -> plan.start(s.position());
            case PlanStep.Done d -> plan.done(d.position(), d.result(), now);
            case PlanStep.Failed f -> plan.fail(f.position(), f.error(), now);
            case PlanStep.Stopped s -> plan.stop(now);
        }
        plans.save(plan);
    }
}
