package com.archive.backend.author.web.dto;

import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public record AuthorDetailsDto(
        UUID id,
        String name,
        @Nullable String realName,
        @Nullable String notes,
        List<LabelDto> labels,
        List<LocationDto> locations,
        List<GroupDto> groups) {

    public record LabelDto(UUID id, String name, boolean display) {}

    public record LocationDto(
            @Nullable UUID countryId,
            @Nullable String country,
            @Nullable UUID cityId,
            @Nullable String city,
            boolean primary) {}

    public record GroupDto(UUID id, String name, @Nullable String as) {}
}
