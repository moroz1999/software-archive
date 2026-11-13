package com.archive.backend.group.web.dto;

import java.time.LocalDate;
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
        List<RelatedGroupDto> groups,
        boolean canChange) {

    public record LabelDto(UUID id, String name, boolean display) {}

    public record LocationDto(
            @Nullable UUID countryId,
            @Nullable String country,
            @Nullable UUID cityId,
            @Nullable String city,
            boolean primary) {}

    public record MemberDto(
            @Nullable UUID id,
            String type,
            String title,
            String name,
            @Nullable UUID personId,
            @Nullable String personName,
            List<String> roles,
            @Nullable LocalDate startDate,
            @Nullable LocalDate endDate) {}

    public record RelatedGroupDto(UUID id, String name) {}
}
