package com.archive.backend.category.web.dto;

import java.util.Map;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public record CategoryDetailsDto(
        UUID id, Map<String, String> titles, @Nullable UUID parentId) {}
