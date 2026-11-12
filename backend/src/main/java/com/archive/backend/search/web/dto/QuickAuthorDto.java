package com.archive.backend.search.web.dto;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

public record QuickAuthorDto(UUID id, String name, @Nullable String realName) {}
