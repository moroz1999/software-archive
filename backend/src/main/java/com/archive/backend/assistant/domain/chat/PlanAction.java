package com.archive.backend.assistant.domain.chat;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * A change of a plan: the write tool, its arguments as JSON as the model wrote them, the earlier changes (counted
 * from 1) whose ids it takes, what kind of change it is to which type of entry, and the entry as it was when the plan
 * was proposed — none for a create and for a change that takes an id of an earlier one. Then how it went: the id it
 * produced, or its error.
 */
@ValueObject
public record PlanAction(
        String tool,
        String arguments,
        List<Integer> dependsOn,
        String kind,
        String entityType,
        @Nullable String state,
        PlanActionStatus status,
        @Nullable String result,
        @Nullable String error) {

    public PlanAction {
        Objects.requireNonNull(tool, "tool");
        Objects.requireNonNull(arguments, "arguments");
        dependsOn = List.copyOf(dependsOn);
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(entityType, "entityType");
        Objects.requireNonNull(status, "status");
    }

    /** A change as proposed: not made yet. */
    public static PlanAction proposed(
            String tool,
            String arguments,
            List<Integer> dependsOn,
            String kind,
            String entityType,
            @Nullable String state) {
        return new PlanAction(
                tool, arguments, dependsOn, kind, entityType, state, PlanActionStatus.PENDING, null, null);
    }

    PlanAction with(PlanActionStatus status, @Nullable String result, @Nullable String error) {
        return new PlanAction(tool, arguments, dependsOn, kind, entityType, state, status, result, error);
    }
}
