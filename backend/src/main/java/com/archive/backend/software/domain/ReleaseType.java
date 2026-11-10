package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import com.archive.backend.shared.domain.error.InvalidValueException;
import java.util.Arrays;

/**
 * The fixed list of release types (docs/domain/software.md), each known by its code.
 */
@ValueObject
public enum ReleaseType {
    ORIGINAL("original"),
    RERELEASE("rerelease"),
    ADAPTATION("adaptation"),
    LOCALIZATION("localization"),
    BUGFIX("bugfix"),
    MOD("mod"),
    CRACK("crack"),
    MIA("mia"),
    CORRUPTED("corrupted"),
    COMPILATION("compilation"),
    INCOMPLETE("incomplete"),
    DEMOVERSION("demoversion"),
    UNKNOWN("unknown");

    private final String code;

    ReleaseType(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    /**
     * The type known by the code.
     */
    public static ReleaseType fromCode(String code) {
        return Arrays.stream(values())
                .filter(type -> type.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new InvalidValueException("release.type-invalid", "The release type is unknown."));
    }
}
