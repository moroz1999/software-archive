package com.archive.backend.software.infra;

import com.archive.backend.category.domain.CategoryId;
import com.archive.backend.category.domain.CategorySet;
import com.archive.backend.hardware.domain.HardwareCode;
import com.archive.backend.hardware.domain.HardwareSet;
import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.software.domain.Gallery;
import com.archive.backend.software.domain.PreservationStatus;
import com.archive.backend.software.domain.Release;
import com.archive.backend.software.domain.ReleaseArchive;
import com.archive.backend.software.domain.ReleaseArchiveId;
import com.archive.backend.software.domain.ReleaseContent;
import com.archive.backend.software.domain.ReleaseDate;
import com.archive.backend.software.domain.ReleaseDescription;
import com.archive.backend.software.domain.ReleaseId;
import com.archive.backend.software.domain.ReleaseTitle;
import com.archive.backend.software.domain.ReleaseType;
import com.archive.backend.software.domain.ReleaseVersion;
import com.archive.backend.software.domain.Screenshot;
import com.archive.backend.software.domain.ScreenshotFileName;
import com.archive.backend.software.domain.ScreenshotFormat;
import com.archive.backend.software.domain.ScreenshotId;
import com.archive.backend.software.domain.Screenshots;
import com.archive.backend.software.domain.Software;
import com.archive.backend.software.domain.SoftwareCompilationItems;
import com.archive.backend.software.domain.SoftwareCreditDraft;
import com.archive.backend.software.domain.SoftwareCredits;
import com.archive.backend.software.domain.SoftwareDescription;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.domain.SoftwareInstructions;
import com.archive.backend.software.domain.SoftwareReference;
import com.archive.backend.software.domain.SoftwareReferenceId;
import com.archive.backend.software.domain.SoftwareReferenceType;
import com.archive.backend.software.domain.SoftwareTitle;
import com.archive.backend.user.infra.AddedByRows;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;

/**
 * Software ↔ its JPA entity. Hardware sets are restored without the reference book: they were checked when stored, and
 * every set of the software is on its platform. The categories come restored by the repository, with their lines as
 * the tree has them now.
 */
@Mapper(componentModel = "spring")
public interface SoftwareMapper {

    default Software toDomain(SoftwareEntity entity, CategorySet categories) {
        SoftwareId id = new SoftwareId(entity.getId());
        PlatformCode platform = new PlatformCode(entity.getPlatformCode());
        String description = entity.getDescription();
        String instructions = entity.getInstructions();
        return new Software(
                id,
                new SoftwareTitle(entity.getTitle()),
                description == null ? null : new SoftwareDescription(description),
                toReleaseDate(entity.getReleaseYear(), entity.getReleaseDay()),
                toHardware(platform, entity.getHardware()),
                categories,
                toCredits(entity.getCredits()),
                entity.getReleases().stream()
                        .map(release -> toRelease(platform, release))
                        .toList(),
                AddedByRows.toAddedBy(entity.getAddedBy()),
                toGalleries(entity.getScreenshots()),
                PreservationStatus.fromCode(entity.getPreservation()),
                instructions == null ? null : new SoftwareInstructions(instructions),
                SoftwareCompilationItems.restore(
                        id,
                        entity.getCompilationItems().stream()
                                .map(SoftwareId::new)
                                .collect(Collectors.toSet())));
    }

