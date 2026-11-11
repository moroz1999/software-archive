package com.archive.backend.author.web.dto;

import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Body of creating and of editing an author. A label without an id is a new one.
 */
public record AuthorRequestDto(
        @Nullable String realName,
        @Nullable String notes,
        @Nullable List<LabelDto> labels,
        @Nullable List<LocationDto> locations) {

    public record LabelDto(
            @Nullable UUID id,
            @Nullable String name,
            @Nullable Boolean display) {}

    public record LocationDto(
            @Nullable UUID country,
            @Nullable UUID city,
            @Nullable Boolean primary) {}
}
