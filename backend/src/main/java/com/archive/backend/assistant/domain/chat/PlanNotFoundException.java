package com.archive.backend.assistant.domain.chat;

import com.archive.backend.shared.domain.error.NotFoundException;

/** No such plan, or one of a conversation of somebody else. */
public class PlanNotFoundException extends NotFoundException {

    private static final long serialVersionUID = 1L;

    public PlanNotFoundException() {
        super("assistant.plan-not-found", "No such plan.");
    }
}