    default SoftwareEntity toEntity(Software software) {
        SoftwareEntity entity = new SoftwareEntity();
        entity.setId(software.getId().value());
        entity.setTitle(software.getTitle().value());
        SoftwareDescription description = software.getDescription();
        entity.setDescription(description == null ? null : description.value());
        SoftwareInstructions instructions = software.getInstructions();
        entity.setInstructions(instructions == null ? null : instructions.value());
        entity.setPreservation(software.getPreservation().code());
        ReleaseDate releaseDate = software.getReleaseDate();
        entity.setReleaseYear(releaseDate == null ? null : releaseDate.year().getValue());
        entity.setReleaseDay(releaseDate == null ? null : releaseDate.day());
        entity.setPlatformCode(software.getPlatform().value());
        entity.setHardware(fromHardware(software.getHardware()));
        entity.setCategories(
                software.getCategories().ids().stream().map(CategoryId::value).collect(Collectors.toSet()));
        entity.setCredits(fromCredits(software.getCredits(), SoftwareCreditEntity::new));
        entity.setCompilationItems(software.getCompilationItems().ids().stream()
                .map(SoftwareId::value)
                .collect(Collectors.toSet()));
        entity.setAddedBy(AddedByRows.fromAddedBy(software.getAddedBy()));
        entity.setReleases(software.getReleases().stream()
                .map(SoftwareMapper::fromRelease)
                .collect(Collectors.toCollection(ArrayList::new)));
        entity.setScreenshots(fromGalleries(software));
        return entity;
    }

    /** The pictures of each gallery, in the order of the rows. */
    private static Map<Gallery, Screenshots> toGalleries(List<SoftwarePictureRow> rows) {
        Map<Gallery, List<Screenshot>> pictures = new EnumMap<>(Gallery.class);
        rows.forEach(row -> pictures.computeIfAbsent(Gallery.fromCode(row.getGallery()), gallery -> new ArrayList<>())
                .add(new Screenshot(
                        new ScreenshotId(row.getId()),
                        ScreenshotFormat.fromCode(row.getFormat()),
                        new ScreenshotFileName(row.getFileName()),
                        row.getSize())));
        Map<Gallery, Screenshots> galleries = new EnumMap<>(Gallery.class);
        pictures.forEach((gallery, list) -> galleries.put(gallery, new Screenshots(list)));
        return galleries;
    }

