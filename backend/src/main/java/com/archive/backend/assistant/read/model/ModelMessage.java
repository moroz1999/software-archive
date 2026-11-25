package com.archive.backend.assistant.read.model;

import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * A message of what the model is told: prose, with pictures for a user's; the calls the model made; the result of a
 * call, by its id.
 */
public record ModelMessage(
        ModelRole role,
        String text,
        List<ModelToolCall> toolCalls,
        @Nullable String toolCallId,
        List<ModelImage> images) {

    public ModelMessage {
        toolCalls = List.copyOf(toolCalls);
        images = List.copyOf(images);
    }

    public static ModelMessage of(ModelRole role, String text) {
        return new ModelMessage(role, text, List.of(), null, List.of());
    }

    public static ModelMessage calls(List<ModelToolCall> calls) {
        return new ModelMessage(ModelRole.ASSISTANT, "", calls, null, List.of());
    }

    public static ModelMessage result(String callId, String text) {
        return new ModelMessage(ModelRole.TOOL, text, List.of(), callId, List.of());
    }

    public static ModelMessage images(String text, List<ModelImage> images) {
        return new ModelMessage(ModelRole.USER, text, List.of(), null, images);
    }
}
