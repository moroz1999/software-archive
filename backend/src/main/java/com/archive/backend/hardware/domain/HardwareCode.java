package com.archive.backend.hardware.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import com.archive.backend.shared.domain.error.InvalidValueException;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * The code an item of hardware is known by: lower-case letters and digits, words joined by a hyphen, such as {@code ay8910}.
 */
@ValueObject
public record HardwareCode(String value) {

    private static final Pattern CODE = Pattern.compile("[a-z0-9]+(-[a-z0-9]+)*");

    public HardwareCode {
        Objects.requireNonNull(value, "value");
        if (!CODE.matcher(value).matches()) {
            throw new InvalidValueException(
                    "hardware.code-invalid", "A hardware code is lower-case letters and digits.");
        }
    }
}