    /** The galleries one after another, each in its order. */
    private static List<SoftwarePictureRow> fromGalleries(Software software) {
        return Arrays.stream(Gallery.values())
                .flatMap(gallery -> software.pictures(gallery).all().stream()
                        .map(picture -> new SoftwarePictureRow(
                                picture.id().value(),
                                picture.format().code(),
                                picture.name().value(),
                                picture.size(),
                                gallery.code())))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static Screenshots toScreenshots(List<ScreenshotRow> rows) {
        return new Screenshots(rows.stream()
                .map(row -> new Screenshot(
                        new ScreenshotId(row.getId()),
                        ScreenshotFormat.fromCode(row.getFormat()),
                        new ScreenshotFileName(row.getFileName()),
                        row.getSize()))
                .toList());
    }

    private static List<ScreenshotRow> fromScreenshots(Screenshots screenshots) {
        return screenshots.all().stream()
                .map(screenshot -> new ScreenshotRow(
                        screenshot.id().value(),
                        screenshot.format().code(),
                        screenshot.name().value(),
                        screenshot.size()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static HardwareSet toHardware(PlatformCode platform, Set<String> codes) {
        return HardwareSet.restore(
                platform, codes.stream().map(HardwareCode::new).collect(Collectors.toSet()));
    }

    private static Set<String> fromHardware(HardwareSet hardware) {
        return hardware.codes().stream().map(HardwareCode::value).collect(Collectors.toSet());
    }

    private static @Nullable ReleaseDate toReleaseDate(@Nullable Integer year, @Nullable LocalDate day) {
        if (day != null) {
            return ReleaseDate.ofDay(day);
        }
        return year == null ? null : ReleaseDate.ofYear(Year.of(year));
    }

    private static Release toRelease(PlatformCode platform, SoftwareReleaseEntity entity) {
        String title = entity.getTitle();
        String version = entity.getVersion();
        String description = entity.getDescription();
        return new Release(
                new ReleaseId(entity.getId()),
                new ReleaseContent(
                        title == null ? null : new ReleaseTitle(title),
                        version == null ? null : new ReleaseVersion(version),
                        ReleaseType.fromCode(entity.getType()),
                        toReleaseDate(entity.getReleaseYear(), entity.getReleaseDay()),
                        description == null ? null : new ReleaseDescription(description)),
                toHardware(platform, entity.getHardware()),
                AddedByRows.toAddedBy(entity.getAddedBy()),
                toScreenshots(entity.getScreenshots()),
                toArchive(entity),
                toCredits(entity.getCredits()));
    }

    private static SoftwareReleaseEntity fromRelease(Release release) {
        ReleaseContent content = release.content();
        ReleaseTitle title = content.title();
        ReleaseVersion version = content.version();
        ReleaseDescription description = content.description();
        SoftwareReleaseEntity entity = new SoftwareReleaseEntity();
        entity.setId(release.id().value());
        entity.setTitle(title == null ? null : title.value());
        entity.setVersion(version == null ? null : version.value());
        entity.setType(content.type().code());
        ReleaseDate releaseDate = content.releaseDate();
        entity.setReleaseYear(releaseDate == null ? null : releaseDate.year().getValue());
        entity.setReleaseDay(releaseDate == null ? null : releaseDate.day());
        entity.setDescription(description == null ? null : description.value());
        entity.setHardware(fromHardware(release.hardware()));
        entity.setAddedBy(AddedByRows.fromAddedBy(release.addedBy()));
        entity.setScreenshots(fromScreenshots(release.screenshots()));
        entity.setCredits(fromCredits(release.credits(), SoftwareReleaseCreditEntity::new));
        ReleaseArchive archive = release.archive();
        if (archive != null) {
            entity.setArchiveId(archive.id().value());
            entity.setArchiveFileName(archive.fileName());
            entity.setArchiveSize(archive.size());
        }
        return entity;
    }

    private static @Nullable ReleaseArchive toArchive(SoftwareReleaseEntity entity) {
        UUID id = entity.getArchiveId();
        return id == null
                ? null
                : new ReleaseArchive(
                        new ReleaseArchiveId(id),
                        Objects.requireNonNull(entity.getArchiveFileName()),
                        Objects.requireNonNull(entity.getArchiveSize()));
    }

    private static SoftwareCredits toCredits(List<? extends CreditColumns> rows) {
        return SoftwareCredits.restore(rows.stream()
                .map(row -> new SoftwareCreditDraft(participant(row), row.getRoleCodes()))
                .toList());
    }

    private static <T extends CreditColumns> List<T> fromCredits(SoftwareCredits credits, Supplier<T> row) {
        return credits.all().stream()
                .map(credit -> fromCredit(credit, row.get()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static SoftwareReference participant(CreditColumns entity) {
        UUID person = entity.getPersonId();
        if (person != null) {
            return new SoftwareReference(SoftwareReferenceType.AUTHOR, new SoftwareReferenceId(person));
        }
        UUID group = entity.getGroupId();
        if (group != null) {
            return new SoftwareReference(SoftwareReferenceType.GROUP, new SoftwareReferenceId(group));
        }
        return new SoftwareReference(
                Boolean.TRUE.equals(entity.getLabelOfGroup())
                        ? SoftwareReferenceType.GROUP_LABEL
                        : SoftwareReferenceType.AUTHOR_LABEL,
                new SoftwareReferenceId(Objects.requireNonNull(entity.getLabelId())));
    }

    private static <T extends CreditColumns> T fromCredit(SoftwareCreditDraft credit, T entity) {
        UUID id = credit.participant().id().value();
        switch (credit.participant().type()) {
            case AUTHOR -> entity.setPersonId(id);
            case GROUP -> entity.setGroupId(id);
            case AUTHOR_LABEL, GROUP_LABEL -> entity.setLabelId(id);
        }
        entity.setRoleCodes(new ArrayList<>(credit.roles()));
        return entity;
    }
}
