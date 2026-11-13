package com.archive.backend.user.web.dto;

import org.jspecify.annotations.Nullable;

public record ResendConfirmationRequestDto(
        @Nullable String login,
        @Nullable String password,
        @Nullable String language) {}
