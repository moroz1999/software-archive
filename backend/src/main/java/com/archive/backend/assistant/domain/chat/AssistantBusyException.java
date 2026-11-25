package com.archive.backend.assistant.domain.chat;

import com.archive.backend.shared.domain.error.ConflictException;

/** The assistant still works on the conversation: one task at a time. */
public class AssistantBusyException extends ConflictException {

    private static final long serialVersionUID = 1L;

    public AssistantBusyException() {
        super("assistant.busy", "The assistant still works on this conversation.");
    }
}
