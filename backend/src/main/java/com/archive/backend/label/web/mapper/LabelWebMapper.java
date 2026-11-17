package com.archive.backend.label.web.mapper;

import com.archive.backend.geo.domain.CityId;
import com.archive.backend.geo.domain.CountryId;
import com.archive.backend.geo.domain.LocationDraft;
import com.archive.backend.label.app.CreateLabelCommand;
import com.archive.backend.label.app.EditLabelCommand;
import com.archive.backend.label.domain.LabelId;
import com.archive.backend.label.domain.LabelName;
import com.archive.backend.label.domain.LabelOwnerChoice;
import com.archive.backend.label.domain.LabelOwnerKind;
import com.archive.backend.label.read.LabelDetails;
import com.archive.backend.label.web.dto.LabelDetailsDto;
import com.archive.backend.label.web.dto.LabelOwnerRequestDto;
import com.archive.backend.label.web.dto.LabelRequestDto;
import com.archive.backend.label.web.dto.NewLabelRequestDto;
import com.archive.backend.shared.domain.error.InvalidValueException;
import com.archive.backend.user.domain.Actor;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LabelWebMapper {

    default LabelDetailsDto toDetails(LabelDetails label) {
        LabelDetails.Owner owner = label.owner();
        return new LabelDetailsDto(
                label.id(),
                label.name(),
                owner == null
                        ? null
                        : new LabelDetailsDto.OwnerDto(owner.type(), owner.id(), owner.name(), owner.note()),
                label.groups().stream()
                        .map(group -> new LabelDetailsDto.MemberOfDto(group.id(), group.name()))
                        .toList(),
                label.locations().stream()
                        .map(location -> new LabelDetailsDto.LocationDto(
                                location.countryId(),
                                location.country(),
                                location.cityId(),
                                location.city(),
                                location.primary()))
                        .toList(),
                label.origins().stream()
                        .map(origin ->
                                new LabelDetailsDto.OriginDto(origin.origin(), origin.externalId(), origin.url()))
                        .toList(),
                new LabelDetailsDto.AddedDto(label.added().by(), label.added().at()),
                label.rights().canChange(),
                label.rights().canMakeOwner(),
                label.rights().canEdit(),
                label.rights().canChangeOwner());
    }

    default CreateLabelCommand toCreate(Actor actor, NewLabelRequestDto request) {
        String name = request.name();
        if (name == null) {
            throw new InvalidValueException("title.required", "A label needs a name.");
        }
        return new CreateLabelCommand(actor, new LabelName(name), toLocations(request.locations()));
    }

    default EditLabelCommand toEdit(Actor actor, UUID id, LabelRequestDto request) {
        String name = request.name();
        if (name == null) {
            throw new InvalidValueException("title.required", "A label needs a name.");
        }
        return new EditLabelCommand(
                actor,
                new LabelId(id),
                new LabelName(name),
                toOwnerOrNobody(request.owner()),
                toLocations(request.locations()));
    }

    /** The owner a label is attached to: {@code author} or {@code group}, by its id. */
    private static LabelOwnerChoice toOwner(LabelOwnerRequestDto owner) {
        UUID id = owner.id();
        if (id == null) {
            throw new InvalidValueException("label.owner-required", "The owner of the label is required.");
        }
        LabelOwnerKind kind = switch (String.valueOf(owner.type())) {
            case "author" -> LabelOwnerKind.AUTHOR;
            case "group" -> LabelOwnerKind.GROUP;
            default ->
                throw new InvalidValueException("label.owner-type-invalid", "A label belongs to a person or a group.");
        };
        return new LabelOwnerChoice(kind, id, Boolean.TRUE.equals(owner.display()));
    }

    private static @Nullable LabelOwnerChoice toOwnerOrNobody(@Nullable LabelOwnerRequestDto owner) {
        return owner == null ? null : toOwner(owner);
    }

    private static List<LocationDraft> toLocations(@Nullable List<LabelRequestDto.LocationDto> locations) {
        return locations == null
                ? List.of()
                : locations.stream()
                        .map(location -> {
                            UUID country = location.country();
                            UUID city = location.city();
                            return new LocationDraft(
                                    country == null ? null : new CountryId(country),
                                    city == null ? null : new CityId(city),
                                    Boolean.TRUE.equals(location.primary()));
                        })
                        .toList();
    }
}
