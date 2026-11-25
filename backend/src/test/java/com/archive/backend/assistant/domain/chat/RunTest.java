package com.archive.backend.assistant.domain.chat;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RunTest {

    private static final Instant NOW = Instant.parse("2026-11-25T10:00:00Z");
    private static final ConversationId CONVERSATION = new ConversationId(UUID.randomUUID());

    @Test
    void aRunIsQueuedUntilItIsClaimedAndOnlyOnce() {
        Run run = Run.chat(new RunId(UUID.randomUUID()), CONVERSATION, NOW);

        assertThat(run.getStatus()).isEqualTo(RunStatus.QUEUED);
        assertThat(run.getType()).isEqualTo(RunType.CHAT);
        assertThat(run.active()).isTrue();
        assertThat(run.claim()).isTrue();
        assertThat(run.getStatus()).isEqualTo(RunStatus.RUNNING);
        assertThat(run.claim()).isFalse();
    }

    @Test
    void aRunMakingAPlanKnowsThePlan() {
        PlanId plan = new PlanId(UUID.randomUUID());

        Run run = Run.execute(new RunId(UUID.randomUUID()), CONVERSATION, plan, NOW);

        assertThat(run.getType()).isEqualTo(RunType.EXECUTE);
        assertThat(run.getPlanId()).isEqualTo(plan);
    }

    @Test
    void countsItsStepsAndTheirTokens() {
        Run run = running();

        run.step(100, 20);
        run.step(150, 30);

        assertThat(run.getUsage()).isEqualTo(new RunUsage(2, 250, 50));
    }

    @Test
    void remembersThatTheEditorAskedItToStop() {
        Run run = running();
        assertThat(run.stopRequested()).isFalse();

        run.requestStop();

        assertThat(run.stopRequested()).isTrue();
        assertThat(run.active()).isTrue();
    }

    @Test
    void endsWithWhyItFailed() {
        Run run = running();

        run.end(RunStatus.FAILED, new RunError("The model failed"), NOW.plusSeconds(30));

        assertThat(run.active()).isFalse();
        assertThat(run.getStatus()).isEqualTo(RunStatus.FAILED);
        assertThat(run.getError()).isEqualTo(new RunError("The model failed"));
        assertThat(run.getFinishedAt()).isEqualTo(NOW.plusSeconds(30));
    }

    @Test
    void isTakenForDeadAQuarterOfAnHourAfterItWasQueued() {
        Run run = running();

        assertThat(run.deadAt(NOW.plus(Duration.ofMinutes(14)))).isFalse();
        assertThat(run.deadAt(NOW.plus(Duration.ofMinutes(15)).plusSeconds(1))).isTrue();

        run.end(RunStatus.DONE, null, NOW.plusSeconds(10));
        assertThat(run.deadAt(NOW.plus(Duration.ofHours(1)))).isFalse();
    }

    private static Run running() {
        Run run = Run.chat(new RunId(UUID.randomUUID()), CONVERSATION, NOW);
        run.claim();
        return run;
    }
}
