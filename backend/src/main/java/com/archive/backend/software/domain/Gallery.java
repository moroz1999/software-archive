package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import com.archive.backend.shared.domain.error.InvalidValueException;
import java.util.Arrays;

/**
 * The galleries of pictures software has (docs/domain/software.md#pictures), each known by its code; a release has
 * only screenshots.
 */
@ValueObject
public enum Gallery {
    SCREENSHOTS("screenshots"),
    MAPS("maps"),
    COVERS("covers"),
    ADVERTS("adverts");

    private final String code;

    Gallery(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    public static Gallery fromCode(String code) {
        return Arrays.stream(values())
                .filter(gallery -> gallery.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new InvalidValueException("gallery.invalid", "The gallery is unknown."));
    }
}
