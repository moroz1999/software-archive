package com.archive.backend.importer.domain.source;

import com.archive.backend.hardware.domain.HardwareCode;
import com.archive.backend.shared.domain.ddd.ValueObject;
import com.archive.backend.software.domain.ReleaseArchiveUpload;
import com.archive.backend.software.domain.ReleaseDate;
import com.archive.backend.software.domain.ReleaseDescription;
import java.util.Set;
import org.jspecify.annotations.Nullable;

/**
 * A release of a program as a source gives it: its file, what the source says of it, its date and its hardware, such
 * as its storage. What the source does not tell stays empty.
 */
@ValueObject
public record SourceRelease(
        @Nullable ReleaseDescription description,
        @Nullable ReleaseDate date,
        Set<HardwareCode> hardware,
        @Nullable ReleaseArchiveUpload file) {

    public SourceRelease {
        hardware = Set.copyOf(hardware);
    }
}
