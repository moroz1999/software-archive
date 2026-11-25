package com.archive.backend.assistant.read.model;

import java.util.List;

/** What the model answered: prose, calls of tools, and the tokens the request took. */
public record ModelReply(String text, List<ModelToolCall> toolCalls, long tokensIn, long tokensOut) {

    public ModelReply {
        toolCalls = List.copyOf(toolCalls);
    }
}
