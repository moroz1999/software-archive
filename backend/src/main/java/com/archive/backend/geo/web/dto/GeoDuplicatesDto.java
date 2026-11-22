package com.archive.backend.geo.web.dto;

import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public record GeoDuplicatesDto(List<List<DuplicateDto>> countries, List<CityDuplicatesDto> cities) {

    public record DuplicateDto(
            UUID id, String name, @Nullable String country, int places) {}

    public record CityDuplicatesDto(@Nullable String country, List<DuplicateDto> cities) {}
}
