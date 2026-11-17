package com.archive.backend.label.web.dto;

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
        boolean canChange,
        boolean canMakeOwner,
        boolean canEdit,
        boolean canChangeOwner) {

    public record OwnerDto(
            String type, UUID id, String name, @Nullable String note) {}

    public record MemberOfDto(UUID id, String name) {}

    public record LocationDto(
            UUID countryId,
            String country,
            @Nullable UUID cityId,
            @Nullable String city,
            boolean primary) {}

    public record OriginDto(
            String origin, String externalId, @Nullable String url) {}
}
