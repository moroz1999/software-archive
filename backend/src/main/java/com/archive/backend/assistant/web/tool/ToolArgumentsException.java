package com.archive.backend.assistant.web.tool;

/** The arguments of a call do not fit the tool's schema; the message is told to the model. */
public class ToolArgumentsException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ToolArgumentsException(String message) {
        super(message);
    }
}
