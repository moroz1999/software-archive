package com.archive.backend.assistant.domain.chat;

import com.archive.backend.shared.domain.ddd.AggregateRoot;
import com.archive.backend.shared.domain.error.InvalidValueException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

/**
 * Changes the assistant proposes, made one by one once the editor approves them all; the first that fails stops the
 * rest. Writing another message instead of approving drops it, and so does cancelling it.
 */
@Getter
@AggregateRoot
public final class Plan {

    /** Why a change being made when the process ended failed. */
    public static final String INTERRUPTED =
            "The change was interrupted by a restart and is not made again: look what it did.";

    private final PlanId id;
    private final ConversationId conversationId;
    private final PlanTitle title;
    private final Instant proposedAt;
    private PlanStatus status;
    private List<PlanAction> actions;
    private @Nullable Instant finishedAt;

    /** A plan as it is stored. */
    public Plan(
            PlanId id,
            ConversationId conversationId,
            PlanTitle title,
            Instant proposedAt,
            PlanStatus status,
            List<PlanAction> actions,
            @Nullable Instant finishedAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.conversationId = Objects.requireNonNull(conversationId, "conversationId");
        this.title = Objects.requireNonNull(title, "title");
        this.proposedAt = Objects.requireNonNull(proposedAt, "proposedAt");
        this.status = Objects.requireNonNull(status, "status");
        this.actions = new ArrayList<>(actions);
        this.finishedAt = finishedAt;
    }

    public static Plan propose(
            PlanId id, ConversationId conversationId, PlanTitle title, List<PlanAction> actions, Instant now) {
        if (actions.isEmpty()) {
            throw new InvalidValueException("assistant.plan-empty", "A plan holds at least one change.");
        }
        return new Plan(id, conversationId, title, now, PlanStatus.PROPOSED, actions, null);
    }

    public List<PlanAction> getActions() {
        return List.copyOf(actions);
    }

    /** The editor approved it: its changes are made now. */
    public void approve() {
        requireProposed();
        status = PlanStatus.EXECUTING;
    }

    /** The editor wrote another message instead; a plan no longer waiting stays as it is. */
    public void supersede(Instant now) {
        if (status == PlanStatus.PROPOSED) {
            end(PlanStatus.SUPERSEDED, now);
        }
    }

    public void cancel(Instant now) {
        requireProposed();
        end(PlanStatus.CANCELLED, now);
    }

    /** The change at the position, counted from 1, is being made. */
    public void start(int position) {
        requireExecuting();
        set(position, action(position).with(PlanActionStatus.RUNNING, null, null));
    }

    /** The change at the position is made, producing the id. */
    public void done(int position, String result, Instant now) {
        requireExecuting();
        set(position, action(position).with(PlanActionStatus.DONE, result, null));
        if (actions.stream().allMatch(action -> action.status() == PlanActionStatus.DONE)) {
            end(PlanStatus.DONE, now);
        }
    }

    /** The change at the position failed: the rest are skipped, the plan failed. */
    public void fail(int position, String error, Instant now) {
        requireExecuting();
        set(position, action(position).with(PlanActionStatus.FAILED, null, error));
        skipTheRest();
        end(PlanStatus.FAILED, now);
    }

    /** The editor stopped it before the next change: the rest are skipped. */
    public void stop(Instant now) {
        requireExecuting();
        skipTheRest();
        end(PlanStatus.STOPPED, now);
    }

    /**
     * The process making it ended in the middle: the change being made, perhaps half-made, failed and is not made
     * again; the rest are skipped.
     */
    public void interrupt(Instant now) {
        requireExecuting();
        for (int i = 0; i < actions.size(); i++) {
            if (actions.get(i).status() == PlanActionStatus.RUNNING) {
                actions.set(i, actions.get(i).with(PlanActionStatus.FAILED, null, INTERRUPTED));
            }
        }
        skipTheRest();
        end(PlanStatus.FAILED, now);
    }

    /** The number of changes, counted from 1 to it. */
    public int size() {
        return actions.size();
    }

    private PlanAction action(int position) {
        if (position < 1 || position > actions.size()) {
            throw new IllegalArgumentException("No change " + position + " in a plan of " + actions.size());
        }
        return actions.get(position - 1);
    }

    private void set(int position, PlanAction action) {
        actions.set(position - 1, action);
    }

    private void skipTheRest() {
        for (int i = 0; i < actions.size(); i++) {
            PlanActionStatus current = actions.get(i).status();
            if (current == PlanActionStatus.PENDING || current == PlanActionStatus.RUNNING) {
                actions.set(i, actions.get(i).with(PlanActionStatus.SKIPPED, null, null));
            }
        }
    }

    private void end(PlanStatus status, Instant now) {
        this.status = status;
        this.finishedAt = Objects.requireNonNull(now, "now");
    }

    private void requireProposed() {
        if (status != PlanStatus.PROPOSED) {
            throw new PlanNotProposedException();
        }
    }

    private void requireExecuting() {
        if (status != PlanStatus.EXECUTING) {
            throw new IllegalStateException("The plan " + id.value() + " is not being made: " + status);
        }
    }
}
