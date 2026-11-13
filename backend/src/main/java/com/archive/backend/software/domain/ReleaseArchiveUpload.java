package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;

/** A release archive as uploaded, kept byte for byte and limited to 100 MB. */
@ValueObject
public record ReleaseArchiveUpload(String fileName, long size, ReleaseArchiveContent content) {

    public static final long MAX_BYTES = 100L * 1024 * 1024;

    public ReleaseArchiveUpload {
        Objects.requireNonNull(fileName, "fileName");
        Objects.requireNonNull(content, "content");
        if (fileName.isBlank()) {
            throw new ReleaseArchiveNameInvalidException();
        }
        if (size < 0 || size > MAX_BYTES) {
            throw new ReleaseArchiveTooLargeException();
        }
    }
}
