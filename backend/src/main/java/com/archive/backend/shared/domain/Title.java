package com.archive.backend.shared.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import com.archive.backend.shared.domain.error.InvalidValueException;

import java.util.Objects;

/**
 * A title of anything in the archive. Every kind of title is a final subclass, so the shared rules
 * (required, not blank, at most {@value #MAX_LENGTH} characters) cannot be skipped and titles of
 * different kinds are never equal.
 */
@ValueObject
public abstract class Title {

    public static final int MAX_LENGTH = 255;

    private final String value;

    protected Title(String value) {
        Objects.requireNonNull(value, "value");
        if (value.isBlank()) {
            throw new InvalidValueException("title.required", "A title must not be blank.");
        }
        if (value.length() > MAX_LENGTH) {
            throw new InvalidValueException("title.too-long", "A title must be at most " + MAX_LENGTH + " characters long.");
        }
        this.value = value;
    }

    public final String value() {
        return value;
    }

    /** Titles of different kinds are never equal, even with the same text: hence the class, not instanceof. */
    @Override
    @SuppressWarnings("EqualsGetClass")
    public final boolean equals(Object other) {
        return other != null && other.getClass() == getClass() && ((Title) other).value.equals(value);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(getClass(), value);
    }

    @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + value + "]";
    }
}
