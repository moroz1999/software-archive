package com.archive.backend.assistant.domain.chat;

import com.archive.backend.shared.domain.error.ConflictException;

/** The plan is no longer waiting for the editor: run, cancelled, or replaced by a later message. */
public class PlanNotProposedException extends ConflictException {

    private static final long serialVersionUID = 1L;

    public PlanNotProposedException() {
        super("assistant.plan-not-proposed", "The plan no longer waits for approval.");
    }
}
