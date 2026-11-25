package com.archive.backend.assistant.domain.chat;

import com.archive.backend.shared.domain.ddd.AggregateRoot;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

/**
 * One task of the assistant in a conversation: answering it, or making the changes of an approved plan. Queued, it is
 * claimed by whoever starts it, once; it ends answered, waiting for a plan to be approved, failed or stopped. One
 * that runs longer than a quarter of an hour is taken for dead.
 */
@Getter
@AggregateRoot
public final class Run {

    /** Longer than any run lives: its limit is 5 minutes. */
    public static final Duration DEAD_AFTER = Duration.ofMinutes(15);

    private final RunId id;
    private final ConversationId conversationId;
    private final RunType type;
    private final @Nullable PlanId planId;
    private final Instant queuedAt;
    private RunStatus status;
    private RunStop stop;
    private RunUsage usage;
    private @Nullable RunError error;
    private @Nullable Instant finishedAt;

    /** A run as it is stored. */
    public Run(
            RunId id,
            ConversationId conversationId,
            RunType type,
            @Nullable PlanId planId,
            Instant queuedAt,
            RunStatus status,
            RunStop stop,
            RunUsage usage,
            @Nullable RunError error,
            @Nullable Instant finishedAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.conversationId = Objects.requireNonNull(conversationId, "conversationId");
        this.type = Objects.requireNonNull(type, "type");
        this.planId = planId;
        this.queuedAt = Objects.requireNonNull(queuedAt, "queuedAt");
        this.status = Objects.requireNonNull(status, "status");
        this.stop = Objects.requireNonNull(stop, "stop");
        this.usage = Objects.requireNonNull(usage, "usage");
        this.error = error;
        this.finishedAt = finishedAt;
    }

    /** A run answering the conversation. */
    public static Run chat(RunId id, ConversationId conversationId, Instant now) {
        return new Run(
                id,
                conversationId,
                RunType.CHAT,
                null,
                now,
                RunStatus.QUEUED,
                RunStop.NOT_ASKED,
                RunUsage.NONE,
                null,
                null);
    }

    /** A run making the changes of the plan. */
    public static Run execute(RunId id, ConversationId conversationId, PlanId planId, Instant now) {
        return new Run(
                id,
                conversationId,
                RunType.EXECUTE,
                planId,
                now,
                RunStatus.QUEUED,
                RunStop.NOT_ASKED,
                RunUsage.NONE,
                null,
                null);
    }

    /** Takes it to run: only a queued one, and only once. */
    public boolean claim() {
        if (status != RunStatus.QUEUED) {
            return false;
        }
        status = RunStatus.RUNNING;
        return true;
    }

    public void requestStop() {
        if (active()) {
            stop = RunStop.ASKED;
        }
    }

    public boolean stopRequested() {
        return stop == RunStop.ASKED;
    }

    /** One more request to the model. */
    public void step(long tokensIn, long tokensOut) {
        usage = usage.plus(tokensIn, tokensOut);
    }

    /** Ends it, with why it failed. */
    public void end(RunStatus status, @Nullable RunError error, Instant now) {
        if (status.active()) {
            throw new IllegalArgumentException(
                    "A run ends as done, waiting for approval, failed or stopped: " + status);
        }
        if (!active()) {
            return;
        }
        this.status = status;
        this.error = error;
        this.finishedAt = Objects.requireNonNull(now, "now");
    }

    /** Queued or running. */
    public boolean active() {
        return status.active();
    }

    /** Still active a quarter of an hour after it was queued: the process that ran it is gone. */
    public boolean deadAt(Instant now) {
        return active() && now.isAfter(queuedAt.plus(DEAD_AFTER));
    }
}
