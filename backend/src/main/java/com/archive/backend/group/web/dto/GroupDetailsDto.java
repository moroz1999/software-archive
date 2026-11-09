package com.archive.backend.group.web.dto;

import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public record GroupDetailsDto(
        UUID id,
        String name,
        String groupName,
        @Nullable String notes,
        List<String> types,
        List<LabelDto> labels,
        List<LocationDto> locations,
        List<MemberDto> members,
        List<RelatedGroupDto> memberGroups,
        List<RelatedGroupDto> groups) {

    public record LabelDto(UUID id, String name) {}

    public record LocationDto(
            @Nullable String country, @Nullable String city, boolean primary) {}

    public record MemberDto(
            String name, @Nullable UUID personId, @Nullable String personName) {}

    public record RelatedGroupDto(UUID id, String name) {}
}
