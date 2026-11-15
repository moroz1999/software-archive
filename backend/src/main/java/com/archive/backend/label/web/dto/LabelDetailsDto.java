package com.archive.backend.label.web.dto;

import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public record LabelDetailsDto(
        UUID id, String name, @Nullable OwnerDto owner, List<MemberOfDto> groups, boolean canChange) {

    public record OwnerDto(String type, UUID id) {}

    public record MemberOfDto(UUID id, String name) {}
}
