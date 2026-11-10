package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import com.archive.backend.shared.domain.error.InvalidValueException;
import java.util.Objects;

/**
 * The version of a release, such as {@code 1.1}.
 */
@ValueObject
public record ReleaseVersion(String value) {

    public static final int MAX_LENGTH = 50;

    public ReleaseVersion {
        Objects.requireNonNull(value, "value");
        if (value.length() > MAX_LENGTH) {
            throw new InvalidValueException(
                    "release.version-too-long", "A version must be at most " + MAX_LENGTH + " characters long.");
        }
    }
}
