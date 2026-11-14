package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import com.archive.backend.shared.domain.error.InvalidValueException;
import java.util.Arrays;

/** A kind of person, group or label that software can reference. */
@ValueObject
public enum SoftwareReferenceType {
    AUTHOR("author"),
    AUTHOR_LABEL("author-label"),
    GROUP("group"),
    GROUP_LABEL("group-label");

    private final String code;

    SoftwareReferenceType(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    public static SoftwareReferenceType fromCode(String code) {
        return Arrays.stream(values())
                .filter(type -> type.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new InvalidValueException(
                        "software.reference-type-invalid", "The software reference type is invalid."));
    }
}
