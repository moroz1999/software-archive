package com.archive.backend.group.web.mapper;

import com.archive.backend.geo.domain.CityId;
import com.archive.backend.geo.domain.CountryId;
import com.archive.backend.geo.domain.LocationDraft;
import com.archive.backend.group.app.CreateGroupCommand;
import com.archive.backend.group.app.ListGroupsQuery;
import com.archive.backend.group.app.UpdateGroupCommand;
import com.archive.backend.group.domain.GroupId;
import com.archive.backend.group.domain.GroupName;
import com.archive.backend.group.domain.GroupNotes;
import com.archive.backend.group.domain.GroupTypeCode;
import com.archive.backend.group.read.GroupCatalogEntry;
import com.archive.backend.group.read.GroupDetails;
import com.archive.backend.group.web.dto.GroupCatalogEntryDto;
import com.archive.backend.group.web.dto.GroupCreatedDto;
import com.archive.backend.group.web.dto.GroupDetailsDto;
import com.archive.backend.group.web.dto.GroupRequestDto;
import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.label.domain.LabelDraft;
import com.archive.backend.label.domain.LabelId;
import com.archive.backend.label.domain.LabelLetter;
import com.archive.backend.label.domain.LabelName;
import com.archive.backend.shared.domain.error.InvalidValueException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Pageable;

@Mapper(componentModel = "spring")
public interface GroupWebMapper {

    GroupCatalogEntryDto toCatalogEntry(GroupCatalogEntry entry);

    GroupDetailsDto toDetails(GroupDetails details);

    default GroupCreatedDto toCreated(GroupId id) {
        return new GroupCreatedDto(id.value());
    }

    default CreateGroupCommand toCreate(GroupRequestDto request) {
        return new CreateGroupCommand(
                toName(request.name()),
                toNotes(request.notes()),
                toTypes(request.types()),
                toLabels(request.labels()),
                toLocations(request.locations()));
    }

    default UpdateGroupCommand toUpdate(UUID id, GroupRequestDto request) {
        return new UpdateGroupCommand(
                new GroupId(id),
                toName(request.name()),
                toNotes(request.notes()),
                toTypes(request.types()),
                toLabels(request.labels()),
                toLocations(request.locations()));
    }

    // Request scalars -> value objects.

    private static GroupName toName(@Nullable String value) {
        if (value == null) {
            throw new InvalidValueException("title.required", "A group needs a name.");
        }
        return new GroupName(value);
    }

    private static @Nullable GroupNotes toNotes(@Nullable String value) {
        return value == null ? null : new GroupNotes(value);
    }

    private static Set<GroupTypeCode> toTypes(@Nullable List<String> types) {
        return types == null
                ? Set.of()
                : types.stream().map(GroupTypeCode::new).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static List<LabelDraft> toLabels(@Nullable List<GroupRequestDto.LabelDto> labels) {
        return labels == null
                ? List.of()
                : labels.stream().map(GroupWebMapper::toLabel).toList();
    }

    private static LabelDraft toLabel(GroupRequestDto.LabelDto label) {
        String name = label.name();
        if (name == null) {
            throw new InvalidValueException("title.required", "A label needs a name.");
        }
        UUID id = label.id();
        return new LabelDraft(
                id == null ? null : new LabelId(id), new LabelName(name), Boolean.TRUE.equals(label.display()));
    }

    private static List<LocationDraft> toLocations(@Nullable List<GroupRequestDto.LocationDto> locations) {
        return locations == null
                ? List.of()
                : locations.stream().map(GroupWebMapper::toLocation).toList();
    }

    private static LocationDraft toLocation(GroupRequestDto.LocationDto location) {
        UUID country = location.country();
        UUID city = location.city();
        return new LocationDraft(
                country == null ? null : new CountryId(country),
                city == null ? null : new CityId(city),
                Boolean.TRUE.equals(location.primary()));
    }

    default ListGroupsQuery toListQuery(
            @Nullable String letter,
            @Nullable UUID country,
            @Nullable UUID city,
            @Nullable String platform,
            Pageable pageable) {
        return new ListGroupsQuery(
                letter == null ? null : new LabelLetter(letter),
                country == null ? null : new CountryId(country),
                city == null ? null : new CityId(city),
                platform == null ? null : new PlatformCode(platform),
                pageable);
    }
}
