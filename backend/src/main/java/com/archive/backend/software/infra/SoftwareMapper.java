package com.archive.backend.software.infra;

import com.archive.backend.author.domain.PersonReference;
import com.archive.backend.author.domain.PersonReferenceId;
import com.archive.backend.author.domain.PersonReferenceType;
import com.archive.backend.category.domain.CategoryId;
import com.archive.backend.category.domain.CategorySet;
import com.archive.backend.hardware.domain.HardwareCode;
import com.archive.backend.hardware.domain.HardwareSet;
import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.software.domain.Release;
import com.archive.backend.software.domain.ReleaseContent;
import com.archive.backend.software.domain.ReleaseDescription;
import com.archive.backend.software.domain.ReleaseId;
import com.archive.backend.software.domain.ReleaseTitle;
import com.archive.backend.software.domain.ReleaseType;
import com.archive.backend.software.domain.ReleaseVersion;
import com.archive.backend.software.domain.Screenshot;
import com.archive.backend.software.domain.ScreenshotFormat;
import com.archive.backend.software.domain.ScreenshotId;
import com.archive.backend.software.domain.Screenshots;
import com.archive.backend.software.domain.Software;
import com.archive.backend.software.domain.SoftwareCreditDraft;
import com.archive.backend.software.domain.SoftwareCredits;
import com.archive.backend.software.domain.SoftwareDescription;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.domain.SoftwareReference;
import com.archive.backend.software.domain.SoftwareReferenceId;
import com.archive.backend.software.domain.SoftwareReferenceType;
import com.archive.backend.software.domain.SoftwareReferences;
import com.archive.backend.software.domain.SoftwareTitle;
import com.archive.backend.user.infra.AddedByRows;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;

/**
 * Software ↔ its JPA entity. Hardware sets are restored without the reference book: they were checked when stored, and
 * every set of the software is on its platform. The categories come restored by the repository, with their lines as
 * the tree has them now.
 */
@Mapper(componentModel = "spring")
public interface SoftwareMapper {

    default Software toDomain(SoftwareEntity entity, CategorySet categories) {
        PlatformCode platform = new PlatformCode(entity.getPlatformCode());
        String description = entity.getDescription();
        return new Software(
                new SoftwareId(entity.getId()),
                new SoftwareTitle(entity.getTitle()),
                description == null ? null : new SoftwareDescription(description),
                entity.getReleaseDate(),
                toHardware(platform, entity.getHardware()),
                categories,
                references(entity),
                SoftwareCredits.restore(entity.getCredits().stream()
                        .map(SoftwareMapper::toCredit)
                        .toList()),
                entity.getReleases().stream()
                        .map(release -> toRelease(platform, release))
                        .toList(),
                AddedByRows.toAddedBy(entity.getAddedBy()),
                toScreenshots(entity.getScreenshots()));
    }

    default SoftwareEntity toEntity(Software software) {
        SoftwareEntity entity = new SoftwareEntity();
        entity.setId(software.getId().value());
        entity.setTitle(software.getTitle().value());
        SoftwareDescription description = software.getDescription();
        entity.setDescription(description == null ? null : description.value());
        entity.setReleaseDate(software.getReleaseDate());
        entity.setPlatformCode(software.getPlatform().value());
        entity.setHardware(fromHardware(software.getHardware()));
        entity.setCategories(
                software.getCategories().ids().stream().map(CategoryId::value).collect(Collectors.toSet()));
        SoftwareReferences references = software.getReferences();
        entity.setPublisherAuthors(ids(references.publishers(), SoftwareReferenceType.AUTHOR));
        entity.setPublisherGroups(ids(references.publishers(), SoftwareReferenceType.GROUP));
        entity.setPublisherAuthorLabels(ids(references.publishers(), SoftwareReferenceType.AUTHOR_LABEL));
        entity.setPublisherGroupLabels(ids(references.publishers(), SoftwareReferenceType.GROUP_LABEL));
        entity.setDeveloperGroups(ids(references.developers(), SoftwareReferenceType.GROUP));
        entity.setDeveloperLabels(ids(references.developers(), SoftwareReferenceType.GROUP_LABEL));
        entity.setCredits(software.getCredits().all().stream()
                .map(SoftwareMapper::fromCredit)
                .collect(Collectors.toCollection(ArrayList::new)));
        entity.setAddedBy(AddedByRows.fromAddedBy(software.getAddedBy()));
        entity.setReleases(software.getReleases().stream()
                .map(SoftwareMapper::fromRelease)
                .collect(Collectors.toCollection(ArrayList::new)));
        entity.setScreenshots(fromScreenshots(software.getScreenshots()));
        return entity;
    }

