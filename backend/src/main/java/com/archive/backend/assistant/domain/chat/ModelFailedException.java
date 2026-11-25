package com.archive.backend.assistant.domain.chat;

import com.archive.backend.shared.domain.error.RuleViolationException;

/** The language model could not be asked, or answered with an error; also when it is not configured. */
public class ModelFailedException extends RuleViolationException {

    private static final long serialVersionUID = 1L;

    public ModelFailedException(String message) {
        super("assistant.model-failed", message);
    }
}
