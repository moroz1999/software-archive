package com.archive.backend.user.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import com.archive.backend.shared.domain.error.InvalidValueException;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/** An email, kept in lower case: one account per email, whatever its letter case. */
@ValueObject
public record Email(String value) {

    public static final int MAX_LENGTH = 254;
    private static final Pattern FORMAT = Pattern.compile("[^@\\s]+@[^@\\s]+");

    public Email {
        Objects.requireNonNull(value, "value");
        value = value.strip().toLowerCase(Locale.ROOT);
        if (value.isEmpty()) {
            throw new InvalidValueException("user.email-required", "An email must not be blank.");
        }
        if (value.length() > MAX_LENGTH) {
            throw new InvalidValueException(
                    "user.email-too-long", "An email must be at most " + MAX_LENGTH + " characters long.");
        }
        if (!FORMAT.matcher(value).matches()) {
            throw new InvalidValueException("user.email-invalid", "Not an email.");
        }
    }
}
