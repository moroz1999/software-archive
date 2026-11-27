package com.archive.backend.importer.web.dto;

import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import org.jspecify.annotations.Nullable;

/** A line of the log of a run: {@code kind} tells what {@code entry} is. */
@Schema(
        oneOf = {
            ImportRunLogLineDto.ListRead.class,
            ImportRunLogLineDto.Downloaded.class,
            ImportRunLogLineDto.Added.class,
            ImportRunLogLineDto.Known.class,
            ImportRunLogLineDto.Skipped.class,
            ImportRunLogLineDto.Failed.class,
            ImportRunLogLineDto.Author.class,
            ImportRunLogLineDto.Place.class
        },
        discriminatorProperty = "kind",
        discriminatorMapping = {
            @DiscriminatorMapping(value = "list", schema = ImportRunLogLineDto.ListRead.class),
            @DiscriminatorMapping(value = "download", schema = ImportRunLogLineDto.Downloaded.class),
            @DiscriminatorMapping(value = "added", schema = ImportRunLogLineDto.Added.class),
            @DiscriminatorMapping(value = "known", schema = ImportRunLogLineDto.Known.class),
            @DiscriminatorMapping(value = "skipped", schema = ImportRunLogLineDto.Skipped.class),
            @DiscriminatorMapping(value = "failed", schema = ImportRunLogLineDto.Failed.class),
            @DiscriminatorMapping(value = "author", schema = ImportRunLogLineDto.Author.class),
            @DiscriminatorMapping(value = "place", schema = ImportRunLogLineDto.Place.class)
        })
public sealed interface ImportRunLogLineDto {

    record ListRead(
            long seq,
            @Nullable Instant at,
            @Schema(allowableValues = "list") String kind,
            ImportRunListReadDto entry) implements ImportRunLogLineDto {
        public ListRead(long seq, @Nullable Instant at, ImportRunListReadDto entry) {
            this(seq, at, "list", entry);
        }
    }

    record Downloaded(
            long seq,
            @Nullable Instant at,
            @Schema(allowableValues = "download") String kind,
            ImportRunDownloadedDto entry)
            implements ImportRunLogLineDto {
        public Downloaded(long seq, @Nullable Instant at, ImportRunDownloadedDto entry) {
            this(seq, at, "download", entry);
        }
    }

    record Added(
            long seq,
            @Nullable Instant at,
            @Schema(allowableValues = "added") String kind,
            ImportRunAddedDto entry) implements ImportRunLogLineDto {
        public Added(long seq, @Nullable Instant at, ImportRunAddedDto entry) {
            this(seq, at, "added", entry);
        }
    }

    record Known(
            long seq,
            @Nullable Instant at,
            @Schema(allowableValues = "known") String kind,
            ImportRunKnownDto entry) implements ImportRunLogLineDto {
        public Known(long seq, @Nullable Instant at, ImportRunKnownDto entry) {
            this(seq, at, "known", entry);
        }
    }

    record Skipped(
            long seq,
            @Nullable Instant at,
            @Schema(allowableValues = "skipped") String kind,
            ImportRunSkippedDto entry) implements ImportRunLogLineDto {
        public Skipped(long seq, @Nullable Instant at, ImportRunSkippedDto entry) {
            this(seq, at, "skipped", entry);
        }
    }

    record Failed(
            long seq,
            @Nullable Instant at,
            @Schema(allowableValues = "failed") String kind,
            ImportRunFailedDto entry) implements ImportRunLogLineDto {
        public Failed(long seq, @Nullable Instant at, ImportRunFailedDto entry) {
            this(seq, at, "failed", entry);
        }
    }

    record Author(
            long seq,
            @Nullable Instant at,
            @Schema(allowableValues = "author") String kind,
            ImportRunAuthorDto entry) implements ImportRunLogLineDto {
        public Author(long seq, @Nullable Instant at, ImportRunAuthorDto entry) {
            this(seq, at, "author", entry);
        }
    }

    record Place(
            long seq,
            @Nullable Instant at,
            @Schema(allowableValues = "place") String kind,
            ImportRunPlaceDto entry) implements ImportRunLogLineDto {
        public Place(long seq, @Nullable Instant at, ImportRunPlaceDto entry) {
            this(seq, at, "place", entry);
        }
    }
}
