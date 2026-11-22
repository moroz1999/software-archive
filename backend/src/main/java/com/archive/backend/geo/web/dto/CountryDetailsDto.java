package com.archive.backend.geo.web.dto;

import java.util.Map;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public record CountryDetailsDto(
        UUID id,
        Map<String, String> names,
        @Nullable Double latitude,
        @Nullable Double longitude) {}
