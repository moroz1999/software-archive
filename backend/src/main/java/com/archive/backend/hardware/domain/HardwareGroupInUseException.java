package com.archive.backend.hardware.domain;

import com.archive.backend.shared.domain.error.RuleViolationException;

/** A group with hardware in it, kept in the reference book. */
public class HardwareGroupInUseException extends RuleViolationException {

    private static final long serialVersionUID = 1L;

    public HardwareGroupInUseException() {
        super("hardware.group-in-use", "The group has hardware in it.", null);
    }
}
