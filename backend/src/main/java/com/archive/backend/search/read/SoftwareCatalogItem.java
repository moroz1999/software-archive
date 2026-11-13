package com.archive.backend.search.read;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** A card of the software catalog, with the screenshots it shows (docs/domain/software.md#screenshots). */
public record SoftwareCatalogItem(
        UUID id, String title, @Nullable LocalDate releaseDate, List<UUID> screenshots) {

    public SoftwareCatalogItem(UUID id, String title, @Nullable LocalDate releaseDate) {
        this(id, title, releaseDate, List.of());
    }
}
