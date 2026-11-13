package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;
import java.util.UUID;

@ValueObject
public record ScreenshotId(UUID value) {

    public ScreenshotId {
        Objects.requireNonNull(value, "value");
    }
}
