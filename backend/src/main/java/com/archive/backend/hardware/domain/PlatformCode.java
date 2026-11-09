package com.archive.backend.hardware.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import com.archive.backend.shared.domain.error.InvalidValueException;
import java.util.Objects;
import java.util.regex.Pattern;

/** The code a platform is known by: lower-case letters and digits, words joined by a hyphen, such as {@code vector}. */
@ValueObject
public record PlatformCode(String value) {

    private static final Pattern CODE = Pattern.compile("[a-z0-9]+(-[a-z0-9]+)*");

    public PlatformCode {
        Objects.requireNonNull(value, "value");
        if (!CODE.matcher(value).matches()) {
            throw new InvalidValueException(
                    "platform.code-invalid", "A platform code is lower-case letters and digits.");
        }
    }
}
