package com.archive.backend.software.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class Software {
    private final UUID id;
    private String title;
    private String description;
    private final Instant createdAt;
    private LocalDateTime releaseDate;
}
