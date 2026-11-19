package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;

/** The name of the file a screenshot was uploaded or imported from, as it was (docs/domain/imports.md#software). */
@ValueObject
public record ScreenshotFileName(String value) {

    public ScreenshotFileName {
        Objects.requireNonNull(value, "value");
    }
}
