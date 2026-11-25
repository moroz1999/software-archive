package com.archive.backend.assistant.read.model;

import java.util.List;
import java.util.UUID;

/** The whole conversation as the model is told it, the tools it may call, and the run asking, for the log. */
public record ModelRequest(List<ModelMessage> messages, List<ModelTool> tools, UUID conversationId, UUID runId) {

    public ModelRequest {
        messages = List.copyOf(messages);
        tools = List.copyOf(tools);
    }
}
