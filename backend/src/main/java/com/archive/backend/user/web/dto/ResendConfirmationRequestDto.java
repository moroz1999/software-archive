package com.archive.backend.user.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.Nullable;

public record ResendConfirmationRequestDto(
        @Nullable String login,
        @Nullable String password,
        @Schema(ref = "Language") @Nullable String language) {}
