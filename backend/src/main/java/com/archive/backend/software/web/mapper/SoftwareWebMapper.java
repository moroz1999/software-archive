package com.archive.backend.software.web.mapper;

import com.archive.backend.category.domain.CategoryId;
import com.archive.backend.hardware.domain.HardwareCode;
import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.shared.domain.error.InvalidValueException;
import com.archive.backend.software.app.CreateSoftwareCommand;
import com.archive.backend.software.app.ListLatestSoftwareQuery;
import com.archive.backend.software.app.ListSoftwareQuery;
import com.archive.backend.software.app.UpdateSoftwareCommand;
import com.archive.backend.software.domain.SoftwareDescription;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.domain.SoftwareTitle;
import com.archive.backend.software.read.SoftwareDetails;
import com.archive.backend.software.read.SoftwareRelease;
import com.archive.backend.software.read.SoftwareSummary;
import com.archive.backend.software.web.dto.SoftwareCreatedDto;
import com.archive.backend.software.web.dto.SoftwareDetailsDto;
import com.archive.backend.software.web.dto.SoftwareListItemDto;
import com.archive.backend.software.web.dto.SoftwareReleaseDto;
import com.archive.backend.software.web.dto.SoftwareRequestDto;
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

    SoftwareDetailsDto toDetails(SoftwareDetails details);

    List<SoftwareReleaseDto> toReleases(List<SoftwareRelease> releases);

    default SoftwareCreatedDto toCreated(SoftwareId id) {
        return new SoftwareCreatedDto(id.value());
    }

    default ListSoftwareQuery toListQuery(@Nullable String platform, Pageable pageable) {
        return new ListSoftwareQuery(toPlatformFilter(platform), pageable);
    }

    default ListLatestSoftwareQuery toLatestQuery(@Nullable String platform, Pageable pageable) {
        return new ListLatestSoftwareQuery(toPlatformFilter(platform), pageable);
    }

    default CreateSoftwareCommand toCreate(SoftwareRequestDto request) {
        return new CreateSoftwareCommand(
                toTitle(request.title()),
                toDescription(request.description()),
                request.releaseDate(),
                toPlatform(request.platform()),
                toHardware(request.hardware()),
                toCategories(request.categories()));
    }

    default UpdateSoftwareCommand toUpdate(UUID id, SoftwareRequestDto request) {
        return new UpdateSoftwareCommand(
                new SoftwareId(id),
                toTitle(request.title()),
                toDescription(request.description()),
                request.releaseDate(),
                toPlatform(request.platform()),
                toHardware(request.hardware()),
                toCategories(request.categories()));
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

    private static @Nullable PlatformCode toPlatformFilter(@Nullable String value) {
        return value == null ? null : new PlatformCode(value);
    }
}
