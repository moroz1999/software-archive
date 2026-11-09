package com.archive.backend.author.web.dto;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

public record AuthorCatalogEntryDto(
        UUID personId,
        String name,
        @Nullable String realName,
        @Nullable String labelOf) {}
