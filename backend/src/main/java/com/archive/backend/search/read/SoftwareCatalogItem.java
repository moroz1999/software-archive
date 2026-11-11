package com.archive.backend.search.read;

import java.time.LocalDate;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** A card of the software catalog. */
public record SoftwareCatalogItem(
        UUID id, String title, @Nullable LocalDate releaseDate) {}