    private static Screenshots toScreenshots(List<ScreenshotRow> rows) {
        return new Screenshots(rows.stream()
                .map(row -> new Screenshot(new ScreenshotId(row.getId()), ScreenshotFormat.fromCode(row.getFormat())))
                .toList());
    }

    private static List<ScreenshotRow> fromScreenshots(Screenshots screenshots) {
        return screenshots.all().stream()
                .map(screenshot -> new ScreenshotRow(
                        screenshot.id().value(), screenshot.format().code()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static HardwareSet toHardware(PlatformCode platform, Set<String> codes) {
        return HardwareSet.restore(
                platform, codes.stream().map(HardwareCode::new).collect(Collectors.toSet()));
    }

    private static Set<String> fromHardware(HardwareSet hardware) {
        return hardware.codes().stream().map(HardwareCode::value).collect(Collectors.toSet());
    }

    private static SoftwareReferences references(SoftwareEntity entity) {
        Set<SoftwareReference> publishers = new java.util.HashSet<>();
        publishers.addAll(references(entity.getPublisherAuthors(), SoftwareReferenceType.AUTHOR));
        publishers.addAll(references(entity.getPublisherGroups(), SoftwareReferenceType.GROUP));
        publishers.addAll(references(entity.getPublisherAuthorLabels(), SoftwareReferenceType.AUTHOR_LABEL));
        publishers.addAll(references(entity.getPublisherGroupLabels(), SoftwareReferenceType.GROUP_LABEL));
        Set<SoftwareReference> developers = new java.util.HashSet<>();
        developers.addAll(references(entity.getDeveloperGroups(), SoftwareReferenceType.GROUP));
        developers.addAll(references(entity.getDeveloperLabels(), SoftwareReferenceType.GROUP_LABEL));
        return SoftwareReferences.restore(publishers, developers);
    }

    private static Set<SoftwareReference> references(Set<UUID> ids, SoftwareReferenceType type) {
        return ids.stream()
                .map(id -> new SoftwareReference(type, new SoftwareReferenceId(id)))
                .collect(Collectors.toSet());
    }

    private static Set<UUID> ids(Set<SoftwareReference> references, SoftwareReferenceType... types) {
        Set<SoftwareReferenceType> allowed = Set.of(types);
        return references.stream()
                .filter(reference -> allowed.contains(reference.type()))
                .map(reference -> reference.id().value())
                .collect(Collectors.toSet());
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
                        entity.getReleaseDate(),
                        description == null ? null : new ReleaseDescription(description)),
                toHardware(platform, entity.getHardware()),
                AddedByRows.toAddedBy(entity.getAddedBy()),
                toScreenshots(entity.getScreenshots()));
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
        entity.setReleaseDate(content.releaseDate());
        entity.setDescription(description == null ? null : description.value());
        entity.setHardware(fromHardware(release.hardware()));
        entity.setAddedBy(AddedByRows.fromAddedBy(release.addedBy()));
        entity.setScreenshots(fromScreenshots(release.screenshots()));
        return entity;
    }

    private static SoftwareCreditDraft toCredit(SoftwareCreditEntity entity) {
        UUID person = entity.getPersonId();
        return new SoftwareCreditDraft(
                new PersonReference(
                        person == null ? PersonReferenceType.AUTHOR_LABEL : PersonReferenceType.AUTHOR,
                        new PersonReferenceId(person == null ? Objects.requireNonNull(entity.getLabelId()) : person)),
                entity.getRoleCodes());
    }

    private static SoftwareCreditEntity fromCredit(SoftwareCreditDraft credit) {
        SoftwareCreditEntity entity = new SoftwareCreditEntity();
        UUID id = credit.person().id().value();
        if (credit.person().type() == PersonReferenceType.AUTHOR) {
            entity.setPersonId(id);
        } else {
            entity.setLabelId(id);
        }
        entity.setRoleCodes(new ArrayList<>(credit.roles()));
        return entity;
    }
}
