package com.archive.backend.category.web.dto;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

public record CategoryDetailsDto(
        UUID id, String title, @Nullable UUID parentId) {}
