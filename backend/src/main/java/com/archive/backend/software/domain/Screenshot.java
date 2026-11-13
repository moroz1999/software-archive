package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;

/** A screenshot of software or of a release: its picture is stored under its id. */
@ValueObject
public record Screenshot(ScreenshotId id, ScreenshotFormat format) {

    public Screenshot {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(format, "format");
    }
}
