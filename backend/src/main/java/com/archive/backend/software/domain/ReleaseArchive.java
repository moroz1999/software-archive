package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;

/** The one downloadable file attached to a release, with the name and size visitors see. */
@ValueObject
public record ReleaseArchive(ReleaseArchiveId id, String fileName, long size) {

    public ReleaseArchive {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(fileName, "fileName");
        if (fileName.isBlank()) {
            throw new ReleaseArchiveNameInvalidException();
        }
        if (size < 0 || size > ReleaseArchiveUpload.MAX_BYTES) {
            throw new ReleaseArchiveTooLargeException();
        }
    }
}
