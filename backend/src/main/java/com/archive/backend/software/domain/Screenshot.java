package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;

/**
 * A screenshot of software or of a release: its picture is stored under its id; the name of its original file and its
 * size are what the archive knows of it.
 */
@ValueObject
public record Screenshot(ScreenshotId id, ScreenshotFormat format, ScreenshotFileName name, long size) {

    public Screenshot {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(format, "format");
        Objects.requireNonNull(name, "name");
    }
}
