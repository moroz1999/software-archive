package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import com.archive.backend.shared.domain.error.InvalidValueException;
import java.util.Arrays;

/** The nature of the rework of the work it comes from (docs/domain/software.md#lineage), each known by its code. */
@ValueObject
public enum LineageRework {
    NO_SUBSTANTIAL_CHANGES("no-substantial-changes"),
    ORIGINAL_MATERIAL_USED("original-material-used"),
    NEW_LEVELS_ADDED("new-levels-added"),
    GRAPHICS_AND_SOUND_CHANGED("graphics-and-sound-changed"),
    NEW_IMPLEMENTATION("new-implementation");

    private final String code;

    LineageRework(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    /** The value known by the code. */
    public static LineageRework fromCode(String code) {
        return Arrays.stream(values())
                .filter(value -> value.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new InvalidValueException(
                        "software.lineage-rework-invalid", "The rework of the lineage is unknown."));
    }
}
