package com.archive.backend.software.web.dto;

import java.time.LocalDate;
import java.util.UUID;

public record SoftwareListItemDto(UUID id, String title, LocalDate releaseDate) {}
