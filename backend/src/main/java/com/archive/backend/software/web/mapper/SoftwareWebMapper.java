package com.archive.backend.software.web.mapper;

import com.archive.backend.shared.domain.error.InvalidValueException;
import com.archive.backend.software.app.CreateSoftwareCommand;
import com.archive.backend.software.app.UpdateSoftwareCommand;
import com.archive.backend.software.domain.SoftwareDescription;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.domain.SoftwareTitle;
import com.archive.backend.software.read.SoftwareDetails;
import com.archive.backend.software.read.SoftwareSummary;
import com.archive.backend.software.web.dto.SoftwareCreatedDto;
import com.archive.backend.software.web.dto.SoftwareDetailsDto;
import com.archive.backend.software.web.dto.SoftwareListItemDto;
import com.archive.backend.software.web.dto.SoftwareRequestDto;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SoftwareWebMapper {

    SoftwareListItemDto toListItem(SoftwareSummary summary);

    SoftwareDetailsDto toDetails(SoftwareDetails details);

    default SoftwareCreatedDto toCreated(SoftwareId id) {
        return new SoftwareCreatedDto(id.value());
    }

    default CreateSoftwareCommand toCreate(SoftwareRequestDto request) {
        return new CreateSoftwareCommand(
                toTitle(request.title()), toDescription(request.description()), request.releaseDate());
    }

    default UpdateSoftwareCommand toUpdate(UUID id, SoftwareRequestDto request) {
        return new UpdateSoftwareCommand(
                new SoftwareId(id),
                toTitle(request.title()),
                toDescription(request.description()),
                request.releaseDate());
    }

    // Request scalars -> value objects.

    private static SoftwareTitle toTitle(@Nullable String value) {
        if (value == null) {
            throw new InvalidValueException("title.required", "A title is required.");
        }
        return new SoftwareTitle(value);
    }

    private static @Nullable SoftwareDescription toDescription(@Nullable String value) {
        return value == null ? null : new SoftwareDescription(value);
    }
}
