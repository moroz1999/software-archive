package com.archive.backend.software.web.mapper;

import com.archive.backend.category.domain.CategoryId;
import com.archive.backend.hardware.domain.HardwareCode;
import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.shared.domain.error.InvalidValueException;
import com.archive.backend.software.app.CreateSoftwareCommand;
import com.archive.backend.software.app.ListLatestSoftwareQuery;
import com.archive.backend.software.app.UpdateSoftwareCommand;
import com.archive.backend.software.domain.ReleaseDate;
import com.archive.backend.software.domain.SoftwareCreditDraft;
import com.archive.backend.software.domain.SoftwareDescription;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.domain.SoftwareReference;
import com.archive.backend.software.domain.SoftwareReferenceId;
import com.archive.backend.software.domain.SoftwareReferenceType;
import com.archive.backend.software.domain.SoftwareTitle;
import com.archive.backend.software.read.CreditedSoftware;
import com.archive.backend.software.read.ReleaseArchiveDetails;
import com.archive.backend.software.read.SoftwareDetails;
import com.archive.backend.software.read.SoftwareRelease;
import com.archive.backend.software.read.SoftwareScreenshot;
import com.archive.backend.software.read.SoftwareSummary;
import com.archive.backend.software.web.dto.CreditedSoftwareDto;
import com.archive.backend.software.web.dto.ReleaseArchiveDto;
import com.archive.backend.software.web.dto.ScreenshotDto;
import com.archive.backend.software.web.dto.SoftwareCreatedDto;
import com.archive.backend.software.web.dto.SoftwareDetailsDto;
import com.archive.backend.software.web.dto.SoftwareListItemDto;
import com.archive.backend.software.web.dto.SoftwareReleaseDto;
import com.archive.backend.software.web.dto.SoftwareRequestDto;
import com.archive.backend.user.domain.Actor;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Pageable;

@Mapper(componentModel = "spring")
public interface SoftwareWebMapper {

    SoftwareListItemDto toListItem(SoftwareSummary summary);

    List<CreditedSoftwareDto> toCredited(List<CreditedSoftware> software);

    SoftwareDetailsDto toDetails(SoftwareDetails details);

    List<com.archive.backend.software.web.dto.SoftwareHardwareDto> toHardwareDtos(
            List<com.archive.backend.software.read.SoftwareHardware> hardware);

    default List<SoftwareReleaseDto> toReleases(UUID softwareId, List<SoftwareRelease> releases) {
        return releases.stream().map(release -> toRelease(softwareId, release)).toList();
    }

    default SoftwareReleaseDto toRelease(UUID softwareId, SoftwareRelease release) {
        return new SoftwareReleaseDto(
                release.id(),
                release.title(),
                release.version(),
                release.type(),
                release.releaseDate(),
                release.description(),
                toHardwareDtos(release.hardware()),
                release.screenshots().stream().map(this::toScreenshot).toList(),
                toArchive(softwareId, release.id(), release.archive()),
                new SoftwareReleaseDto.AddedDto(
                        release.added().by(), release.added().at()),
                release.canChange());
    }

    default ScreenshotDto toScreenshot(SoftwareScreenshot screenshot) {
        return new ScreenshotDto(screenshot.id(), ScreenshotWebMapper.url(screenshot.id()));
    }

    private static @Nullable ReleaseDate toReleaseDate(@Nullable String date) {
        return date == null ? null : ReleaseDate.parse(date);
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

    default SoftwareCreatedDto toCreated(SoftwareId id) {
        return new SoftwareCreatedDto(id.value());
    }

    default ListLatestSoftwareQuery toLatestQuery(@Nullable String platform, Pageable pageable) {
        return new ListLatestSoftwareQuery(toPlatformFilter(platform), pageable);
    }

    default CreateSoftwareCommand toCreate(Actor actor, SoftwareRequestDto request) {
        return new CreateSoftwareCommand(
                actor,
                toTitle(request.title()),
                toDescription(request.description()),
                toReleaseDate(request.releaseDate()),
                toPlatform(request.platform()),
                toHardware(request.hardware()),
                toCategories(request.categories()),
                toCredits(request.credits()));
    }

    default UpdateSoftwareCommand toUpdate(Actor actor, UUID id, SoftwareRequestDto request) {
        return new UpdateSoftwareCommand(
                actor,
                new SoftwareId(id),
                toTitle(request.title()),
                toDescription(request.description()),
                toReleaseDate(request.releaseDate()),
                toPlatform(request.platform()),
                toHardware(request.hardware()),
                toCategories(request.categories()),
                toCredits(request.credits()));
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

    private static PlatformCode toPlatform(@Nullable String value) {
        if (value == null) {
            throw new InvalidValueException("platform.required", "A platform is required.");
        }
        return new PlatformCode(value);
    }

    private static Set<HardwareCode> toHardware(@Nullable List<String> codes) {
        return codes == null ? Set.of() : codes.stream().map(HardwareCode::new).collect(Collectors.toUnmodifiableSet());
    }

    private static Set<CategoryId> toCategories(@Nullable List<UUID> ids) {
        return ids == null ? Set.of() : ids.stream().map(CategoryId::new).collect(Collectors.toUnmodifiableSet());
    }

    private static List<SoftwareCreditDraft> toCredits(@Nullable List<SoftwareRequestDto.CreditDto> credits) {
        return credits == null
                ? List.of()
                : credits.stream().map(SoftwareWebMapper::toCredit).toList();
    }

    private static SoftwareCreditDraft toCredit(SoftwareRequestDto.CreditDto credit) {
        UUID id = credit.id();
        if (id == null) {
            throw new InvalidValueException("software.reference-id-required", "A credited id is required.");
        }
        String type = credit.type();
        if (type == null) {
            throw new InvalidValueException("software.reference-type-invalid", "A credited type is required.");
        }
        return new SoftwareCreditDraft(
                new SoftwareReference(SoftwareReferenceType.fromCode(type), new SoftwareReferenceId(id)),
                credit.roles() == null ? List.of() : credit.roles());
    }

    private static @Nullable PlatformCode toPlatformFilter(@Nullable String value) {
        return value == null ? null : new PlatformCode(value);
    }
}
