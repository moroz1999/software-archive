package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import com.archive.backend.shared.domain.error.InvalidValueException;
import java.util.Arrays;

/** How reliable the kind of a lineage is (docs/domain/software.md#lineage), each known by its code. */
@ValueObject
public enum LineageReliability {
    RELIABLE("reliable"),
    ASSUMPTION("assumption");

    private final String code;

    LineageReliability(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    /** The value known by the code. */
    public static LineageReliability fromCode(String code) {
        return Arrays.stream(values())
                .filter(value -> value.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new InvalidValueException(
                        "software.lineage-reliability-invalid", "The reliability of the lineage is unknown."));
    }
}
