package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;
import java.util.regex.Pattern;

@ValueObject
public record ReleaseFileHash(String value) {

    private static final Pattern MD5 = Pattern.compile("[0-9a-f]{32}");

    public ReleaseFileHash {
        Objects.requireNonNull(value, "value");
        if (!MD5.matcher(value).matches()) {
            throw new IllegalArgumentException("A release file hash must be a lowercase MD5 digest.");
        }
    }
}
