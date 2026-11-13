package com.archive.backend.user.web.dto;

import org.jspecify.annotations.Nullable;

/** The login is the user name or the email. */
public record LogInRequestDto(
        @Nullable String login,
        @Nullable String password,
        @Nullable Boolean remember) {}
