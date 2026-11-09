package com.archive.backend.author.web.dto;

import org.jspecify.annotations.Nullable;

import java.util.UUID;

public record AuthorCatalogEntryDto(UUID personId, String name, @Nullable String realName, @Nullable String labelOf) {
}
