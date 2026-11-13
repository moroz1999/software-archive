package com.archive.backend.user.web.dto;

import org.jspecify.annotations.Nullable;

/** The interface language picks the language of the confirmation letter. */
public record RegisterRequestDto(
        @Nullable String name,
        @Nullable String email,
        @Nullable String password,
        @Nullable String language) {}
