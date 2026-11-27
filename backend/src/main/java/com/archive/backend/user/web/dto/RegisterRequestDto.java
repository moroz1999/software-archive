package com.archive.backend.user.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.Nullable;

/**
 * The interface language picks the language of the confirmation letter.
 */
public record RegisterRequestDto(
        @Nullable String name,
        @Nullable String email,
        @Nullable String password,
        @Schema(ref = "Language") @Nullable String language) {}
