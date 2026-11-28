package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import com.archive.backend.shared.domain.error.InvalidValueException;
import java.util.Objects;

/** The work software comes from, as free text: its title, authors, year, platform, page (docs/domain/software.md#lineage). */
@ValueObject
public record LineageSource(String value) {

    public static final int MAX_LENGTH = 255;

    public LineageSource {
        Objects.requireNonNull(value, "value");
        if (value.isBlank() || value.length() > MAX_LENGTH) {
            throw new InvalidValueException(
                    "software.lineage-source-invalid", "The original must be text of at most 255 characters.");
        }
    }
}
