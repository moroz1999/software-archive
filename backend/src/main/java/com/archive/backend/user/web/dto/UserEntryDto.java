package com.archive.backend.user.web.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UserEntryDto(
        UUID id,
        String name,
        String email,
        boolean confirmed,
        Instant registeredAt,
        @ArraySchema(schema = @Schema(ref = "UserGroup")) List<String> groups) {}
