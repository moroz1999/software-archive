package com.archive.backend.search.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** A person, or a label of nobody, found by the search: {@code kind} tells which. */
public record FoundAuthorDto(
        UUID id,
        String name,
        @Nullable String realName,
        @Schema(allowableValues = {"person", "label"}) String kind) {}
