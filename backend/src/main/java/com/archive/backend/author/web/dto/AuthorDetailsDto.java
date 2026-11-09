package com.archive.backend.author.web.dto;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public record AuthorDetailsDto(
        UUID id,
        String name,
        @Nullable String realName,
        @Nullable String notes,
        List<LabelDto> labels,
        List<LocationDto> locations,
        List<GroupDto> groups
) {

    public record LabelDto(UUID id, String name) {
    }

    public record LocationDto(@Nullable String country, @Nullable String city, boolean primary) {
    }

    public record GroupDto(UUID id, String name, @Nullable String as) {
    }
}
