package com.archive.backend.software.web.mapper;

import com.archive.backend.hardware.domain.HardwareCode;
import com.archive.backend.shared.domain.Language;
import com.archive.backend.shared.domain.error.InvalidValueException;
import com.archive.backend.software.app.CreateReleaseCommand;
import com.archive.backend.software.app.DeleteReleaseCommand;
import com.archive.backend.software.app.GetSoftwareReleaseQuery;
import com.archive.backend.software.app.UpdateReleaseCommand;
import com.archive.backend.software.domain.ReleaseArchiveRemoval;
import com.archive.backend.software.domain.ReleaseArchiveUpload;
import com.archive.backend.software.domain.ReleaseContent;
import com.archive.backend.software.domain.ReleaseDate;
import com.archive.backend.software.domain.ReleaseDescription;
import com.archive.backend.software.domain.ReleaseId;
import com.archive.backend.software.domain.ReleaseTitle;
import com.archive.backend.software.domain.ReleaseType;
import com.archive.backend.software.domain.ReleaseVersion;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.read.ReleaseArchiveDetails;
import com.archive.backend.software.read.ReleaseDetails;
import com.archive.backend.software.read.ReleaseFileItem;
import com.archive.backend.software.read.ReleaseFiles;
import com.archive.backend.software.read.SoftwareHardware;
import com.archive.backend.software.web.dto.ReleaseArchiveDto;
import com.archive.backend.software.web.dto.ReleaseCreatedDto;
import com.archive.backend.software.web.dto.ReleaseDetailsDto;
import com.archive.backend.software.web.dto.ReleaseFileDto;
import com.archive.backend.software.web.dto.ReleaseFilesDto;
import com.archive.backend.software.web.dto.ReleaseRequestDto;
import com.archive.backend.user.domain.Actor;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.springframework.web.multipart.MultipartFile;

@Mapper(componentModel = "spring")
public interface ReleaseWebMapper {

    List<com.archive.backend.software.web.dto.SoftwareHardwareDto> toHardwareDtos(List<SoftwareHardware> hardware);

    ReleaseFileDto toFileDto(ReleaseFileItem file);

    ReleaseFilesDto toFilesDto(ReleaseFiles files);

    default ReleaseDetailsDto toDetails(UUID softwareId, ReleaseDetails details) {
        return new ReleaseDetailsDto(
                details.id(),
                details.title(),
                details.version(),
                details.type(),
                details.releaseDate(),
                details.description(),
                toHardwareDtos(details.hardware()),
                toArchive(softwareId, details.id(), details.archive()));
    }

    default ReleaseCreatedDto toCreated(ReleaseId id) {
        return new ReleaseCreatedDto(id.value());
    }

    default GetSoftwareReleaseQuery toGet(UUID softwareId, UUID releaseId, Language language) {
        return new GetSoftwareReleaseQuery(new SoftwareId(softwareId), new ReleaseId(releaseId), language);
    }

    default CreateReleaseCommand toCreate(Actor actor, UUID softwareId, ReleaseRequestDto request) {
        return toCreate(actor, softwareId, request, null);
    }

    default CreateReleaseCommand toCreate(
            Actor actor, UUID softwareId, ReleaseRequestDto request, @Nullable MultipartFile archive) {
        return new CreateReleaseCommand(
                actor,
                new SoftwareId(softwareId),
                toContent(request),
                toHardware(request.hardware()),
                toArchiveUpload(archive));
    }

    default UpdateReleaseCommand toUpdate(Actor actor, UUID softwareId, UUID releaseId, ReleaseRequestDto request) {
        return toUpdate(actor, softwareId, releaseId, request, null, false);
    }

    default UpdateReleaseCommand toUpdate(
            Actor actor,
            UUID softwareId,
            UUID releaseId,
            ReleaseRequestDto request,
            @Nullable MultipartFile archive,
            boolean removeArchive) {
        return new UpdateReleaseCommand(
                actor,
                new SoftwareId(softwareId),
                new ReleaseId(releaseId),
                toContent(request),
                toHardware(request.hardware()),
                toArchiveUpload(archive),
                new ReleaseArchiveRemoval(removeArchive));
    }

    default DeleteReleaseCommand toDelete(Actor actor, UUID softwareId, UUID releaseId) {
        return new DeleteReleaseCommand(actor, new SoftwareId(softwareId), new ReleaseId(releaseId));
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
                toReleaseDate(request.releaseDate()),
                description == null ? null : new ReleaseDescription(description));
    }

    private static @Nullable ReleaseDate toReleaseDate(@Nullable String date) {
        return date == null ? null : ReleaseDate.parse(date);
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

    private static @Nullable ReleaseArchiveUpload toArchiveUpload(@Nullable MultipartFile archive) {
        if (archive == null) {
            return null;
        }
        String name = archive.getOriginalFilename();
        return new ReleaseArchiveUpload(name == null ? "" : name, archive.getSize(), archive::getInputStream);
    }

    private static @Nullable ReleaseArchiveDto toArchive(
            UUID softwareId, UUID releaseId, @Nullable ReleaseArchiveDetails archive) {
        return archive == null
                ? null
                : new ReleaseArchiveDto(
                        archive.fileName(),
                        archive.size(),
                        "/api/software/" + softwareId + "/releases/" + releaseId + "/archive");
    }
}
