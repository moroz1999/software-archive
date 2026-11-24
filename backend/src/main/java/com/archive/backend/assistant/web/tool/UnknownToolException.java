package com.archive.backend.assistant.web.tool;

/** No tool has the name a call gave; the message is told to the model. */
public class UnknownToolException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public UnknownToolException(String name) {
        super("Unknown tool: " + name);
    }
}
