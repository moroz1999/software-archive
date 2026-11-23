package com.archive.backend.hardware.domain;

import com.archive.backend.shared.domain.error.RuleViolationException;

/** A group of hardware gone from the reference book in the meantime. */
public class HardwareGroupUnknownException extends RuleViolationException {

    private static final long serialVersionUID = 1L;

    public HardwareGroupUnknownException() {
        super("hardware.group-unknown", "The group is not in the reference book.", "group");
    }
}
