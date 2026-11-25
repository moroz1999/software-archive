package com.archive.backend.assistant.read.chat;

import java.util.List;
import org.jspecify.annotations.Nullable;

/** A change of a plan, its arguments, its state and its result as JSON. */
public record ChatPlanAction(
        int position,
        String tool,
        String arguments,
        List<Integer> dependsOn,
        String kind,
        String entityType,
        @Nullable String state,
        String status,
        @Nullable String result,
        @Nullable String error) {}
