package com.archive.backend.geo.web.dto;

import java.util.List;
import java.util.UUID;

public record GeographyCountryDto(UUID id, String name, int places, List<GeographyCityDto> cities) {}
