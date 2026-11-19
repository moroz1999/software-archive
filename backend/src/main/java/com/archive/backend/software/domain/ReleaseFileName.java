package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Locale;
import java.util.Objects;

@ValueObject
public record ReleaseFileName(String value) {

    public ReleaseFileName {
        Objects.requireNonNull(value, "value");
        if (value.isEmpty()) {
            throw new IllegalArgumentException("A release file needs a name.");
        }
    }

    public String extension() {
        int dot = value.lastIndexOf('.');
        return dot < 0 || dot == value.length() - 1
                ? ""
                : value.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
