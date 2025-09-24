package com.archive.backend.software.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record SoftwareListItemDto(
        UUID id,
        String title,
        LocalDateTime releaseDate
) {
}
