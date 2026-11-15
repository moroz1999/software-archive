package com.archive.backend.author.web.dto;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** An entry of the author catalog: of a person ({@code personId}), or a label of nobody ({@code labelId}). */
public record AuthorCatalogEntryDto(
        @Nullable UUID personId,
        String name,
        @Nullable String realName,
        @Nullable String labelOf,
        @Nullable UUID labelId) {}
