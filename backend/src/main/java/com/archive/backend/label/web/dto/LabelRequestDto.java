package com.archive.backend.label.web.dto;

import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Body of editing a label: its name, whose it is after saving ({@code null} for nobody) and the whole list of its
 * locations.
 */
public record LabelRequestDto(
        @Nullable String name,
        @Nullable LabelOwnerRequestDto owner,
        @Nullable List<LocationDto> locations) {

    public record LocationDto(
            @Nullable UUID country,
            @Nullable UUID city,
            @Nullable Boolean primary) {}
}
