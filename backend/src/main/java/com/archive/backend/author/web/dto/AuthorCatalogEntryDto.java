package com.archive.backend.author.web.dto;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** An entry of the author catalog: of a person ({@code personId}), or of a label ({@code labelId}). */
public record AuthorCatalogEntryDto(
        @Nullable UUID personId,
        @Nullable UUID labelId,
        String name,
        @Nullable String note) {}
