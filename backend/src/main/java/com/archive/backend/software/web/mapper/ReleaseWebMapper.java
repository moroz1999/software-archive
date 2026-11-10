package com.archive.backend.software.web.mapper;

import com.archive.backend.hardware.domain.HardwareCode;
import com.archive.backend.shared.domain.error.InvalidValueException;
import com.archive.backend.software.app.CreateReleaseCommand;
import com.archive.backend.software.app.DeleteReleaseCommand;
import com.archive.backend.software.app.GetSoftwareReleaseQuery;
import com.archive.backend.software.app.UpdateReleaseCommand;
import com.archive.backend.software.domain.ReleaseContent;
import com.archive.backend.software.domain.ReleaseDescription;
import com.archive.backend.software.domain.ReleaseId;
import com.archive.backend.software.domain.ReleaseTitle;
import com.archive.backend.software.domain.ReleaseType;
import com.archive.backend.software.domain.ReleaseVersion;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.read.ReleaseDetails;
import com.archive.backend.software.web.dto.ReleaseCreatedDto;
import com.archive.backend.software.web.dto.ReleaseDetailsDto;
import com.archive.backend.software.web.dto.ReleaseRequestDto;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReleaseWebMapper {

    ReleaseDetailsDto toDetails(ReleaseDetails details);

    default ReleaseCreatedDto toCreated(ReleaseId id) {
        return new ReleaseCreatedDto(id.value());
    }

    default GetSoftwareReleaseQuery toGet(UUID softwareId, UUID releaseId) {
        return new GetSoftwareReleaseQuery(new SoftwareId(softwareId), new ReleaseId(releaseId));
    }

    default CreateReleaseCommand toCreate(UUID softwareId, ReleaseRequestDto request) {
        return new CreateReleaseCommand(new SoftwareId(softwareId), toContent(request), toHardware(request.hardware()));
    }

    default UpdateReleaseCommand toUpdate(UUID softwareId, UUID releaseId, ReleaseRequestDto request) {
        return new UpdateReleaseCommand(
                new SoftwareId(softwareId),
                new ReleaseId(releaseId),
                toContent(request),
                toHardware(request.hardware()));
    }

    default DeleteReleaseCommand toDelete(UUID softwareId, UUID releaseId) {
        return new DeleteReleaseCommand(new SoftwareId(softwareId), new ReleaseId(releaseId));
    }

    // Request scalars -> value objects.
    private static ReleaseContent toContent(ReleaseRequestDto request) {
        String title = request.title();
        String version = request.version();
        String description = request.description();
        return new ReleaseContent(
                title == null ? null : new ReleaseTitle(title),
                version == null ? null : new ReleaseVersion(version),
                toType(request.type()),
                request.releaseDate(),
                description == null ? null : new ReleaseDescription(description));
    }

    private static Set<HardwareCode> toHardware(@Nullable List<String> hardware) {
        return hardware == null
                ? Set.of()
                : hardware.stream().map(HardwareCode::new).collect(Collectors.toSet());
    }

    private static ReleaseType toType(@Nullable String value) {
        if (value == null) {
            throw new InvalidValueException("release.type-required", "A release type is required.");
        }
        return ReleaseType.fromCode(value);
    }
}
