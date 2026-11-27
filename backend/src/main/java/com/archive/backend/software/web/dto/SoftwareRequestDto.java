package com.archive.backend.software.web.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Body of creating and of editing software.
 */
public record SoftwareRequestDto(
        @Nullable String title,
        @Nullable String description,
        @Nullable String instructions,
        @Nullable String releaseDate,
        @Nullable String platform,
        @Nullable List<String> hardware,
        @Nullable List<UUID> categories,
        @Nullable List<CreditDto> credits,
        @Schema(ref = "PreservationStatus") @Nullable String preservation,
        @Nullable List<@Nullable UUID> compilationItemIds) {

    public SoftwareRequestDto(
            @Nullable String title,
            @Nullable String description,
            @Nullable String instructions,
            @Nullable String releaseDate,
            @Nullable String platform,
            @Nullable List<String> hardware,
            @Nullable List<UUID> categories,
            @Nullable List<CreditDto> credits,
            @Nullable String preservation) {
        this(
                title,
                description,
                instructions,
                releaseDate,
                platform,
                hardware,
                categories,
                credits,
                preservation,
                null);
    }

    /** A person, a group or the exact label of either, with the roles they had. */
    public record CreditDto(
            @Nullable UUID id,
            @Schema(ref = "SoftwareReferenceType") @Nullable String type,

            @ArraySchema(schema = @Schema(ref = "CreditRole")) @Nullable
            List<String> roles) {}
}
