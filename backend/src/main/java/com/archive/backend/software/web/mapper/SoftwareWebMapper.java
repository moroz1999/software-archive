package com.archive.backend.software.web.mapper;

import com.archive.backend.author.domain.PersonReference;
import com.archive.backend.author.domain.PersonReferenceId;
import com.archive.backend.author.domain.PersonReferenceType;
import com.archive.backend.category.domain.CategoryId;
import com.archive.backend.hardware.domain.HardwareCode;
import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.shared.domain.error.InvalidValueException;
import com.archive.backend.software.app.CreateSoftwareCommand;
import com.archive.backend.software.app.ListLatestSoftwareQuery;
import com.archive.backend.software.app.UpdateSoftwareCommand;
import com.archive.backend.software.domain.SoftwareCreditDraft;
import com.archive.backend.software.domain.SoftwareDescription;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.domain.SoftwareReference;
import com.archive.backend.software.domain.SoftwareReferenceId;
import com.archive.backend.software.domain.SoftwareReferenceType;
import com.archive.backend.software.domain.SoftwareTitle;
import com.archive.backend.software.read.SoftwareDetails;
import com.archive.backend.software.read.SoftwareRelease;
import com.archive.backend.software.read.SoftwareSummary;
import com.archive.backend.software.web.dto.SoftwareCreatedDto;
import com.archive.backend.software.web.dto.SoftwareDetailsDto;
import com.archive.backend.software.web.dto.SoftwareListItemDto;
import com.archive.backend.software.web.dto.SoftwareReferenceDto;
import com.archive.backend.software.web.dto.SoftwareReferenceRequestDto;
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

    SoftwareReferenceDto toReference(com.archive.backend.software.read.SoftwareReferenceOption reference);

    List<SoftwareReleaseDto> toReleases(List<SoftwareRelease> releases);

    default SoftwareCreatedDto toCreated(SoftwareId id) {
        return new SoftwareCreatedDto(id.value());
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
                toCategories(request.categories()),
                toReferences(request.publishers()),
                toDeveloperReferences(request.developers()),
                toCredits(request.credits()));
    }

    default UpdateSoftwareCommand toUpdate(UUID id, SoftwareRequestDto request) {
        return new UpdateSoftwareCommand(
                new SoftwareId(id),
                toTitle(request.title()),
                toDescription(request.description()),
                request.releaseDate(),
                toPlatform(request.platform()),
                toHardware(request.hardware()),
                toCategories(request.categories()),
                toReferences(request.publishers()),
                toDeveloperReferences(request.developers()),
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

    private static Set<SoftwareReference> toReferences(@Nullable List<SoftwareReferenceRequestDto> references) {
        return references == null
                ? Set.of()
                : references.stream().map(SoftwareWebMapper::toReference).collect(Collectors.toUnmodifiableSet());
    }

    private static Set<SoftwareReference> toDeveloperReferences(
            @Nullable List<SoftwareReferenceRequestDto> references) {
        Set<SoftwareReference> mapped = toReferences(references);
        if (mapped.stream().anyMatch(reference -> !reference.type().isGroup())) {
            throw new InvalidValueException(
                    "software.reference-type-invalid", "A developer company must be a group or a group label.");
        }
        return mapped;
    }

    private static SoftwareReference toReference(SoftwareReferenceRequestDto reference) {
        if (reference.id() == null) {
            throw new InvalidValueException("software.reference-id-required", "A software reference id is required.");
        }
        if (reference.type() == null) {
            throw new InvalidValueException(
                    "software.reference-type-invalid", "A software reference type is required.");
        }
        return new SoftwareReference(
                SoftwareReferenceType.fromCode(reference.type()), new SoftwareReferenceId(reference.id()));
    }

    private static List<SoftwareCreditDraft> toCredits(@Nullable List<SoftwareRequestDto.CreditDto> credits) {
        return credits == null
                ? List.of()
                : credits.stream().map(SoftwareWebMapper::toCredit).toList();
    }

    private static SoftwareCreditDraft toCredit(SoftwareRequestDto.CreditDto credit) {
        UUID id = credit.id();
        if (id == null) {
            throw new InvalidValueException("person.reference-id-required", "A person reference id is required.");
        }
        String type = credit.type();
        if (type == null) {
            throw new InvalidValueException("person.reference-type-invalid", "A person reference type is required.");
        }
        return new SoftwareCreditDraft(
                new PersonReference(PersonReferenceType.fromCode(type), new PersonReferenceId(id)),
                credit.roles() == null ? List.of() : credit.roles());
    }

    private static @Nullable PlatformCode toPlatformFilter(@Nullable String value) {
        return value == null ? null : new PlatformCode(value);
    }
}
