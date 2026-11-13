package com.archive.backend.user.web.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UserEntryDto(
        UUID id, String name, String email, boolean confirmed, Instant registeredAt, List<String> groups) {}
