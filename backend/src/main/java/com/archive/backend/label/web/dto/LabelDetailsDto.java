package com.archive.backend.label.web.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public record LabelDetailsDto(
        UUID id,
        String name,
        @Nullable OwnerDto owner,
        List<MemberOfDto> groups,
        List<LocationDto> locations,
        List<OriginDto> origins,
        AddedDto added,
        boolean canChange,
        boolean canMakeOwner,
        boolean canEdit,
        boolean canChangeOwner,
        boolean canGiveToGroup) {

    public record OwnerDto(
            String type, UUID id, String name, @Nullable String note) {}

    public record MemberOfDto(UUID id, String name) {}

    public record LocationDto(
            @Nullable UUID countryId,
            @Nullable String country,
            @Nullable UUID cityId,
            @Nullable String city,
            boolean primary) {}

    public record OriginDto(
            String origin, String externalId, @Nullable String url) {}

    /** Who added the entry, {@code null} once their account was deleted, and when. */
    public record AddedDto(@Nullable String by, Instant at) {}
}
