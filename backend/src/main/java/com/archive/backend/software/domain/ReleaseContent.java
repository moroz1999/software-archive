package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.time.LocalDate;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * What a release states about itself besides its hardware (docs/domain/software.md).
 */
@ValueObject
public record ReleaseContent(
        @Nullable ReleaseTitle title,
        @Nullable ReleaseVersion version,
        ReleaseType type,
        @Nullable LocalDate releaseDate,
        @Nullable ReleaseDescription description) {

    public ReleaseContent {
        Objects.requireNonNull(type, "type");
    }
}
