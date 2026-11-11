package com.archive.backend.search.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/** The word a visitor narrows the software catalog by; never blank. */
@ValueObject
public record CatalogKeyword(String value) {

    public CatalogKeyword {
        Objects.requireNonNull(value, "value");
        value = value.strip();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("A keyword is never blank");
        }
    }

    /** The keyword of a raw value; none for a missing or blank one. */
    public static @Nullable CatalogKeyword of(@Nullable String raw) {
        return raw == null || raw.isBlank() ? null : new CatalogKeyword(raw);
    }
}
