package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import com.archive.backend.shared.domain.error.InvalidValueException;
import java.util.Arrays;

/** What the work is to the work it comes from (docs/domain/software.md#lineage), each known by its code. */
@ValueObject
public enum LineageKind {
    ORIGINAL("original"),
    PORT("port"),
    PORT_WITH_CHANGES("port-with-changes"),
    BASED_ON("based-on"),
    CLONE("clone"),
    REMAKE("remake"),
    REMASTER("remaster"),
    MODIFICATION("modification"),
    NEW_LEVELS("new-levels"),
    SEQUEL("sequel"),
    DEMO("demo");

    private final String code;

    LineageKind(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    /** The value known by the code. */
    public static LineageKind fromCode(String code) {
        return Arrays.stream(values())
                .filter(value -> value.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new InvalidValueException(
                        "software.lineage-kind-invalid", "The kind of the lineage is unknown."));
    }
}
