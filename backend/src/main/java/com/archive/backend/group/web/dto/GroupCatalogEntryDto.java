package com.archive.backend.group.web.dto;

import org.jspecify.annotations.Nullable;

import java.util.UUID;

public record GroupCatalogEntryDto(UUID groupId, String name, @Nullable String groupName) {
}
