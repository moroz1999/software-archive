package com.archive.backend.assistant.web.tool;

/**
 * A tool of the AI assistant: a name, what it does for the model, and the JSON Schema of its arguments, which are
 * read into the record {@code A}. Tools know nothing of the chat, so another transport (an MCP server) offers the same
 * ones.
 */
public interface Tool<A extends Record> {

    String name();

    String description();

    /** The JSON Schema of the arguments: an object, closed at every depth. */
    String schema();

    Class<A> argumentsType();
}
