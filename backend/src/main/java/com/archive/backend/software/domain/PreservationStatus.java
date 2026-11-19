package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import com.archive.backend.shared.domain.error.InvalidValueException;
import java.util.Arrays;

/** Whether the software can be had and how (docs/domain/software.md), each status known by its code. */
@ValueObject
public enum PreservationStatus {
    UNKNOWN("unknown"),
    MISSING_IN_ACTION("missing-in-action"),
    DONATIONWARE("donationware"),
    IN_SALES("in-sales");

    private final String code;

    PreservationStatus(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    /** The status known by the code. */
    public static PreservationStatus fromCode(String code) {
        return Arrays.stream(values())
                .filter(status -> status.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new InvalidValueException(
                        "software.preservation-invalid", "The preservation status is unknown."));
    }
}
