package com.archive.backend.group.web.dto;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

public record GroupCatalogEntryDto(
        UUID groupId, String name, @Nullable String groupName) {}
