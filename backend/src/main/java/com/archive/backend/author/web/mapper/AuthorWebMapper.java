package com.archive.backend.author.web.mapper;

import com.archive.backend.author.app.CreateAuthorCommand;
import com.archive.backend.author.app.ListAuthorsQuery;
import com.archive.backend.author.app.UpdateAuthorCommand;
import com.archive.backend.author.domain.AuthorId;
import com.archive.backend.author.domain.AuthorNotes;
import com.archive.backend.author.domain.RealName;
import com.archive.backend.author.read.AuthorCatalogEntry;
import com.archive.backend.author.read.AuthorDetails;
import com.archive.backend.author.web.dto.AuthorCatalogEntryDto;
import com.archive.backend.author.web.dto.AuthorCreatedDto;
import com.archive.backend.author.web.dto.AuthorDetailsDto;
import com.archive.backend.author.web.dto.AuthorRequestDto;
import com.archive.backend.geo.domain.CityId;
import com.archive.backend.geo.domain.CountryId;
import com.archive.backend.geo.domain.LocationDraft;
import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.label.domain.LabelDraft;
import com.archive.backend.label.domain.LabelId;
import com.archive.backend.label.domain.LabelLetter;
import com.archive.backend.label.domain.LabelName;
import com.archive.backend.shared.domain.error.InvalidValueException;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Pageable;

@Mapper(componentModel = "spring")
public interface AuthorWebMapper {

    AuthorCatalogEntryDto toCatalogEntry(AuthorCatalogEntry entry);

    AuthorDetailsDto toDetails(AuthorDetails details);

    default AuthorCreatedDto toCreated(AuthorId id) {
        return new AuthorCreatedDto(id.value());
    }

    default CreateAuthorCommand toCreate(AuthorRequestDto request) {
        return new CreateAuthorCommand(
                toRealName(request.realName()),
                toNotes(request.notes()),
                toLabels(request.labels()),
                toLocations(request.locations()));
    }

    default UpdateAuthorCommand toUpdate(UUID id, AuthorRequestDto request) {
        return new UpdateAuthorCommand(
                new AuthorId(id),
                toRealName(request.realName()),
                toNotes(request.notes()),
                toLabels(request.labels()),
                toLocations(request.locations()));
    }

    // Request scalars -> value objects.

    private static @Nullable RealName toRealName(@Nullable String value) {
        return value == null ? null : new RealName(value);
    }

    private static @Nullable AuthorNotes toNotes(@Nullable String value) {
        return value == null ? null : new AuthorNotes(value);
    }

    private static List<LabelDraft> toLabels(@Nullable List<AuthorRequestDto.LabelDto> labels) {
        return labels == null
                ? List.of()
                : labels.stream().map(AuthorWebMapper::toLabel).toList();
    }

    private static LabelDraft toLabel(AuthorRequestDto.LabelDto label) {
        String name = label.name();
        if (name == null) {
            throw new InvalidValueException("title.required", "A label needs a name.");
        }
        UUID id = label.id();
        return new LabelDraft(
                id == null ? null : new LabelId(id), new LabelName(name), Boolean.TRUE.equals(label.display()));
    }

    private static List<LocationDraft> toLocations(@Nullable List<AuthorRequestDto.LocationDto> locations) {
        return locations == null
                ? List.of()
                : locations.stream().map(AuthorWebMapper::toLocation).toList();
    }

    private static LocationDraft toLocation(AuthorRequestDto.LocationDto location) {
        UUID country = location.country();
        UUID city = location.city();
        return new LocationDraft(
                country == null ? null : new CountryId(country),
                city == null ? null : new CityId(city),
                Boolean.TRUE.equals(location.primary()));
    }

    default ListAuthorsQuery toListQuery(
            @Nullable String letter,
            @Nullable UUID country,
            @Nullable UUID city,
            @Nullable String platform,
            Pageable pageable) {
        return new ListAuthorsQuery(
                letter == null ? null : new LabelLetter(letter),
                country == null ? null : new CountryId(country),
                city == null ? null : new CityId(city),
                platform == null ? null : new PlatformCode(platform),
                pageable);
    }
}
