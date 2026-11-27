package com.archive.backend.user.web.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * An account as an administrator changes it: the groups are those given on purpose, by their codes.
 */
public record UserRequestDto(
        @Nullable String name,
        @Nullable String email,

        @ArraySchema(schema = @Schema(ref = "UserGroup")) @Nullable
        List<String> groups) {}
