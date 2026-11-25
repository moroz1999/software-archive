package com.archive.backend.assistant.domain.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.shared.domain.error.InvalidValueException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PlanTest {

    private static final Instant NOW = Instant.parse("2026-11-25T10:00:00Z");

    @Test
    void aPlanIsProposedWithNoneOfItsChangesMade() {
        Plan plan = plan(2);

        assertThat(plan.getStatus()).isEqualTo(PlanStatus.PROPOSED);
        assertThat(plan.getActions()).extracting(PlanAction::status).containsOnly(PlanActionStatus.PENDING);
    }

    @Test
    void aPlanHoldsAtLeastOneChange() {
        assertThatThrownBy(() -> Plan.propose(
                        new PlanId(UUID.randomUUID()),
                        new ConversationId(UUID.randomUUID()),
                        new PlanTitle("Nothing"),
                        List.of(),
                        NOW))
                .isInstanceOf(InvalidValueException.class);
    }

    @Test
    void itIsMadeOnceApprovedAndDoneAfterItsLastChange() {
        Plan plan = plan(2);

        plan.approve();
        assertThat(plan.getStatus()).isEqualTo(PlanStatus.EXECUTING);
        plan.start(1);
        assertThat(plan.getActions().get(0).status()).isEqualTo(PlanActionStatus.RUNNING);
        plan.done(1, "id-1", NOW);
        plan.start(2);
        plan.done(2, "id-2", NOW.plusSeconds(5));

        assertThat(plan.getStatus()).isEqualTo(PlanStatus.DONE);
        assertThat(plan.getActions()).extracting(PlanAction::result).containsExactly("id-1", "id-2");
        assertThat(plan.getFinishedAt()).isEqualTo(NOW.plusSeconds(5));
    }

    @Test
    void onlyAPlanWaitingForTheEditorIsApproved() {
        Plan plan = plan(1);
        plan.approve();

        assertThatThrownBy(plan::approve).isInstanceOf(PlanNotProposedException.class);
        Plan cancelled = plan(1);
        cancelled.cancel(NOW);
        assertThatThrownBy(cancelled::approve).isInstanceOf(PlanNotProposedException.class);
    }

    @Test
    void theFirstFailedChangeSkipsTheRest() {
        Plan plan = plan(3);
        plan.approve();
        plan.start(1);
        plan.done(1, "id-1", NOW);
        plan.start(2);

        plan.fail(2, "No such software.", NOW);

        assertThat(plan.getStatus()).isEqualTo(PlanStatus.FAILED);
        assertThat(plan.getActions())
                .extracting(PlanAction::status)
                .containsExactly(PlanActionStatus.DONE, PlanActionStatus.FAILED, PlanActionStatus.SKIPPED);
        assertThat(plan.getActions().get(1).error()).isEqualTo("No such software.");
    }

    @Test
    void stoppingItSkipsTheChangesNotMadeYet() {
        Plan plan = plan(3);
        plan.approve();
        plan.start(1);
        plan.done(1, "id-1", NOW);

        plan.stop(NOW);

        assertThat(plan.getStatus()).isEqualTo(PlanStatus.STOPPED);
        assertThat(plan.getActions())
                .extracting(PlanAction::status)
                .containsExactly(PlanActionStatus.DONE, PlanActionStatus.SKIPPED, PlanActionStatus.SKIPPED);
    }

    @Test
    void aChangeInterruptedByARestartFailsAndIsNotMadeAgain() {
        Plan plan = plan(3);
        plan.approve();
        plan.start(1);
        plan.done(1, "id-1", NOW);
        plan.start(2);

        plan.interrupt(NOW);

        assertThat(plan.getStatus()).isEqualTo(PlanStatus.FAILED);
        assertThat(plan.getActions())
                .extracting(PlanAction::status)
                .containsExactly(PlanActionStatus.DONE, PlanActionStatus.FAILED, PlanActionStatus.SKIPPED);
        assertThat(plan.getActions().get(1).error()).contains("interrupted");
    }

    @Test
    void anotherMessageDropsOnlyAPlanStillWaiting() {
        Plan waiting = plan(1);
        waiting.supersede(NOW);
        assertThat(waiting.getStatus()).isEqualTo(PlanStatus.SUPERSEDED);

        Plan executing = plan(1);
        executing.approve();
        executing.supersede(NOW);
        assertThat(executing.getStatus()).isEqualTo(PlanStatus.EXECUTING);
    }

    @Test
    void onlyAPlanStillWaitingIsCancelled() {
        Plan plan = plan(1);
        plan.cancel(NOW);
        assertThat(plan.getStatus()).isEqualTo(PlanStatus.CANCELLED);

        Plan executing = plan(1);
        executing.approve();
        assertThatThrownBy(() -> executing.cancel(NOW)).isInstanceOf(PlanNotProposedException.class);
    }

    private static Plan plan(int changes) {
        List<PlanAction> actions = java.util.stream.IntStream.rangeClosed(1, changes)
                .mapToObj(i -> PlanAction.proposed(
                        "create_software", "{\"title\": \"Elite " + i + "\"}", List.of(), "create", "software", null))
                .toList();
        return Plan.propose(
                new PlanId(UUID.randomUUID()),
                new ConversationId(UUID.randomUUID()),
                new PlanTitle("Add Elite"),
                actions,
                NOW);
    }
}
