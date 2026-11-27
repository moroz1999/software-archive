package com.archive.backend.software.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** One chosen person, group or exact label as the software form shows it. */
public record SoftwareReferenceDto(
        UUID id,
        @Nullable UUID ownerId,
        @Schema(ref = "SoftwareReferenceType") String type,
        String title) {}
