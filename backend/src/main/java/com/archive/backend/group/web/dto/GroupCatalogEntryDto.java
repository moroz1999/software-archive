package com.archive.backend.group.web.dto;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** An entry of the group catalog: of a group ({@code groupId}), or of one of its labels ({@code labelId}). */
public record GroupCatalogEntryDto(
        @Nullable UUID groupId,
        @Nullable UUID labelId,
        String name,
        @Nullable String note) {}
