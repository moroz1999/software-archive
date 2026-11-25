package com.archive.backend.assistant.read.model;

/**
 * A chat model that may answer with calls of tools. It keeps nothing between requests: each carries the whole
 * conversation. One that fails, or is not configured, says so with a business exception.
 */
public interface LanguageModel {

    ModelReply complete(ModelRequest request);
}
