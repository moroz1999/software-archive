package com.archive.backend.user.web.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

public record CurrentUserDto(
        UUID id,
        String name,
        String email,
        @ArraySchema(schema = @Schema(ref = "UserGroup")) List<String> groups,
        @ArraySchema(schema = @Schema(ref = "Ability")) List<String> abilities) {}
