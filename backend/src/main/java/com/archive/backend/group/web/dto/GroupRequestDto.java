package com.archive.backend.group.web.dto;

import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Body of creating and of editing a group. A label without an id is a new one.
 */
public record GroupRequestDto(
        @Nullable String name,
        @Nullable String notes,
        @Nullable List<String> types,
        @Nullable List<LabelDto> labels,
        @Nullable List<LocationDto> locations,
        @Nullable List<MemberDto> members) {

    public GroupRequestDto(
            @Nullable String name,
            @Nullable String notes,
            @Nullable List<String> types,
            @Nullable List<LabelDto> labels,
            @Nullable List<LocationDto> locations) {
        this(name, notes, types, labels, locations, null);
    }

    public record LabelDto(@Nullable UUID id, @Nullable String name) {}

    public record LocationDto(
            @Nullable UUID country,
            @Nullable UUID city,
            @Nullable Boolean primary) {}

    public record MemberDto(
            @Nullable UUID id,
            @Nullable String type,
            @Nullable List<String> roles) {}
}
