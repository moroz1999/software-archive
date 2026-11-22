package com.archive.backend.geo.web.dto;

import java.util.UUID;

public record GeographyCityDto(UUID id, String name, int places) {}
